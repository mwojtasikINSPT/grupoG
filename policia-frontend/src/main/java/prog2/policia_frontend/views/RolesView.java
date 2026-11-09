package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import prog2.policia_frontend.clients.RolClient;
import prog2.policia_frontend.dtos.RolDTO;
import prog2.policia_frontend.dtos.RolPermisoEntidadDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

// Vista utilizada para administrar los roles del sistema.
@Route(value = "roles", layout = MainLayout.class)
@PageTitle("Roles | Sistema Policial")
public class RolesView extends VerticalLayout implements BeforeEnterObserver {

    private final RolClient rolClient;
    private final PermisosInterfaz permisosInterfaz;
    private final Grid<RolDTO> tablaRoles = new Grid<>(RolDTO.class, false);
    private final Select<String> filtroEstado = new Select<>();
    private List<String> entidadesDisponibles = List.of();

    public RolesView(RolClient rolClient, PermisosInterfaz permisosInterfaz) {
        this.rolClient = rolClient;
        this.permisosInterfaz = permisosInterfaz;
        configurarFiltro();
        configurarTabla();
        configurarVista();
    }

    private void configurarFiltro() {
        filtroEstado.setLabel("Mostrar");
        filtroEstado.setItems("Todos", "Activos", "Inactivos");
        filtroEstado.setValue("Todos");
        filtroEstado.addValueChangeListener(evento -> cargarRoles());
    }

    private void configurarTabla() {
        tablaRoles.addColumn(RolDTO::getCodigo).setHeader("Código").setAutoWidth(true);
        tablaRoles.addColumn(RolDTO::getNombre).setHeader("Nombre").setAutoWidth(true);
        tablaRoles.addColumn(RolDTO::getPrefijo).setHeader("Prefijo").setAutoWidth(true);
        tablaRoles.addComponentColumn(rol -> new Button("Ver permisos", evento -> mostrarPermisos(rol))).setHeader("Permisos").setAutoWidth(true);
        tablaRoles.addColumn(rol -> rol.getDescripcion() != null ? rol.getDescripcion() : "-").setHeader("Descripción").setAutoWidth(true);
        tablaRoles.addColumn(rol -> Boolean.TRUE.equals(rol.getActivo()) ? "Activo" : "Inactivo").setHeader("Estado").setAutoWidth(true);
        tablaRoles.addComponentColumn(rol -> {
            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }
            boolean estaActivo = Boolean.TRUE.equals(rol.getActivo());
            Button botonModificar = new Button("Modificar");
            botonModificar.setEnabled(estaActivo);
            if (estaActivo) {
                botonModificar.addClickListener(evento -> abrirFormulario(rol));
            } else {
                botonModificar.setTooltipText("Debe reactivar el rol antes de modificarlo.");
            }
            Button botonEstado = new Button(estaActivo ? "Dar de baja" : "Reactivar");
            if (estaActivo) {
                botonEstado.addClickListener(evento -> confirmarBaja(rol));
            } else {
                botonEstado.addClickListener(evento -> reactivarRol(rol));
            }
            return new HorizontalLayout(botonModificar, botonEstado);
        }).setHeader("Acciones").setAutoWidth(true);
        tablaRoles.setSizeFull();
    }

    private void configurarVista() {
        H2 titulo = new H2("Roles");
        Button botonNuevo = new Button("Nuevo rol", evento -> abrirFormulario(null));
        botonNuevo.setVisible(puedeEscribir());
        Button botonActualizar = new Button("Actualizar", evento -> cargarRoles());
        HorizontalLayout controles = new HorizontalLayout(filtroEstado, botonActualizar);
        controles.setAlignItems(HorizontalLayout.Alignment.END);
        setSizeFull();
        add(titulo, botonNuevo, controles, tablaRoles);
        expand(tablaRoles);
    }

    private void abrirFormulario(RolDTO rol) {
        boolean esCreacion = rol == null;

        try {
            entidadesDisponibles = rolClient.listarEntidades();
            if (entidadesDisponibles.isEmpty()) {
                NotificacionUtils.mostrarInformacion("No se pudieron obtener las entidades.");
                return;
            }
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            return;
        }

        Dialog dialogo = new Dialog();
        dialogo.setWidth("600px");
        dialogo.setHeaderTitle(esCreacion ? "Nuevo rol" : "Modificar " + rol.getCodigo());
        TextField campoNombre = new TextField("Nombre");
        TextField campoPrefijo = new TextField("Prefijo");
        TextArea campoDescripcion = new TextArea("Descripción");
        Select<String> tipoPermiso = new Select<>();
        tipoPermiso.setLabel("1. Elegí un permiso");
        tipoPermiso.setItems(PermisosApp.ESCRITURA, PermisosApp.LECTURA, PermisosApp.LECTURA_DATOS_PROPIOS);
        tipoPermiso.setItemLabelGenerator(this::nombrePermiso);
        tipoPermiso.setPlaceholder("Seleccioná un permiso");
        tipoPermiso.setWidthFull();
        MultiSelectComboBox<String> escritura = crearSelectorEntidades(PermisosApp.ESCRITURA);
        MultiSelectComboBox<String> lectura = crearSelectorEntidades(PermisosApp.LECTURA);
        MultiSelectComboBox<String> propios = crearSelectorEntidades(PermisosApp.LECTURA_DATOS_PROPIOS);
        VerticalLayout opcionesEscritura = crearOpcionesEntidades(escritura, PermisosApp.ESCRITURA);
        VerticalLayout opcionesLectura = crearOpcionesEntidades(lectura, PermisosApp.LECTURA);
        VerticalLayout opcionesPropios = crearOpcionesEntidades(propios, PermisosApp.LECTURA_DATOS_PROPIOS);
        opcionesEscritura.setVisible(false);
        opcionesLectura.setVisible(false);
        opcionesPropios.setVisible(false);
        tipoPermiso.addValueChangeListener(evento -> {
            String seleccionado = evento.getValue();
            opcionesEscritura.setVisible(PermisosApp.ESCRITURA.equals(seleccionado));
            opcionesLectura.setVisible(PermisosApp.LECTURA.equals(seleccionado));
            opcionesPropios.setVisible(PermisosApp.LECTURA_DATOS_PROPIOS.equals(seleccionado));
        });
        Button continuar = new Button("Continuar con lectura", evento -> {
            Dialog confirmacion = new Dialog();
            confirmacion.setHeaderTitle("Permisos de lectura");
            Span pregunta = new Span("¿Querés aplicar a lectura las mismas entidades seleccionadas para escritura?");
            Button si = new Button("Sí", clic -> {
                lectura.setValue(new HashSet<>(escritura.getSelectedItems()));
                confirmacion.close();
                tipoPermiso.setValue(PermisosApp.LECTURA);
            });
            Button no = new Button("No, elegir manualmente", clic -> {
                confirmacion.close();
                tipoPermiso.setValue(PermisosApp.LECTURA);
            });
            confirmacion.add(new VerticalLayout(pregunta, new HorizontalLayout(si, no)));
            confirmacion.open();
        });
        opcionesEscritura.add(continuar);
        campoNombre.setRequiredIndicatorVisible(true);
        campoPrefijo.setRequiredIndicatorVisible(true);
        campoNombre.setWidthFull();
        campoPrefijo.setWidthFull();
        campoDescripcion.setWidthFull();
        campoPrefijo.setHelperText("Se utiliza para generar códigos de usuarios del rol.");
        if (!esCreacion) {
            campoNombre.setValue(valorSeguro(rol.getNombre()));
            campoPrefijo.setValue(valorSeguro(rol.getPrefijo()));
            campoDescripcion.setValue(valorSeguro(rol.getDescripcion()));
            if (rol.getPermisosPorEntidad() != null) {
                incluirEntidadesExistentes(escritura, PermisosApp.ESCRITURA, rol.getPermisosPorEntidad());
                incluirEntidadesExistentes(lectura, PermisosApp.LECTURA, rol.getPermisosPorEntidad());
                incluirEntidadesExistentes(propios, PermisosApp.LECTURA_DATOS_PROPIOS, rol.getPermisosPorEntidad());
                for (RolPermisoEntidadDTO permiso : rol.getPermisosPorEntidad()) {
                    if (permiso == null || permiso.getEntidad() == null) {
                        continue;
                    }
                    if (PermisosApp.ESCRITURA.equals(permiso.getPermiso())) {
                        escritura.select(permiso.getEntidad());
                    } else if (PermisosApp.LECTURA.equals(permiso.getPermiso())) {
                        lectura.select(permiso.getEntidad());
                    } else if (PermisosApp.LECTURA_DATOS_PROPIOS.equals(permiso.getPermiso())) {
                        propios.select(permiso.getEntidad());
                    }
                }
            }
        }
        Button botonGuardar = new Button(esCreacion ? "Guardar" : "Guardar cambios");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());
        botonGuardar.addClickListener(evento -> {
            String nombre = campoNombre.getValue().trim();
            String prefijo = campoPrefijo.getValue().trim();
            Set<String> lecturasFaltantes = new HashSet<>();
            if (escritura.getSelectedItems().contains("ASALTO")) {
                if (!lectura.getSelectedItems().contains("ASALTANTE")) {
                    lecturasFaltantes.add("ASALTANTE");
                }
                if (!lectura.getSelectedItems().contains("SUCURSAL")) {
                    lecturasFaltantes.add("SUCURSAL");
                }
            }
            if (escritura.getSelectedItems().contains("CASO_JUDICIAL")) {
                if (!lectura.getSelectedItems().contains("ASALTO")) {
                    lecturasFaltantes.add("ASALTO");
                }
                if (!lectura.getSelectedItems().contains("JUEZ")) {
                    lecturasFaltantes.add("JUEZ");
                }
            }
            if (!lecturasFaltantes.isEmpty()) {
                Dialog aviso = new Dialog();
                aviso.setHeaderTitle("Permisos de lectura necesarios");
                Span mensaje = new Span("Para estos permisos de escritura también se agregará lectura de: " + String.join(", ", lecturasFaltantes) + ".");
                Button aceptar = new Button("Agregar permisos", clic -> {
                    lectura.select(lecturasFaltantes);
                    aviso.close();
                    NotificacionUtils.mostrarInformacion("Lecturas agregadas. Revisá los permisos y presioná Guardar.");
                });
                Button cancelar = new Button("Cancelar", clic -> aviso.close());
                aviso.add(new VerticalLayout(mensaje, new HorizontalLayout(aceptar, cancelar)));
                aviso.open();
                return;
            }
            Set<String> permisos = new HashSet<>();
            agregarPermisos(permisos, PermisosApp.ESCRITURA, escritura.getSelectedItems());
            agregarPermisos(permisos, PermisosApp.LECTURA, lectura.getSelectedItems());
            agregarPermisos(permisos, PermisosApp.LECTURA_DATOS_PROPIOS, propios.getSelectedItems());
            boolean nombreVacio = nombre.isBlank();
            boolean prefijoVacio = prefijo.isBlank();
            boolean permisosVacios = permisos.isEmpty();
            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage("El nombre es obligatorio.");
            campoPrefijo.setInvalid(prefijoVacio);
            campoPrefijo.setErrorMessage("El prefijo es obligatorio.");
            if (permisosVacios) {
                NotificacionUtils.mostrarInformacion("Seleccioná al menos un permiso y una entidad.");
            }
            if (nombreVacio || prefijoVacio || permisosVacios) {
                return;
            }
            try {
                if (existeNombreOPrefijoRepetido(rol, nombre, prefijo)) {
                    return;
                }
                RolDTO datos = new RolDTO();
                datos.setNombre(nombre);
                datos.setPrefijo(prefijo);
                datos.setDescripcion(campoDescripcion.getValue().trim());
                datos.setPermisosPorEntidad(convertirPermisos(permisos));
                if (esCreacion) {
                    rolClient.crear(datos);
                } else {
                    rolClient.actualizar(rol.getCodigo(), datos);
                }
                dialogo.close();
                cargarRoles();
                NotificacionUtils.mostrarExito(esCreacion ? "Rol creado correctamente." : "Rol modificado correctamente.");
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });
        dialogo.add(
                new VerticalLayout(
                        campoNombre,
                        campoPrefijo,
                        campoDescripcion,
                        tipoPermiso,
                        opcionesEscritura,
                        opcionesLectura,
                        opcionesPropios,
                        new HorizontalLayout(
                                botonGuardar,
                                botonCancelar
                        )
                )
        );
        dialogo.open();
    }

    private String nombrePermiso(String permiso) {
        if (PermisosApp.ESCRITURA.equals(permiso)) {
            return "Escritura";
        }
        if (PermisosApp.LECTURA.equals(permiso)) {
            return "Lectura";
        }
        return "Lectura de datos propios";
    }

    private List<String> entidadesPara(String permiso) {
        List<String> entidades = new java.util.ArrayList<>();
        String prefijo = permiso + " · ";
        for (String opcion : opcionesPermisos()) {
            if (opcion.startsWith(prefijo)) {
                entidades.add(opcion.substring(prefijo.length()));
            }
        }
        return entidades;
    }

    private MultiSelectComboBox<String> crearSelectorEntidades(String permiso) {
        MultiSelectComboBox<String> selector = new MultiSelectComboBox<>("2. Elegí las entidades");
        selector.setItems(entidadesPara(permiso));
        selector.setWidthFull();
        return selector;
    }

    private void incluirEntidadesExistentes(MultiSelectComboBox<String> selector, String tipo, Set<RolPermisoEntidadDTO> permisos) {
        List<String> entidades = entidadesPara(tipo);
        for (RolPermisoEntidadDTO permiso : permisos) {
            if (permiso != null && tipo.equals(permiso.getPermiso()) && permiso.getEntidad() != null
                    && !entidades.contains(permiso.getEntidad())) {
                entidades.add(permiso.getEntidad());
            }
        }
        selector.setItems(entidades);
    }

    private VerticalLayout crearOpcionesEntidades(MultiSelectComboBox<String> selector, String permiso) {
        VerticalLayout opciones = new VerticalLayout();
        opciones.setPadding(false);
        opciones.add(selector);
        if (!PermisosApp.LECTURA_DATOS_PROPIOS.equals(permiso)) {
            Button seleccionar = new Button("Seleccionar todas", evento -> selector.select(entidadesPara(permiso)));
            Button deseleccionar = new Button("Deseleccionar todas", evento -> selector.clear());
            opciones.add(new HorizontalLayout(seleccionar, deseleccionar));
        }
        return opciones;
    }

    private void agregarPermisos(Set<String> permisos, String tipo, Set<String> entidades) {
        for (String entidad : entidades) {
            permisos.add(tipo + " · " + entidad);
        }
    }

    private List<String> opcionesPermisos() {
        List<String> opciones = new java.util.ArrayList<>();

        for (String entidad : entidadesDisponibles) {
            for (String permiso : PermisosApp.todos()) {
                if (PermisosApp.LECTURA_DATOS_PROPIOS.equals(permiso) && !"USUARIO".equals(entidad) && !"CONTRATO_VIGILANCIA".equals(entidad)) {
                    continue;
                }
                opciones.add(permiso + " · " + entidad);
            }
        }
        return opciones;
    }

    private Set<String> etiquetasPermisos(Set<RolPermisoEntidadDTO> permisos) {
        Set<String> etiquetas = new HashSet<>();
        if (permisos != null) {
            for (RolPermisoEntidadDTO permiso : permisos) {
                if (permiso != null && permiso.getPermiso() != null && permiso.getEntidad() != null) {
                    etiquetas.add(permiso.getPermiso() + " · " + permiso.getEntidad());
                }
            }
        }
        return etiquetas;
    }

    private Set<RolPermisoEntidadDTO> convertirPermisos(Set<String> seleccionados) {
        Set<RolPermisoEntidadDTO> resultado = new HashSet<>();
        for (String seleccionado : seleccionados) {
            String[] partes = seleccionado.split(" · ", 2);
            if (partes.length == 2) {
                resultado.add(new RolPermisoEntidadDTO(partes[0], partes[1]));
            }
        }
        return resultado;
    }

    private boolean existeNombreOPrefijoRepetido(RolDTO rolActual, String nombre, String prefijo) {
        List<RolDTO> roles = rolClient.listar(null);
        for (RolDTO existente : roles) {
            boolean mismoRol = rolActual != null
                    && existente.getCodigo() != null && existente.getCodigo().equalsIgnoreCase(rolActual.getCodigo());
            if (mismoRol) {
                continue;
            }
            if (existente.getNombre() != null && existente.getNombre().equalsIgnoreCase(nombre)) {
                NotificacionUtils.mostrarInformacion("Ya existe un rol con ese nombre.");
                return true;
            }
            if (existente.getPrefijo() != null && existente.getPrefijo().equalsIgnoreCase(prefijo)) {
                NotificacionUtils.mostrarInformacion("Ya existe un rol con ese prefijo.");
                return true;
            }
        }
        return false;
    }

    private void confirmarBaja(RolDTO rol) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Dar de baja " + rol.getCodigo());
        Span mensaje = new Span("El rol dejará de estar disponible para nuevas asignaciones.");
        Button botonConfirmar = new Button(
                "Confirmar baja",
                evento -> {
                    darDeBaja(rol);
                    dialogo.close();
                }
        );
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());
        dialogo.add(new VerticalLayout(mensaje, new HorizontalLayout(botonConfirmar, botonCancelar)));
        dialogo.open();
    }

    private void darDeBaja(RolDTO rol) {
        try {
            rolClient.darDeBaja(rol.getCodigo());
            cargarRoles();
            NotificacionUtils.mostrarExito("Rol dado de baja correctamente.");
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        }
    }

    private void reactivarRol(RolDTO rol) {
        try {
            rolClient.reactivar(rol.getCodigo());
            cargarRoles();
            NotificacionUtils.mostrarExito("Rol reactivado correctamente.");
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        }
    }

    private Boolean obtenerEstadoSeleccionado() {
        return switch (filtroEstado.getValue()) {
            case "Activos" ->
                true;
            case "Inactivos" ->
                false;
            default ->
                null;
        };
    }

    private void cargarRoles() {
        try {
            List<RolDTO> roles = rolClient.listar(obtenerEstadoSeleccionado());
            tablaRoles.setItems(roles);
            if (roles.isEmpty()) {
                NotificacionUtils.mostrarInformacion("No existen roles con el estado seleccionado.");
            }
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalStateException excepcion) {
            NotificacionUtils.mostrarInformacion("Debe iniciar sesión.");
        }
    }

    private void mostrarErrorOperacion(RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor : "";
    }

    private boolean puedeEscribir() {
        return permisosInterfaz.puedeAdministrarRoles();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarRoles();
    }

    private void mostrarPermisos(RolDTO rol) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Permisos de " + rol.getNombre());
        dialogo.setWidth("450px");
        VerticalLayout contenido = new VerticalLayout();
        contenido.setPadding(false);
        contenido.setSpacing(true);
        String[] tipos = {
            PermisosApp.LECTURA,
            PermisosApp.ESCRITURA,
            PermisosApp.LECTURA_DATOS_PROPIOS
        };
        for (String tipo : tipos) {
            StringBuilder entidades = new StringBuilder();
            if (rol.getPermisosPorEntidad() != null) {
                for (RolPermisoEntidadDTO permiso : rol.getPermisosPorEntidad()) {
                    if (permiso != null && tipo.equals(permiso.getPermiso()) && permiso.getEntidad() != null) {
                        if (entidades.length() > 0) {
                            entidades.append(", ");
                        }
                        entidades.append(permiso.getEntidad().replace('_', ' '));
                    }
                }
            }
            if (entidades.length() == 0) {
                continue;
            }
            String titulo;
            if (PermisosApp.LECTURA.equals(tipo)) {
                titulo = "Lectura";
            } else if (PermisosApp.ESCRITURA.equals(tipo)) {
                titulo = "Escritura";
            } else {
                titulo = "Lectura de datos propios";
            }
            Span encabezado = new Span(titulo);
            encabezado.getStyle().set("font-weight", "bold");
            Span detalle = new Span(entidades.toString());
            VerticalLayout grupo = new VerticalLayout(encabezado, detalle);
            grupo.setPadding(false);
            grupo.setSpacing(false);
            contenido.add(grupo);
        }
        Button cerrar = new Button("Cerrar", evento -> dialogo.close());
        contenido.add(cerrar);
        dialogo.add(contenido);
        dialogo.open();
    }
}
