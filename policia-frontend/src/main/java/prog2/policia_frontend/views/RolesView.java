package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
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
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;

/* Pantalla utilizada para administrar los roles del sistema. */
@Route(value = "roles", layout = MainLayout.class)
@PageTitle("Roles | Sistema Policial")
public class RolesView extends VerticalLayout
        implements BeforeEnterObserver {

    private final RolClient rolClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<RolDTO> tablaRoles
            = new Grid<>(RolDTO.class, false);

    private final Select<String> filtroEstado
            = new Select<>();

    public RolesView(
            RolClient rolClient,
            SesionUsuario sesionUsuario) {

        this.rolClient = rolClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltro();
        configurarTabla();
        configurarVista();
    }

    private void configurarFiltro() {

        filtroEstado.setLabel("Mostrar");
        filtroEstado.setItems("Todos", "Activos", "Inactivos");
        filtroEstado.setValue("Todos");
        filtroEstado.addValueChangeListener(
                evento -> cargarRoles()
        );
    }

    private void configurarTabla() {

        tablaRoles.addColumn(RolDTO::getCodigo)
                .setHeader("Código").setAutoWidth(true);

        tablaRoles.addColumn(RolDTO::getNombre)
                .setHeader("Nombre").setAutoWidth(true);

        tablaRoles.addColumn(RolDTO::getPrefijo)
                .setHeader("Prefijo").setAutoWidth(true);

        tablaRoles.addColumn(rol -> {
            Set<String> permisos = rol.getPermisos();
            return permisos == null || permisos.isEmpty()
                    ? "Sin permisos"
                    : String.join(", ", permisos);
        }).setHeader("Permisos")
                .setAutoWidth(true)
                .setFlexGrow(1);

        tablaRoles.addColumn(rol ->
                rol.getDescripcion() != null
                        ? rol.getDescripcion()
                        : "-"
        ).setHeader("Descripción").setAutoWidth(true);

        tablaRoles.addColumn(rol ->
                Boolean.TRUE.equals(rol.getActivo())
                        ? "Activo"
                        : "Inactivo"
        ).setHeader("Estado").setAutoWidth(true);

        tablaRoles.addComponentColumn(rol -> {

            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            boolean estaActivo = Boolean.TRUE.equals(rol.getActivo());

            Button botonModificar = new Button("Modificar");
            botonModificar.setEnabled(estaActivo);

            if (estaActivo) {
                botonModificar.addClickListener(
                        evento -> abrirFormulario(rol)
                );
            } else {
                botonModificar.setTooltipText(
                        "Debe reactivar el rol antes de modificarlo."
                );
            }

            Button botonEstado = new Button(
                    estaActivo ? "Dar de baja" : "Reactivar"
            );

            if (estaActivo) {
                botonEstado.addClickListener(
                        evento -> confirmarBaja(rol)
                );
            } else {
                botonEstado.addClickListener(
                        evento -> reactivarRol(rol)
                );
            }

            return new HorizontalLayout(
                    botonModificar,
                    botonEstado
            );

        }).setHeader("Acciones").setAutoWidth(true);

        tablaRoles.setSizeFull();
    }

    private void configurarVista() {

        H2 titulo = new H2("Administración de roles");

        Button botonNuevo = new Button(
                "Nuevo rol",
                evento -> abrirFormulario(null)
        );
        botonNuevo.setVisible(puedeEscribir());

        Button botonActualizar = new Button(
                "Actualizar",
                evento -> cargarRoles()
        );

        HorizontalLayout controles = new HorizontalLayout(
                filtroEstado,
                botonActualizar
        );
        controles.setAlignItems(HorizontalLayout.Alignment.END);

        setSizeFull();
        add(titulo, botonNuevo, controles, tablaRoles);
        expand(tablaRoles);
    }

    private void abrirFormulario(RolDTO rol) {

        boolean esCreacion = rol == null;

        Dialog dialogo = new Dialog();
        dialogo.setWidth("600px");
        dialogo.setHeaderTitle(
                esCreacion
                        ? "Nuevo rol"
                        : "Modificar " + rol.getCodigo()
        );

        TextField campoNombre = new TextField("Nombre");
        TextField campoPrefijo = new TextField("Prefijo");
        TextArea campoDescripcion = new TextArea("Descripción");

        MultiSelectComboBox<String> campoPermisos
                = new MultiSelectComboBox<>("Permisos");

        campoPermisos.setItems(PermisosApp.todos());
        campoPermisos.setRequiredIndicatorVisible(true);

        campoNombre.setRequiredIndicatorVisible(true);
        campoPrefijo.setRequiredIndicatorVisible(true);

        campoNombre.setWidthFull();
        campoPrefijo.setWidthFull();
        campoDescripcion.setWidthFull();
        campoPermisos.setWidthFull();

        campoPrefijo.setHelperText(
                "Se utiliza para generar códigos de usuarios del rol."
        );

        if (!esCreacion) {
            campoNombre.setValue(valorSeguro(rol.getNombre()));
            campoPrefijo.setValue(valorSeguro(rol.getPrefijo()));
            campoDescripcion.setValue(valorSeguro(rol.getDescripcion()));

            if (rol.getPermisos() != null) {
                campoPermisos.select(rol.getPermisos());
            }
        }

        Button botonGuardar = new Button(
                esCreacion ? "Guardar" : "Guardar cambios"
        );

        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        botonGuardar.addClickListener(evento -> {

            String nombre = campoNombre.getValue().trim();
            String prefijo = campoPrefijo.getValue().trim();
            Set<String> permisos = new HashSet<>(
                    campoPermisos.getSelectedItems()
            );

            boolean nombreVacio = nombre.isBlank();
            boolean prefijoVacio = prefijo.isBlank();
            boolean permisosVacios = permisos.isEmpty();

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage("El nombre es obligatorio.");

            campoPrefijo.setInvalid(prefijoVacio);
            campoPrefijo.setErrorMessage("El prefijo es obligatorio.");

            campoPermisos.setInvalid(permisosVacios);
            campoPermisos.setErrorMessage(
                    "Seleccione al menos un permiso."
            );

            if (nombreVacio || prefijoVacio || permisosVacios) {
                return;
            }

            try {

                if (existeNombreOPrefijoRepetido(
                        rol,
                        nombre,
                        prefijo
                )) {
                    return;
                }

                RolDTO datos = new RolDTO();
                datos.setNombre(nombre);
                datos.setPrefijo(prefijo);
                datos.setDescripcion(
                        campoDescripcion.getValue().trim()
                );
                datos.setPermisos(permisos);

                if (esCreacion) {
                    rolClient.crear(datos);
                } else {
                    rolClient.actualizar(rol.getCodigo(), datos);
                }

                dialogo.close();
                cargarRoles();

                Notification.show(
                        esCreacion
                                ? "Rol creado correctamente."
                                : "Rol modificado correctamente."
                );

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                Notification.show("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                Notification.show(excepcion.getMessage());
            }
        });

        dialogo.add(
                new VerticalLayout(
                        campoNombre,
                        campoPrefijo,
                        campoDescripcion,
                        campoPermisos,
                        new HorizontalLayout(
                                botonGuardar,
                                botonCancelar
                        )
                )
        );

        dialogo.open();
    }

    private boolean existeNombreOPrefijoRepetido(
            RolDTO rolActual,
            String nombre,
            String prefijo) {

        List<RolDTO> roles = rolClient.listar(null);

        for (RolDTO existente : roles) {

            boolean mismoRol = rolActual != null
                    && existente.getCodigo() != null
                    && existente.getCodigo().equalsIgnoreCase(
                            rolActual.getCodigo()
                    );

            if (mismoRol) {
                continue;
            }

            if (existente.getNombre() != null
                    && existente.getNombre().equalsIgnoreCase(nombre)) {

                Notification.show("Ya existe un rol con ese nombre.");
                return true;
            }

            if (existente.getPrefijo() != null
                    && existente.getPrefijo().equalsIgnoreCase(prefijo)) {

                Notification.show("Ya existe un rol con ese prefijo.");
                return true;
            }
        }

        return false;
    }

    private void confirmarBaja(RolDTO rol) {

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Dar de baja " + rol.getCodigo());

        Span mensaje = new Span(
                "El rol dejará de estar disponible para nuevas asignaciones."
        );

        Button botonConfirmar = new Button(
                "Confirmar baja",
                evento -> {
                    darDeBaja(rol);
                    dialogo.close();
                }
        );

        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        dialogo.add(
                new VerticalLayout(
                        mensaje,
                        new HorizontalLayout(
                                botonConfirmar,
                                botonCancelar
                        )
                )
        );
        dialogo.open();
    }

    private void darDeBaja(RolDTO rol) {

        try {
            rolClient.darDeBaja(rol.getCodigo());
            cargarRoles();
            Notification.show("Rol dado de baja correctamente.");
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            Notification.show("No se puede conectar con el backend.");
        }
    }

    private void reactivarRol(RolDTO rol) {

        try {
            rolClient.reactivar(rol.getCodigo());
            cargarRoles();
            Notification.show("Rol reactivado correctamente.");
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            Notification.show("No se puede conectar con el backend.");
        }
    }

    private Boolean obtenerEstadoSeleccionado() {

        return switch (filtroEstado.getValue()) {
            case "Activos" -> true;
            case "Inactivos" -> false;
            default -> null;
        };
    }

    private void cargarRoles() {

        try {
            List<RolDTO> roles = rolClient.listar(
                    obtenerEstadoSeleccionado()
            );

            tablaRoles.setItems(roles);

            if (roles.isEmpty()) {
                Notification.show(
                        "No existen roles con el estado seleccionado."
                );
            }
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            Notification.show("No se puede conectar con el backend.");
        } catch (IllegalStateException excepcion) {
            Notification.show("Debe iniciar sesión.");
        }
    }

    private void mostrarErrorOperacion(
            RestClientResponseException excepcion) {
        Notification.show(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    private String valorSeguro(String valor) {
        return valor != null ? valor : "";
    }

    private boolean puedeEscribir() {
        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarRoles();
    }
}
