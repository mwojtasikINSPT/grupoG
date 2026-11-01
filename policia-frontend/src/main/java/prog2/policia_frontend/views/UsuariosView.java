package prog2.policia_frontend.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import prog2.policia_frontend.utils.NotificacionUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.UsuarioClient;
import prog2.policia_frontend.clients.VigilanteClient;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.dtos.VigilanteDTO;
import prog2.policia_frontend.layouts.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.dialog.Dialog;
import prog2.policia_frontend.clients.RolClient;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.NumberField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prog2.policia_frontend.dtos.RolDTO;
import prog2.policia_frontend.dtos.UsuarioFormularioDTO;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;
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
    private final VigilanteClient vigilanteClient;
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
     * Conserva los datos específicos de los vigilantes, especialmente la
     * edad, mientras se muestran dentro de la tabla general de usuarios.
     */
    private final Map<String, VigilanteDTO> vigilantesPorCodigo
            = new HashMap<>();
    
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
        VigilanteClient vigilanteClient,
        RolClient rolClient,
        SesionUsuario sesionUsuario) {

    this.usuarioClient = usuarioClient;
    this.vigilanteClient = vigilanteClient;
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

            /*
             * El vigilante aparece en el listado general porque también es
             * un usuario, pero su edad se gestiona en la vista específica.
             */
            if (esVigilante(usuario)) {

                VigilanteDTO vigilante
                        = vigilantesPorCodigo.get(usuario.getCodigo());

                if (vigilante == null) {
                    return new Span("Datos no disponibles");
                }

                Button botonModificar
                        = new Button("Modificar");

                botonModificar.setEnabled(
                        Boolean.TRUE.equals(vigilante.getActivo())
                );

                botonModificar.addClickListener(evento ->
                        abrirFormularioEdicionVigilante(vigilante)
                );

                Button botonEstado = new Button();

                if (Boolean.TRUE.equals(vigilante.getActivo())) {

                    botonEstado.setText("Dar de baja");
                    botonEstado.addClickListener(evento ->
                            abrirDialogoBajaVigilante(vigilante)
                    );

                } else if ("FALLECIMIENTO".equals(
                        vigilante.getMotivoBaja())) {

                    botonEstado.setText("No reactivable");
                    botonEstado.setEnabled(false);
                    botonEstado.setTooltipText(
                            "El motivo de baja fue fallecimiento."
                    );

                } else {

                    botonEstado.setText("Reactivar");
                    botonEstado.addClickListener(evento ->
                            reactivarVigilante(vigilante)
                    );
                }

                return new HorizontalLayout(
                        botonModificar,
                        botonEstado
                );
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

        /*
         * La tabla solo recibe diez elementos por página. Ajustar su altura
         * a esas filas mantiene siempre visible el paginador inferior.
         */
        tablaUsuarios.setWidthFull();
        tablaUsuarios.setAllRowsVisible(true);
        tablaUsuarios.addThemeVariants(
                GridVariant.LUMO_COMPACT
        );
    }

/*
 * Abre el formulario con los datos actuales
 * del usuario que se desea modificar.
 */
private void abrirFormularioEdicion(
        UsuarioDTO usuario) {

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

    campoNombre.setRequiredIndicatorVisible(true);

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

        String nuevaPassword
                = campoPassword.getValue();

        boolean passwordInvalida
                = !nuevaPassword.isBlank()
                && (nuevaPassword.length() < 4
                || nuevaPassword.length() > 20);

        campoNombre.setInvalid(nombreVacio);
        campoNombre.setErrorMessage(
                "El nombre es obligatorio."
        );

        campoPassword.setInvalid(passwordInvalida);
        campoPassword.setErrorMessage(
                "La contraseña debe tener entre 4 y 20 caracteres."
        );

        if (nombreVacio || passwordInvalida) {
            return;
        }

        String passwordParaEnviar
                = nuevaPassword.isBlank()
                        ? null
                        : nuevaPassword;

        UsuarioFormularioDTO datos
                = new UsuarioFormularioDTO(
                        campoNombre.getValue().trim(),
                        passwordParaEnviar,
                        null
                );

        try {

            UsuarioDTO usuarioModificado = usuarioClient.actualizar(
                    usuario.getCodigo(),
                    datos
            );

            dialogo.close();
            cargarUsuarios();

            NotificacionUtils.mostrarExito(
                    "Usuario "
                    + usuarioModificado.getCodigo()
                    + " - "
                    + usuarioModificado.getNombre()
                    + " modificado correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            NotificacionUtils.mostrarInformacion(
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
        rolesActivos = rolClient.listar(true);

    } catch (RestClientResponseException excepcion) {

        mostrarErrorOperacion(excepcion);
        return;

    } catch (ResourceAccessException excepcion) {

        NotificacionUtils.mostrarInformacion(
                "No se puede consultar los roles."
        );
        return;
    }

    if (rolesActivos.isEmpty()) {

        NotificacionUtils.mostrarInformacion(
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

    NumberField campoEdad
            = new NumberField("Edad");

    campoEdad.setMin(18);
    campoEdad.setMax(65);
    campoEdad.setStep(1);
    campoEdad.setStepButtonsVisible(true);
    campoEdad.setRequiredIndicatorVisible(true);
    campoEdad.setVisible(false);

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

    /*
     * La edad solo corresponde cuando el rol elegido es VIGILANTE.
     */
    campoRol.addValueChangeListener(evento -> {
        boolean rolVigilante
                = esRolVigilante(evento.getValue());

        campoEdad.setVisible(rolVigilante);

        if (!rolVigilante) {
            campoEdad.clear();
            campoEdad.setInvalid(false);
        }
    });

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

        boolean rolVigilante
                = esRolVigilante(campoRol.getValue());

        Double edadIngresada = campoEdad.getValue();

        boolean edadInvalida
                = rolVigilante
                && (edadIngresada == null
                || edadIngresada < 18
                || edadIngresada > 65
                || edadIngresada != Math.floor(edadIngresada));

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

        campoEdad.setInvalid(edadInvalida);
        campoEdad.setErrorMessage(
                "La edad debe ser un número entero entre 18 y 65."
        );

        if (nombreVacio
                || passwordVacio
                || rolVacio
                || edadInvalida) {

            return;
        }

        RolDTO rolSeleccionado
                = campoRol.getValue();

        try {

            if (rolVigilante) {

                VigilanteDTO datosVigilante
                        = new VigilanteDTO(
                                campoNombre.getValue().trim(),
                                campoPassword.getValue(),
                                edadIngresada.intValue()
                        );

                vigilanteClient.crear(datosVigilante);

            } else {

                UsuarioFormularioDTO datosUsuario
                        = new UsuarioFormularioDTO(
                                campoNombre.getValue().trim(),
                                campoPassword.getValue(),
                                rolSeleccionado.getNombre()
                        );

                usuarioClient.crear(datosUsuario);
            }

            dialogo.close();
            cargarUsuarios();

            NotificacionUtils.mostrarExito(
                    "Usuario creado correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            NotificacionUtils.mostrarInformacion(
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
                    campoEdad,
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

            NotificacionUtils.mostrarExito(
                    "Usuario dado de baja correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            NotificacionUtils.mostrarInformacion(
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

        NotificacionUtils.mostrarExito(
                "Usuario reactivado correctamente."
        );

    } catch (RestClientResponseException excepcion) {

        mostrarErrorOperacion(excepcion);

    } catch (ResourceAccessException excepcion) {

        NotificacionUtils.mostrarInformacion(
                "No se puede conectar con el backend."
        );

    } catch (IllegalArgumentException excepcion) {

        NotificacionUtils.mostrarInformacion(
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
    NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
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

        List<UsuarioDTO> todosLosUsuarios
                = new ArrayList<>();

        todosLosUsuarios.addAll(
                usuarioClient.listar(
                        estadoSeleccionado,
                        nombreIngresado
                )
        );

        List<VigilanteDTO> vigilantes
                = vigilanteClient.listar(
                        estadoSeleccionado,
                        nombreIngresado
                );

        vigilantesPorCodigo.clear();

        for (VigilanteDTO vigilante : vigilantes) {

            if (vigilante.getCodigo() != null) {
                vigilantesPorCodigo.put(
                        vigilante.getCodigo(),
                        vigilante
                );
            }

            todosLosUsuarios.add(
                    convertirVigilanteAUsuario(vigilante)
            );
        }

        todosLosUsuarios.sort(
                Comparator.comparing(
                        UsuarioDTO::getCodigo,
                        Comparator.nullsLast(
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
        );

        paginadorUsuarios.setItems(todosLosUsuarios);

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalStateException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "Debe iniciar sesión."
            );
        }
    }

    private boolean puedeEscribir() {
        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    private UsuarioDTO convertirVigilanteAUsuario(
            VigilanteDTO vigilante) {

        UsuarioDTO usuario = new UsuarioDTO();

        usuario.setId(vigilante.getId());
        usuario.setCodigo(vigilante.getCodigo());
        usuario.setNombre(vigilante.getNombre());
        usuario.setRol(vigilante.getRol());
        usuario.setMotivoBaja(vigilante.getMotivoBaja());
        usuario.setActivo(vigilante.getActivo());

        return usuario;
    }

    private boolean esVigilante(UsuarioDTO usuario) {
        return usuario.getRol() != null
                && usuario.getRol().equalsIgnoreCase("VIGILANTE");
    }

    private boolean esRolVigilante(RolDTO rol) {
        return rol != null
                && rol.getNombre() != null
                && rol.getNombre().equalsIgnoreCase("VIGILANTE");
    }

    private void abrirFormularioEdicionVigilante(
            VigilanteDTO vigilante) {

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(
                "Modificar " + vigilante.getCodigo()
        );

        TextField campoNombre = new TextField("Nombre");
        NumberField campoEdad = new NumberField("Edad");
        PasswordField campoPassword
                = new PasswordField("Nueva contraseña");

        campoNombre.setValue(
                vigilante.getNombre() != null
                        ? vigilante.getNombre()
                        : ""
        );

        if (vigilante.getEdad() != null) {
            campoEdad.setValue(
                    vigilante.getEdad().doubleValue()
            );
        }

        campoEdad.setMin(18);
        campoEdad.setMax(65);
        campoEdad.setStep(1);
        campoEdad.setStepButtonsVisible(true);
        campoPassword.setPlaceholder(
                "Dejar vacío para conservar la actual"
        );
        campoPassword.setRevealButtonVisible(false);

        campoNombre.setRequiredIndicatorVisible(true);
        campoEdad.setRequiredIndicatorVisible(true);
        campoNombre.setWidthFull();
        campoEdad.setWidthFull();
        campoPassword.setWidthFull();

        Button botonGuardar = new Button("Guardar cambios");
        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        botonGuardar.addClickListener(evento -> {

            Double edadIngresada = campoEdad.getValue();
            String nuevaPassword = campoPassword.getValue();

            boolean nombreVacio
                    = campoNombre.getValue().isBlank();
            boolean edadInvalida
                    = edadIngresada == null
                    || edadIngresada < 18
                    || edadIngresada > 65
                    || edadIngresada != Math.floor(edadIngresada);
            boolean passwordInvalida
                    = !nuevaPassword.isBlank()
                    && (nuevaPassword.length() < 4
                    || nuevaPassword.length() > 20);

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage(
                    "El nombre es obligatorio."
            );
            campoEdad.setInvalid(edadInvalida);
            campoEdad.setErrorMessage(
                    "La edad debe ser un número entero entre 18 y 65."
            );
            campoPassword.setInvalid(passwordInvalida);
            campoPassword.setErrorMessage(
                    "La contraseña debe tener entre 4 y 20 caracteres."
            );

            if (nombreVacio || edadInvalida || passwordInvalida) {
                return;
            }

            VigilanteDTO datos = new VigilanteDTO(
                    campoNombre.getValue().trim(),
                    nuevaPassword.isBlank() ? null : nuevaPassword,
                    edadIngresada.intValue()
            );

            try {
                VigilanteDTO vigilanteModificado = vigilanteClient.actualizar(
                        vigilante.getCodigo(),
                        datos
                );
                dialogo.close();
                cargarUsuarios();
                NotificacionUtils.mostrarExito(
                        "Vigilante "
                        + vigilanteModificado.getCodigo()
                        + " - "
                        + vigilanteModificado.getNombre()
                        + " modificado correctamente."
                );
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion(
                        "No se puede conectar con el backend."
                );
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(new VerticalLayout(
                campoNombre,
                campoEdad,
                campoPassword,
                new HorizontalLayout(
                        botonGuardar,
                        botonCancelar
                )
        ));
        dialogo.open();
    }

    private void abrirDialogoBajaVigilante(
            VigilanteDTO vigilante) {

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(
                "Dar de baja " + vigilante.getCodigo()
        );

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

        Button botonConfirmar = new Button("Confirmar baja");
        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        botonConfirmar.addClickListener(evento -> {

            String motivo = campoMotivo.getValue();
            boolean motivoVacio
                    = motivo == null || motivo.isBlank();

            campoMotivo.setInvalid(motivoVacio);
            campoMotivo.setErrorMessage(
                    "Debe seleccionar un motivo."
            );

            if (motivoVacio) {
                return;
            }

            try {
                vigilanteClient.darDeBaja(
                        vigilante.getCodigo(),
                        motivo
                );
                dialogo.close();
                cargarUsuarios();
                NotificacionUtils.mostrarExito(
                        "Vigilante dado de baja correctamente."
                );
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion(
                        "No se puede conectar con el backend."
                );
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(
                campoMotivo,
                new HorizontalLayout(
                        botonConfirmar,
                        botonCancelar
                )
        );
        dialogo.open();
    }

    private void reactivarVigilante(
            VigilanteDTO vigilante) {

        try {
            vigilanteClient.reactivar(
                    vigilante.getCodigo()
            );
            cargarUsuarios();
            NotificacionUtils.mostrarExito(
                    "Vigilante reactivado correctamente."
            );
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );
        } catch (IllegalArgumentException excepcion) {
            NotificacionUtils.mostrarInformacion(excepcion.getMessage());
        }
    }
}
