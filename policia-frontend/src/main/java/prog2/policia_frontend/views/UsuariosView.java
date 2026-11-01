package prog2.policia_frontend.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.UsuarioClient;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.layouts.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import prog2.policia_frontend.clients.RolClient;
import com.vaadin.flow.component.textfield.PasswordField;
import java.util.List;
import prog2.policia_frontend.dtos.RolDTO;
import prog2.policia_frontend.dtos.UsuarioFormularioDTO;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.components.PaginadorGrid;

/*
 * Pantalla utilizada para consultar
 * los usuarios genéricos registrados.
 */
@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Usuarios | Sistema Policial")
public class UsuariosView extends VerticalLayout
        implements BeforeEnterObserver {

    private final UsuarioClient usuarioClient;
    private final SesionUsuario sesionUsuario;
/*
 * Permite consultar los roles disponibles
 * para crear o modificar usuarios.
 */
private final RolClient rolClient;
    /*
     * Tabla que mostrará los usuarios.
     *
     * false evita que Vaadin genere
     * columnas automáticamente.
     */
    private final Grid<UsuarioDTO> tablaUsuarios
            = new Grid<>(UsuarioDTO.class, false);
    private final PaginadorGrid<UsuarioDTO> paginadorUsuarios
            = new PaginadorGrid<>(tablaUsuarios, 10);
    
    /*
 * Permite buscar coincidencias en el nombre.
 */
private final TextField filtroNombre
        = new TextField("Buscar por nombre");

/*
 * Permite mostrar todos, activos o inactivos.
 */
private final Select<String> filtroEstado
        = new Select<>();

private final Button botonBuscar
        = new Button("Buscar");

private final Button botonNuevoUsuario
        = new Button("Nuevo usuario");

private final Button botonLimpiar
        = new Button("Limpiar");

  public UsuariosView(
        UsuarioClient usuarioClient,
        RolClient rolClient,
        SesionUsuario sesionUsuario) {

    this.usuarioClient = usuarioClient;
    this.rolClient = rolClient;
    this.sesionUsuario = sesionUsuario;

    configurarFiltros();
    configurarVista();
    configurarTabla();
}
    
    /*
 * Configura los campos utilizados
 * para filtrar la tabla.
 */
private void configurarFiltros() {

    filtroNombre.setPlaceholder(
            "Ejemplo: Laura"
    );

    filtroNombre.setClearButtonVisible(true);

    filtroEstado.setLabel("Estado");
    filtroEstado.setItems(
            "Todos",
            "Activos",
            "Inactivos"
    );
    filtroEstado.setValue("Todos");

    /*
     * Ejecuta la búsqueda utilizando
     * los valores escritos por el usuario.
     */
    botonBuscar.addClickListener(
            evento -> cargarUsuarios()
    );

    /*
     * Borra los filtros y vuelve a cargar
     * todos los usuarios.
     */
    botonLimpiar.addClickListener(evento -> {

        filtroNombre.clear();
        filtroEstado.setValue("Todos");

        cargarUsuarios();
    });
}
    
    
    
    

    /*
 * Organiza los componentes de la pantalla.
 */
private void configurarVista() {

    H2 titulo = new H2(
            "Administración de usuarios"
    );

    /*
     * Coloca los filtros y botones
     * en una misma fila.
     */
    HorizontalLayout barraFiltros
            = new HorizontalLayout(
                    filtroNombre,
                    filtroEstado,
                    botonBuscar,
                    botonLimpiar
            );

    /*
     * Alinea los botones con la parte inferior
     * de los campos de búsqueda.
     */
    barraFiltros.setAlignItems(
            HorizontalLayout.Alignment.END
    );

    setSizeFull();
    botonNuevoUsuario.addClickListener(
        evento -> abrirFormularioNuevo()
);
    botonNuevoUsuario.setVisible(puedeEscribir());

add(
        titulo,
        botonNuevoUsuario,
        barraFiltros,
        tablaUsuarios,
        paginadorUsuarios.getControles()
);
    /*
     * La tabla utiliza el espacio
     * disponible de la pantalla.
     */
    expand(tablaUsuarios);
}
    private void configurarTabla() {

        tablaUsuarios.addColumn(
                UsuarioDTO::getCodigo
        ).setHeader("Código")
                .setAutoWidth(true);

        tablaUsuarios.addColumn(
                UsuarioDTO::getNombre
        ).setHeader("Nombre")
                .setAutoWidth(true);

        tablaUsuarios.addColumn(
                UsuarioDTO::getRol
        ).setHeader("Rol")
                .setAutoWidth(true);

        tablaUsuarios.addColumn(usuario ->
                Boolean.TRUE.equals(
                        usuario.getActivo()
                )
                        ? "Activo"
                        : "Inactivo"
        ).setHeader("Estado")
                .setAutoWidth(true);

        /*
         * Muestra las acciones disponibles
         * para cada usuario.
         */
        tablaUsuarios.addComponentColumn(usuario -> {

            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            Button botonModificar
                    = new Button("Modificar");

            botonModificar.addClickListener(
                    evento -> abrirFormularioEdicion(usuario)
            );

            /*
             * Un usuario inactivo debe reactivarse
             * antes de modificar sus datos.
             */
            botonModificar.setEnabled(
                    Boolean.TRUE.equals(usuario.getActivo())
            );

            if (!Boolean.TRUE.equals(usuario.getActivo())) {

                botonModificar.setTooltipText(
                        "Debe reactivar el usuario antes de modificarlo."
                );
            }

            Button botonEstado = new Button();

            if (Boolean.TRUE.equals(usuario.getActivo())) {

                botonEstado.setText("Dar de baja");

                botonEstado.addClickListener(
                        evento -> abrirDialogoBaja(usuario)
                );

            } else {

                botonEstado.setText("Reactivar");

                botonEstado.addClickListener(
                        evento -> reactivarUsuario(usuario)
                );
            }

            return new HorizontalLayout(
                    botonModificar,
                    botonEstado
            );

        }).setHeader("Acciones")
                .setAutoWidth(true);

        tablaUsuarios.setSizeFull();
    }

/*
 * Abre el formulario con los datos actuales
 * del usuario que se desea modificar.
 */
private void abrirFormularioEdicion(
        UsuarioDTO usuario) {

    List<RolDTO> rolesActivos;

    try {

        rolesActivos = obtenerRolesAsignables();

    } catch (RestClientResponseException excepcion) {

        mostrarErrorOperacion(excepcion);
        return;

    } catch (ResourceAccessException excepcion) {

        Notification.show(
                "No se puede consultar los roles."
        );
        return;
    }

    Dialog dialogo = new Dialog();

    dialogo.setHeaderTitle(
            "Modificar usuario "
                    + usuario.getCodigo()
    );

    TextField campoNombre
            = new TextField("Nombre");

    campoNombre.setValue(
            usuario.getNombre() != null
                    ? usuario.getNombre()
                    : ""
    );

    PasswordField campoPassword
            = new PasswordField(
                    "Nueva contraseña"
            );

    campoPassword.setHelperText(
            "Dejar vacío para conservar la contraseña actual."
    );

    Select<RolDTO> campoRol
            = new Select<>();

    campoRol.setLabel("Rol");
    campoRol.setItemLabelGenerator(
            RolDTO::getNombre
    );
    campoRol.setItems(rolesActivos);

    /*
     * Busca y selecciona el rol que actualmente
     * posee el usuario.
     */
    RolDTO rolActual = rolesActivos.stream()
            .filter(rol
                    -> rol.getNombre() != null
                    && rol.getNombre().equalsIgnoreCase(
                            usuario.getRol()
                    )
            )
            .findFirst()
            .orElse(null);

    if (rolActual != null) {
        campoRol.setValue(rolActual);
    }

    campoNombre.setRequiredIndicatorVisible(true);
    campoRol.setRequiredIndicatorVisible(true);

    Button botonGuardar
            = new Button("Guardar cambios");

    Button botonCancelar
            = new Button(
                    "Cancelar",
                    evento -> dialogo.close()
            );

    botonGuardar.addClickListener(evento -> {

        boolean nombreVacio
                = campoNombre.getValue().isBlank();

        boolean rolVacio
                = campoRol.getValue() == null;

        campoNombre.setInvalid(nombreVacio);
        campoNombre.setErrorMessage(
                "El nombre es obligatorio."
        );

        campoRol.setInvalid(rolVacio);
        campoRol.setErrorMessage(
                "Debe seleccionar un rol activo."
        );

        if (nombreVacio || rolVacio) {
            return;
        }

        UsuarioFormularioDTO datos
                = new UsuarioFormularioDTO(
                        campoNombre.getValue().trim(),
                        campoPassword.getValue(),
                        campoRol.getValue().getNombre()
                );

        try {

            usuarioClient.actualizar(
                    usuario.getCodigo(),
                    datos
            );

            dialogo.close();
            cargarUsuarios();

            Notification.show(
                    "Usuario modificado correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            Notification.show(
                    excepcion.getMessage()
            );
        }
    });

    HorizontalLayout botones
            = new HorizontalLayout(
                    botonGuardar,
                    botonCancelar
            );

    VerticalLayout formulario
            = new VerticalLayout(
                    campoNombre,
                    campoPassword,
                    campoRol,
                    botones
            );

    dialogo.add(formulario);
    dialogo.open();
}

    /*
 * Abre el formulario para crear
 * un usuario con uno de los roles activos.
 */
private void abrirFormularioNuevo() {

    List<RolDTO> rolesActivos;

    try {

        /*
         * Consulta solamente los roles activos,
         * porque no corresponde asignar un rol inactivo.
         */
        rolesActivos = obtenerRolesAsignables();

    } catch (RestClientResponseException excepcion) {

        mostrarErrorOperacion(excepcion);
        return;

    } catch (ResourceAccessException excepcion) {

        Notification.show(
                "No se puede consultar los roles."
        );
        return;
    }

    if (rolesActivos.isEmpty()) {

        Notification.show(
                "No existen roles activos disponibles."
        );
        return;
    }

    Dialog dialogo = new Dialog();

    dialogo.setHeaderTitle(
            "Crear nuevo usuario"
    );

    TextField campoNombre
            = new TextField("Nombre");

    PasswordField campoPassword
            = new PasswordField("Contraseña");

    Select<RolDTO> campoRol
            = new Select<>();

    campoRol.setLabel("Rol");

    /*
     * Muestra el nombre del rol en lugar
     * de mostrar el objeto completo.
     */
    campoRol.setItemLabelGenerator(
            RolDTO::getNombre
    );

    campoRol.setItems(rolesActivos);

    campoNombre.setRequiredIndicatorVisible(true);
    campoPassword.setRequiredIndicatorVisible(true);
    campoRol.setRequiredIndicatorVisible(true);

    Button botonGuardar
            = new Button("Guardar");

    Button botonCancelar
            = new Button(
                    "Cancelar",
                    evento -> dialogo.close()
            );

    botonGuardar.addClickListener(evento -> {

        boolean nombreVacio
                = campoNombre.getValue().isBlank();

        boolean passwordVacio
                = campoPassword.getValue().isBlank();

        boolean rolVacio
                = campoRol.getValue() == null;

        campoNombre.setInvalid(nombreVacio);
        campoNombre.setErrorMessage(
                "El nombre es obligatorio."
        );

        campoPassword.setInvalid(passwordVacio);
        campoPassword.setErrorMessage(
                "La contraseña es obligatoria."
        );

        campoRol.setInvalid(rolVacio);
        campoRol.setErrorMessage(
                "Debe seleccionar un rol."
        );

        if (nombreVacio
                || passwordVacio
                || rolVacio) {

            return;
        }

        RolDTO rolSeleccionado
                = campoRol.getValue();

        UsuarioFormularioDTO datos
                = new UsuarioFormularioDTO(
                        campoNombre.getValue().trim(),
                        campoPassword.getValue(),
                        rolSeleccionado.getNombre()
                );

        try {

            usuarioClient.crear(datos);

            dialogo.close();
            cargarUsuarios();

            Notification.show(
                    "Usuario creado correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            Notification.show(
                    excepcion.getMessage()
            );
        }
    });

    HorizontalLayout botones
            = new HorizontalLayout(
                    botonGuardar,
                    botonCancelar
            );

    VerticalLayout formulario
            = new VerticalLayout(
                    campoNombre,
                    campoPassword,
                    campoRol,
                    botones
            );

    dialogo.add(formulario);
    dialogo.open();
}
    
    
    
private void abrirDialogoBaja(
        UsuarioDTO usuario) {

    Dialog dialogo = new Dialog();

    dialogo.setHeaderTitle(
            "Dar de baja a "
                    + usuario.getNombre()
    );

    Select<String> campoMotivo
            = new Select<>();

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

    Button botonConfirmar
            = new Button("Confirmar baja");

    Button botonCancelar
            = new Button(
                    "Cancelar",
                    evento -> dialogo.close()
            );

    botonConfirmar.addClickListener(evento -> {

        String motivoSeleccionado
                = campoMotivo.getValue();

        if (motivoSeleccionado == null) {

            campoMotivo.setInvalid(true);
            campoMotivo.setErrorMessage(
                    "Debe seleccionar un motivo."
            );

            return;
        }

        try {

            usuarioClient.darDeBaja(
                    usuario.getCodigo(),
                    motivoSeleccionado
            );

            dialogo.close();
            cargarUsuarios();

            Notification.show(
                    "Usuario dado de baja correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            Notification.show(
                    excepcion.getMessage()
            );
        }
    });

    HorizontalLayout botones
            = new HorizontalLayout(
                    botonConfirmar,
                    botonCancelar
            );

    dialogo.add(
            campoMotivo,
            botones
    );

    dialogo.open();
}

/*
 * Reactiva un usuario que estaba inactivo.
 */
private void reactivarUsuario(
        UsuarioDTO usuario) {

    try {

        usuarioClient.reactivar(
                usuario.getCodigo()
        );

        cargarUsuarios();

        Notification.show(
                "Usuario reactivado correctamente."
        );

    } catch (RestClientResponseException excepcion) {

        mostrarErrorOperacion(excepcion);

    } catch (ResourceAccessException excepcion) {

        Notification.show(
                "No se puede conectar con el backend."
        );

    } catch (IllegalArgumentException excepcion) {

        Notification.show(
                excepcion.getMessage()
        );
    }
}

/*
 * Convierte los errores HTTP del backend
 * en mensajes comprensibles.
 */
private void mostrarErrorOperacion(
        RestClientResponseException excepcion) {

    int estado
            = excepcion.getStatusCode().value();

    if (estado == 400) {

        Notification.show(
                "Los datos enviados no son válidos."
        );

    } else if (estado == 401) {

        Notification.show(
                "La sesión no es válida."
        );

    } else if (estado == 403) {

        Notification.show(
                "No tiene permiso para realizar esta acción."
        );

    } else if (estado == 404) {

        Notification.show(
                "El usuario ya no existe."
        );

    } else if (estado == 409) {

        Notification.show(
                "La operación no puede realizarse en el estado actual."
        );

    } else {

        Notification.show(
                "No se pudo completar la operación."
        );
    }
}

    /**
     * Devuelve los roles que el backend permite asignar a un usuario
     * genérico. Los vigilantes se crean desde su módulo porque requieren edad.
     */
    private List<RolDTO> obtenerRolesAsignables() {

        return rolClient.listar(true).stream()
                .filter(rol -> rol.getNombre() == null
                || !rol.getNombre().equalsIgnoreCase("VIGILANTE"))
                .toList();
    }

    /*
     * Cuando se abre la pantalla,
     * solicita los usuarios al backend.
     */
    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarUsuarios();
    }
    
    /*
 * Convierte el estado elegido en el valor
 * que espera recibir el backend.
 */
private Boolean obtenerEstadoSeleccionado() {

    return switch (filtroEstado.getValue()) {

        case "Activos" -> true;
        case "Inactivos" -> false;
        default -> null;
    };
}
    

    /*
     * Consulta los usuarios y los coloca
     * dentro de la tabla.
     */
    private void cargarUsuarios() {

        try {

                    Boolean estadoSeleccionado
                = obtenerEstadoSeleccionado();

        String nombreIngresado
                = filtroNombre.getValue();

paginadorUsuarios.setItems(
        usuarioClient.listar(
                estadoSeleccionado,
                nombreIngresado
        )
);

        } catch (RestClientResponseException excepcion) {

            int estado
                    = excepcion.getStatusCode().value();

            if (estado == 401) {
                Notification.show(
                        "La sesión no es válida."
                );
            } else if (estado == 403) {
                Notification.show(
                        "No tiene permiso para consultar usuarios."
                );
            } else {
                Notification.show(
                        "No se pudieron consultar los usuarios."
                );
            }

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalStateException excepcion) {

            Notification.show(
                    "Debe iniciar sesión."
            );
        }
    }

    private boolean puedeEscribir() {
        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }
}
