package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import prog2.policia_frontend.clients.RolClient;
import prog2.policia_frontend.clients.UsuarioClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.RolDTO;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.dtos.UsuarioFormularioDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Usuarios | Sistema Policial")
public class UsuariosView extends VerticalLayout implements BeforeEnterObserver {

    private final UsuarioClient usuarioClient;
    private final RolClient rolClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<UsuarioDTO> tablaUsuarios = new Grid<>(UsuarioDTO.class, false);
    private final PaginadorGrid<UsuarioDTO> paginadorUsuarios = new PaginadorGrid<>(tablaUsuarios, 10);

    private final TextField filtroNombre = new TextField("Buscar por nombre");
    private final Select<String> filtroEstado = new Select<>();
    private final Select<RolDTO> filtroRol = new Select<>();

    private final Button botonBuscar = new Button("Buscar");
    private final Button botonNuevoUsuario = new Button("Nuevo usuario");
    private final Button botonLimpiar = new Button("Limpiar");

    public UsuariosView(UsuarioClient usuarioClient, RolClient rolClient, SesionUsuario sesionUsuario) {
        this.usuarioClient = usuarioClient;
        this.rolClient = rolClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltros();
        configurarVista();
        configurarTabla();
    }

    private void configurarFiltros() {
        filtroNombre.setPlaceholder("Ejemplo: Laura");
        filtroNombre.setClearButtonVisible(true);

        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todos", "Activos", "Inactivos");
        filtroEstado.setValue("Todos");

        filtroRol.setLabel("Tipo de usuario");
        filtroRol.setItemLabelGenerator(RolDTO::getNombre);
        filtroRol.setPlaceholder("Todos los roles");

        botonBuscar.addClickListener(evento -> cargarUsuarios());

        botonLimpiar.addClickListener(evento -> {
            filtroNombre.clear();
            filtroEstado.setValue("Todos");
            filtroRol.clear();
            cargarUsuarios();
        });
    }

    private void configurarVista() {
        H2 titulo = new H2("Usuarios");

        HorizontalLayout barraFiltros = new HorizontalLayout(
                filtroNombre, filtroEstado, filtroRol, botonBuscar, botonLimpiar
        );

        barraFiltros.setAlignItems(HorizontalLayout.Alignment.END);

        setSizeFull();

        botonNuevoUsuario.addClickListener(evento -> abrirFormularioNuevo());
        botonNuevoUsuario.setVisible(puedeEscribir());

        add(titulo, botonNuevoUsuario, barraFiltros, tablaUsuarios, paginadorUsuarios.getControles());
    }

    private void configurarTabla() {
        tablaUsuarios.addColumn(UsuarioDTO::getCodigo).setHeader("Código").setAutoWidth(true);
        tablaUsuarios.addColumn(UsuarioDTO::getNombre).setHeader("Nombre").setAutoWidth(true);
        tablaUsuarios.addColumn(UsuarioDTO::getEdad).setHeader("Edad").setAutoWidth(true);
        tablaUsuarios.addColumn(usuario -> usuario.getFechaNacimiento() != null
                ? usuario.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "").setHeader("Fecha de nacimiento").setAutoWidth(true);
        tablaUsuarios.addColumn(UsuarioDTO::getRol).setHeader("Rol").setAutoWidth(true);

        tablaUsuarios.addColumn(usuario -> Boolean.TRUE.equals(usuario.getActivo()) ? "Activo" : "Inactivo")
                .setHeader("Estado").setAutoWidth(true);

        tablaUsuarios.addColumn(usuario -> usuario.getMotivoBaja() != null ? usuario.getMotivoBaja() : "-")
                .setHeader("Motivo de baja").setAutoWidth(true);

        tablaUsuarios.addComponentColumn(usuario -> {
            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            Button botonModificar = new Button("Modificar");
            botonModificar.setEnabled(Boolean.TRUE.equals(usuario.getActivo()));
            botonModificar.addClickListener(evento -> abrirFormularioEdicion(usuario));

            if (!Boolean.TRUE.equals(usuario.getActivo())) {
                botonModificar.setTooltipText("Debe reactivar el usuario antes de modificarlo.");
            }

            Button botonEstado = new Button();

            if (Boolean.TRUE.equals(usuario.getActivo())) {
                botonEstado.setText("Dar de baja");
                botonEstado.addClickListener(evento -> abrirDialogoBaja(usuario));
            } else if ("FALLECIMIENTO".equals(usuario.getMotivoBaja())) {
                botonEstado.setText("No reactivable");
                botonEstado.setEnabled(false);
                botonEstado.setTooltipText("El motivo de baja fue fallecimiento.");
            } else {
                botonEstado.setText("Reactivar");
                botonEstado.addClickListener(evento -> reactivarUsuario(usuario));
            }

            return new HorizontalLayout(botonModificar, botonEstado);
        }).setHeader("Acciones").setAutoWidth(true);

        tablaUsuarios.setWidthFull();
        tablaUsuarios.setAllRowsVisible(true);
        tablaUsuarios.addThemeVariants(GridVariant.LUMO_COMPACT);
    }

    private DatePicker crearCampoFechaNacimiento() {
        DatePicker campo = new DatePicker("Fecha de nacimiento");
        campo.setRequiredIndicatorVisible(true);
        campo.setMax(LocalDate.now());
        campo.setWidthFull();
        return campo;
    }

    private boolean fechaNacimientoInvalida(LocalDate fecha, boolean vigilante) {
        if (fecha == null || fecha.isAfter(LocalDate.now())) {
            return true;
        }

        int edad = Period.between(fecha, LocalDate.now()).getYears();

        if (edad < 18) {
            return true;
        }

        return vigilante && edad > 65;
    }

    private void validarCampoFecha(DatePicker campo, boolean vigilante) {
        boolean invalida = fechaNacimientoInvalida(campo.getValue(), vigilante);

        campo.setInvalid(invalida);
        campo.setErrorMessage(vigilante
                ? "El vigilante debe tener entre 18 y 65 años."
                : "El usuario debe tener al menos 18 años.");
    }

    private void abrirFormularioEdicion(UsuarioDTO usuario) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Modificar usuario " + usuario.getCodigo());

        TextField campoNombre = new TextField("Nombre");
        campoNombre.setValue(usuario.getNombre() != null ? usuario.getNombre() : "");
        campoNombre.setRequiredIndicatorVisible(true);
        campoNombre.setWidthFull();

        DatePicker campoFechaNacimiento = crearCampoFechaNacimiento();

        if (usuario.getFechaNacimiento() != null) {
            campoFechaNacimiento.setValue(usuario.getFechaNacimiento());
        }

        PasswordField campoPassword = new PasswordField("Nueva contraseña");
        campoPassword.setHelperText("Dejar vacío para conservar la contraseña actual.");
        campoPassword.setWidthFull();

        Button botonGuardar = new Button("Guardar cambios");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());

        botonGuardar.addClickListener(evento -> {
            boolean nombreVacio = campoNombre.getValue().isBlank();
            String nuevaPassword = campoPassword.getValue();
            boolean passwordInvalida = !nuevaPassword.isBlank()
                    && (nuevaPassword.length() < 4 || nuevaPassword.length() > 20);

            validarCampoFecha(campoFechaNacimiento, esVigilante(usuario));

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage("El nombre es obligatorio.");

            campoPassword.setInvalid(passwordInvalida);
            campoPassword.setErrorMessage("La contraseña debe tener entre 4 y 20 caracteres.");

            if (nombreVacio || passwordInvalida || campoFechaNacimiento.isInvalid()) {
                return;
            }

            String passwordParaEnviar = nuevaPassword.isBlank() ? null : nuevaPassword;

            UsuarioFormularioDTO datos = new UsuarioFormularioDTO(
                    campoNombre.getValue().trim(),
                    passwordParaEnviar,
                    null,
                    campoFechaNacimiento.getValue()
            );

            try {
                UsuarioDTO usuarioModificado = usuarioClient.actualizar(usuario.getCodigo(), datos);

                dialogo.close();
                cargarUsuarios();

                NotificacionUtils.mostrarExito(
                        "Usuario " + usuarioModificado.getCodigo() + " - "
                        + usuarioModificado.getNombre() + " modificado correctamente."
                );

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(new VerticalLayout(
                campoNombre,
                campoFechaNacimiento,
                campoPassword,
                new HorizontalLayout(botonGuardar, botonCancelar)
        ));

        dialogo.open();
    }

    private void abrirFormularioNuevo() {
        List<RolDTO> rolesActivos;

        try {
            rolesActivos = rolClient.listar(true);
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar los roles.");
            return;
        }

        if (rolesActivos.isEmpty()) {
            NotificacionUtils.mostrarInformacion("No existen roles activos disponibles.");
            return;
        }

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Crear nuevo usuario");

        TextField campoNombre = new TextField("Nombre");
        PasswordField campoPassword = new PasswordField("Contraseña");
        DatePicker campoFechaNacimiento = crearCampoFechaNacimiento();

        Select<RolDTO> campoRol = new Select<>();
        campoRol.setLabel("Rol");
        campoRol.setItemLabelGenerator(RolDTO::getNombre);
        campoRol.setItems(rolesActivos);

        campoNombre.setRequiredIndicatorVisible(true);
        campoPassword.setRequiredIndicatorVisible(true);
        campoRol.setRequiredIndicatorVisible(true);

        campoNombre.setWidthFull();
        campoPassword.setWidthFull();
        campoRol.setWidthFull();

        campoRol.addValueChangeListener(evento -> {
            campoFechaNacimiento.setInvalid(false);
        });

        Button botonGuardar = new Button("Guardar");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());

        botonGuardar.addClickListener(evento -> {
            boolean nombreVacio = campoNombre.getValue().isBlank();
            boolean passwordVacio = campoPassword.getValue().isBlank();
            boolean passwordInvalida = passwordVacio || campoPassword.getValue().length() < 4 || campoPassword.getValue().length() > 20;
            boolean rolVacio = campoRol.getValue() == null;
            boolean rolVigilante = esRolVigilante(campoRol.getValue());

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage("El nombre es obligatorio.");

            campoPassword.setInvalid(passwordInvalida);
            campoPassword.setErrorMessage("La contraseña debe tener entre 4 y 20 caracteres.");

            campoRol.setInvalid(rolVacio);
            campoRol.setErrorMessage("Debe seleccionar un rol.");

            validarCampoFecha(campoFechaNacimiento, rolVigilante);

            if (nombreVacio || passwordInvalida || rolVacio || campoFechaNacimiento.isInvalid()) {
                return;
            }

            RolDTO rolSeleccionado = campoRol.getValue();

            try {
                UsuarioFormularioDTO datosUsuario = new UsuarioFormularioDTO(
                        campoNombre.getValue().trim(),
                        campoPassword.getValue(),
                        rolSeleccionado.getNombre(),
                        campoFechaNacimiento.getValue()
                );

                UsuarioDTO creado = usuarioClient.crear(datosUsuario);
                String codigoCreado = creado.getCodigo();

                dialogo.close();
                cargarUsuarios();

                NotificacionUtils.mostrarExito("Usuario creado correctamente. Código: " + codigoCreado);

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(new VerticalLayout(
                campoNombre,
                campoPassword,
                campoRol,
                campoFechaNacimiento,
                new HorizontalLayout(botonGuardar, botonCancelar)
        ));

        dialogo.open();
    }

    private void abrirDialogoBaja(UsuarioDTO usuario) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Dar de baja a " + usuario.getNombre());

        Select<String> campoMotivo = crearCampoMotivo();

        Button botonConfirmar = new Button("Confirmar baja");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());

        botonConfirmar.addClickListener(evento -> {
            if (!validarMotivo(campoMotivo)) {
                return;
            }

            try {
                usuarioClient.darDeBaja(usuario.getCodigo(), campoMotivo.getValue());

                dialogo.close();
                cargarUsuarios();

                NotificacionUtils.mostrarExito("Usuario dado de baja correctamente.");

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(campoMotivo, new HorizontalLayout(botonConfirmar, botonCancelar));
        dialogo.open();
    }

    private Select<String> crearCampoMotivo() {
        Select<String> campoMotivo = new Select<>();

        campoMotivo.setLabel("Motivo de baja");
        campoMotivo.setItems(
                "FALLECIMIENTO",
                "JUBILACION",
                "LICENCIA",
                "RENUNCIA",
                "DESPIDO",
                "ABANDONO_TRABAJO",
                "INCAPACIDAD"
        );
        campoMotivo.setRequiredIndicatorVisible(true);

        return campoMotivo;
    }

    private boolean validarMotivo(Select<String> campoMotivo) {
        String motivo = campoMotivo.getValue();
        boolean motivoVacio = motivo == null || motivo.isBlank();

        campoMotivo.setInvalid(motivoVacio);
        campoMotivo.setErrorMessage("Debe seleccionar un motivo.");

        return !motivoVacio;
    }

    private void reactivarUsuario(UsuarioDTO usuario) {
        try {
            usuarioClient.reactivar(usuario.getCodigo());
            cargarUsuarios();

            NotificacionUtils.mostrarExito("Usuario reactivado correctamente.");

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalArgumentException excepcion) {
            NotificacionUtils.mostrarInformacion(excepcion.getMessage());
        }
    }

    private void cargarOpcionesFiltros() {
        try {
            if (sesionUsuario.tieneRol("ADMINISTRADOR")) {
                filtroRol.setItems(rolClient.listar(null));
                return;
            }

            List<UsuarioDTO> usuarios = usuarioClient.listar(null, null);
            List<RolDTO> roles = new ArrayList<>();
            java.util.Set<String> nombresAgregados = new java.util.TreeSet<>(
                    String.CASE_INSENSITIVE_ORDER
            );

            for (UsuarioDTO usuario : usuarios) {
                String nombreRol = usuario.getRol();

                if (nombreRol != null && !nombreRol.isBlank() && nombresAgregados.add(nombreRol)) {
                    RolDTO rol = new RolDTO();
                    rol.setNombre(nombreRol);
                    roles.add(rol);
                }
            }

            filtroRol.setItems(roles);
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pudieron cargar los roles para los filtros.");
        }
    }

    private Boolean obtenerEstadoSeleccionado() {
        switch (filtroEstado.getValue()) {
            case "Activos":
                return true;
            case "Inactivos":
                return false;
            default:
                return null;
        }
    }

    private void cargarUsuarios() {
        try {
            Boolean estadoSeleccionado = obtenerEstadoSeleccionado();
            String nombreIngresado = filtroNombre.getValue();
            RolDTO rolSeleccionado = filtroRol.getValue();

            List<UsuarioDTO> todosLosUsuarios = usuarioClient.listar(estadoSeleccionado, nombreIngresado);
            List<UsuarioDTO> usuariosFiltrados = new ArrayList<>();

            for (UsuarioDTO usuario : todosLosUsuarios) {
                if (rolSeleccionado == null) {
                    usuariosFiltrados.add(usuario);
                    continue;
                }

                if (usuario.getRol() == null) {
                    continue;
                }

                boolean coincideCodigo = usuario.getRol().equalsIgnoreCase(rolSeleccionado.getCodigo());
                boolean coincideNombre = usuario.getRol().equalsIgnoreCase(rolSeleccionado.getNombre());

                if (coincideCodigo || coincideNombre) {
                    usuariosFiltrados.add(usuario);
                }
            }

            usuariosFiltrados.sort(new java.util.Comparator<UsuarioDTO>() {
                @Override
                public int compare(UsuarioDTO primero, UsuarioDTO segundo) {
                    String codigoPrimero = primero.getCodigo();
                    String codigoSegundo = segundo.getCodigo();

                    if (codigoPrimero == null && codigoSegundo == null) {
                        return 0;
                    }

                    if (codigoPrimero == null) {
                        return 1;
                    }

                    if (codigoSegundo == null) {
                        return -1;
                    }

                    return codigoPrimero.compareToIgnoreCase(codigoSegundo);
                }
            });

            paginadorUsuarios.setItems(usuariosFiltrados);

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalStateException excepcion) {
            NotificacionUtils.mostrarInformacion("Debe iniciar sesión.");
        }
    }

    private boolean puedeEscribir() {
        return sesionUsuario.tieneRol("ADMINISTRADOR") || sesionUsuario.tienePermiso(PermisosApp.ESCRITURA + "_USUARIO");
    }

    private boolean esVigilante(UsuarioDTO usuario) {
        return usuario.getRol() != null && usuario.getRol().equalsIgnoreCase("VIGILANTE");
    }

    private boolean esRolVigilante(RolDTO rol) {
        return rol != null
                && rol.getNombre() != null
                && rol.getNombre().equalsIgnoreCase("VIGILANTE");
    }

    private void mostrarErrorOperacion(RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarOpcionesFiltros();
        cargarUsuarios();
    }
}
