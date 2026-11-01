package prog2.policia_frontend.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import prog2.policia_frontend.utils.NotificacionUtils;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.VigilanteClient;
import prog2.policia_frontend.dtos.VigilanteDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;

/*
 * Pantalla utilizada para administrar vigilantes.
 * Por ahora se accede escribiendo /vigilantes.
 */
@Route(
        value = "vigilantes",
        layout = MainLayout.class
)
@PageTitle("Vigilantes | Sistema Policial")
public class VigilantesView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final VigilanteClient vigilanteClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<VigilanteDTO> tabla
            = new Grid<>(VigilanteDTO.class, false);

    private final TextField filtroNombre
            = new TextField("Buscar por nombre");

    private final Select<String> filtroEstado
            = new Select<>();
    /*
 * Abre el formulario para registrar
 * un vigilante nuevo.
 */
private final Button botonNuevoVigilante
        = new Button("Nuevo vigilante");

    public VigilantesView(
            VigilanteClient vigilanteClient,
            SesionUsuario sesionUsuario) {

        this.vigilanteClient = vigilanteClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltros();
        configurarTabla();
        configurarVista();
    }

    /*
     * Configura la búsqueda por nombre y estado.
     */
    private void configurarFiltros() {

        filtroNombre.setPlaceholder(
                "Ingrese una coincidencia"
        );

        filtroNombre.setClearButtonVisible(true);

        filtroEstado.setLabel("Estado");
        filtroEstado.setItems(
                "Todos",
                "Activos",
                "Inactivos"
        );
        filtroEstado.setValue("Todos");
    }

    /*
     * Define las columnas visibles.
     * La contraseña nunca se muestra.
     */
    private void configurarTabla() {

        tabla.addColumn(
                VigilanteDTO::getCodigo
        ).setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(
                VigilanteDTO::getNombre
        ).setHeader("Nombre")
                .setAutoWidth(true)
                .setFlexGrow(1);

        tabla.addColumn(
                VigilanteDTO::getEdad
        ).setHeader("Edad")
                .setAutoWidth(true);

        tabla.addColumn(
                VigilanteDTO::getRol
        ).setHeader("Rol")
                .setAutoWidth(true);

        tabla.addColumn(vigilante ->
                Boolean.TRUE.equals(
                        vigilante.getActivo()
                )
                        ? "Activo"
                        : "Inactivo"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addColumn(vigilante ->
                vigilante.getMotivoBaja() != null
                        ? vigilante.getMotivoBaja()
                        : "-"
        ).setHeader("Motivo de baja")
                .setAutoWidth(true);

        /*
         * Muestra las acciones disponibles
         * según el estado del vigilante.
         */
        tabla.addComponentColumn(vigilante -> {

            /*
             * Un usuario con permiso de lectura puede ver
             * los datos, pero no recibe botones que los cambien.
             */
            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            boolean estaActivo
                    = Boolean.TRUE.equals(
                            vigilante.getActivo()
                    );

            Button botonModificar
                    = new Button("Modificar");

            botonModificar.setEnabled(estaActivo);

            if (estaActivo) {

                botonModificar.addClickListener(
                        evento
                                -> abrirFormularioEdicion(vigilante)
                );

            } else {

                botonModificar.setTooltipText(
                        "Debe reactivar al vigilante antes de modificarlo."
                );
            }

            Button botonEstado = new Button();

            if (estaActivo) {

                botonEstado.setText("Dar de baja");

                botonEstado.addClickListener(
                        evento
                                -> abrirDialogoBaja(vigilante)
                );

            } else if ("FALLECIMIENTO".equals(
                    vigilante.getMotivoBaja()
            )) {

                /*
                 * Una persona dada de baja por
                 * fallecimiento no puede reactivarse.
                 */
                botonEstado.setText("No reactivable");
                botonEstado.setEnabled(false);

                botonEstado.setTooltipText(
                        "El motivo de baja fue fallecimiento."
                );

            } else {

                botonEstado.setText("Reactivar");

                botonEstado.addClickListener(
                        evento
                                -> reactivarVigilante(vigilante)
                );
            }

            return new HorizontalLayout(
                    botonModificar,
                    botonEstado
            );

        }).setHeader("Acciones")
                .setAutoWidth(true);
        
        tabla.setSizeFull();
    }

    /*
     * Organiza los elementos de la pantalla.
     */
    private void configurarVista() {

        H2 titulo = new H2(
                "Administración de vigilantes"
        );

        com.vaadin.flow.component.button.Button botonBuscar
                = new com.vaadin.flow.component.button.Button(
                        "Buscar",
                        evento -> cargarVigilantes()
                );

        com.vaadin.flow.component.button.Button botonLimpiar
                = new com.vaadin.flow.component.button.Button(
                        "Limpiar",
                        evento -> {

                            filtroNombre.clear();
                            filtroEstado.setValue("Todos");
                            cargarVigilantes();
                        }
                );

        HorizontalLayout filtros
                = new HorizontalLayout(
                        filtroNombre,
                        filtroEstado,
                        botonBuscar,
                        botonLimpiar
                );

        filtros.setAlignItems(
                HorizontalLayout.Alignment.END
        );
        
botonNuevoVigilante.addClickListener(
        evento -> abrirFormularioNuevo()
);

        /*
         * El alta solamente se ofrece a quienes poseen
         * el permiso de escritura.
         */
        botonNuevoVigilante.setVisible(
                puedeEscribir()
        );
        setSizeFull();

        add(
                
        titulo,
        botonNuevoVigilante,
        filtros,
        tabla
);
        
        expand(tabla);
    }
    
    
    /*
     * Reactiva un vigilante inactivo,
     * siempre que el backend lo permita.
     */
    private void reactivarVigilante(
            VigilanteDTO vigilante) {

        try {

            vigilanteClient.reactivar(
                    vigilante.getCodigo()
            );

            cargarVigilantes();

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

            NotificacionUtils.mostrarInformacion(
                    excepcion.getMessage()
            );
        }
    }

    /*
     * Solicita el motivo antes de dar
     * de baja lógica a un vigilante.
     */
    private void abrirDialogoBaja(
            VigilanteDTO vigilante) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Dar de baja "
                        + vigilante.getCodigo()
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
        campoMotivo.setWidthFull();

        Button botonConfirmar
                = new Button("Confirmar baja");

        Button botonCancelar
                = new Button(
                        "Cancelar",
                        evento -> dialogo.close()
                );

        botonConfirmar.addClickListener(evento -> {

            String motivo
                    = campoMotivo.getValue();

            boolean motivoVacio
                    = motivo == null
                    || motivo.isBlank();

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
                cargarVigilantes();

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

        VerticalLayout contenido
                = new VerticalLayout(
                        campoMotivo,
                        botones
                );

        dialogo.add(contenido);
        dialogo.open();
    }

    /*
     * Abre un formulario con los datos actuales
     * de un vigilante activo.
     */
    private void abrirFormularioEdicion(
            VigilanteDTO vigilante) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Modificar "
                        + vigilante.getCodigo()
        );

        TextField campoNombre
                = new TextField("Nombre");

        NumberField campoEdad
                = new NumberField("Edad");

        PasswordField campoPassword
                = new PasswordField(
                        "Nueva contraseña"
                );

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

        campoPassword.setPlaceholder(
                "Dejar vacío para conservar la actual"
        );

        campoPassword.setRevealButtonVisible(false);

        campoNombre.setRequiredIndicatorVisible(true);
        campoEdad.setRequiredIndicatorVisible(true);

        campoEdad.setMin(18);
        campoEdad.setMax(65);
        campoEdad.setStep(1);
        campoEdad.setStepButtonsVisible(true);

        campoNombre.setWidthFull();
        campoEdad.setWidthFull();
        campoPassword.setWidthFull();

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

            Double edadIngresada
                    = campoEdad.getValue();

            boolean edadInvalida
                    = edadIngresada == null
                    || edadIngresada < 18
                    || edadIngresada > 65
                    || edadIngresada
                            != Math.floor(edadIngresada);

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

            campoEdad.setInvalid(edadInvalida);
            campoEdad.setErrorMessage(
                    "La edad debe ser un número entero entre 18 y 65."
            );

            campoPassword.setInvalid(passwordInvalida);
            campoPassword.setErrorMessage(
                    "La contraseña debe tener entre 4 y 20 caracteres."
            );

            if (nombreVacio
                    || edadInvalida
                    || passwordInvalida) {

                return;
            }

            /*
             * Si no se escribió una contraseña nueva,
             * se envía null para conservar la actual.
             */
            String passwordParaEnviar
                    = nuevaPassword.isBlank()
                            ? null
                            : nuevaPassword;

            VigilanteDTO datos
                    = new VigilanteDTO(
                            campoNombre.getValue().trim(),
                            passwordParaEnviar,
                            edadIngresada.intValue()
                    );

            try {

                vigilanteClient.actualizar(
                        vigilante.getCodigo(),
                        datos
                );

                dialogo.close();
                cargarVigilantes();

                NotificacionUtils.mostrarExito(
                        "Vigilante modificado correctamente."
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
                        campoEdad,
                        campoPassword,
                        botones
                );

        dialogo.add(formulario);
        dialogo.open();
    }

    /*
     * Abre el formulario para registrar
     * un vigilante nuevo.
     */
private void abrirFormularioNuevo() {

    Dialog dialogo = new Dialog();

    dialogo.setHeaderTitle(
            "Nuevo vigilante"
    );

    TextField campoNombre
            = new TextField("Nombre");

    NumberField campoEdad
            = new NumberField("Edad");

    PasswordField campoPassword
            = new PasswordField("Contraseña");

    campoNombre.setRequiredIndicatorVisible(true);
    campoEdad.setRequiredIndicatorVisible(true);
    campoPassword.setRequiredIndicatorVisible(true);

    /*
     * La edad debe ser un número entero
     * comprendido entre 18 y 65.
     */
    campoEdad.setMin(18);
    campoEdad.setMax(65);
    campoEdad.setStep(1);
    campoEdad.setStepButtonsVisible(true);

    /*
     * Evita mostrar la contraseña mientras
     * el administrativo la escribe.
     */
    campoPassword.setRevealButtonVisible(false);

    campoNombre.setWidthFull();
    campoEdad.setWidthFull();
    campoPassword.setWidthFull();

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

        Double edadIngresada
                = campoEdad.getValue();

        boolean edadInvalida
                = edadIngresada == null
                || edadIngresada < 18
                || edadIngresada > 65
                || edadIngresada
                        != Math.floor(edadIngresada);

        String password
                = campoPassword.getValue();

        boolean passwordInvalida
                = password == null
                || password.length() < 4
                || password.length() > 20;

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

        /*
         * Si algún dato no es válido,
         * no se envía el formulario.
         */
        if (nombreVacio
                || edadInvalida
                || passwordInvalida) {

            return;
        }

        VigilanteDTO datos
                = new VigilanteDTO(
                        campoNombre.getValue().trim(),
                        password,
                        edadIngresada.intValue()
                );

        try {

            vigilanteClient.crear(datos);

            dialogo.close();
            cargarVigilantes();

            NotificacionUtils.mostrarExito(
                    "Vigilante creado correctamente."
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
                    campoEdad,
                    campoPassword,
                    botones
            );

    dialogo.add(formulario);
    dialogo.open();
}

/*
 * Convierte las respuestas de error
 * del backend en mensajes comprensibles.
 */
private void mostrarErrorOperacion(
        RestClientResponseException excepcion) {
    NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
}

    /*
     * Centraliza la comprobación usada por esta pantalla.
     * Así no repetimos el nombre del permiso en varios lugares.
     */
    private boolean puedeEscribir() {

        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    /*
     * Convierte la opción de la pantalla
     * al valor que espera el backend.
     */
    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {

            case "Activos" -> true;
            case "Inactivos" -> false;
            default -> null;
        };
    }

    /*
     * Consulta el backend y carga la tabla.
     */
    private void cargarVigilantes() {

        try {

            tabla.setItems(
                    vigilanteClient.listar(
                            obtenerEstado(),
                            filtroNombre.getValue()
                    )
            );

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

    /*
     * Carga los datos cada vez que
     * se ingresa en esta dirección.
     */
    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarVigilantes();
    }
}
