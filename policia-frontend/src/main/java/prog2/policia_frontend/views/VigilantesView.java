package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import prog2.policia_frontend.clients.VigilanteClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.VigilanteDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

/*
 * Pantalla utilizada para administrar vigilantes.
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

    private final PaginadorGrid<VigilanteDTO> paginadorVigilantes
            = new PaginadorGrid<>(tabla, 10);

    private final TextField filtroNombre
            = new TextField("Buscar por nombre");

    private final Select<String> filtroEstado
            = new Select<>();

    private final Button botonNuevoVigilante
            = new Button("Nuevo vigilante");

    public VigilantesView(
            VigilanteClient vigilanteClient,
            SesionUsuario sesionUsuario) {

        this.vigilanteClient = vigilanteClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltros();
        configurarVista();
        configurarTabla();
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
                vigilante -> vigilante.getFechaNacimiento() != null
                ? vigilante.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : ""
        ).setHeader("Fecha de nacimiento")
                .setAutoWidth(true);
        
        tabla.addColumn(
                VigilanteDTO::getRol
        ).setHeader("Rol")
                .setAutoWidth(true);

        tabla.addColumn(vigilante
                -> Boolean.TRUE.equals(
                        vigilante.getActivo()
                )
                ? "Activo"
                : "Inactivo"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addColumn(vigilante
                -> vigilante.getMotivoBaja() != null
                ? vigilante.getMotivoBaja()
                : "-"
        ).setHeader("Motivo de baja")
                .setAutoWidth(true);

        /*
         * Muestra las acciones disponibles
         * según el estado del vigilante.
         */
        tabla.addComponentColumn(vigilante -> {

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
                "Vigilantes"
        );

        Button botonBuscar = new Button(
                "Buscar",
                evento -> cargarVigilantes()
        );

        Button botonLimpiar = new Button(
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

        botonNuevoVigilante.setVisible(
                puedeEscribir()
        );
        setSizeFull();

        add(
                titulo,
                botonNuevoVigilante,
                filtros,
                tabla,
                paginadorVigilantes.getControles()
        );

        expand(tabla);
    }

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

    private void abrirFormularioEdicion(
            VigilanteDTO vigilante) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Modificar "
                + vigilante.getCodigo()
        );

        TextField campoNombre
                = new TextField("Nombre");

        DatePicker campoFechaNacimiento
                = new DatePicker("Fecha de nacimiento");

        PasswordField campoPassword
                = new PasswordField(
                        "Nueva contraseña"
                );

        campoNombre.setValue(
                vigilante.getNombre() != null
                ? vigilante.getNombre()
                : ""
        );

        if (vigilante.getFechaNacimiento() != null) {
            campoFechaNacimiento.setValue(vigilante.getFechaNacimiento());
        }

        campoPassword.setPlaceholder(
                "Dejar vacío para conservar la actual"
        );

        campoPassword.setRevealButtonVisible(false);

        campoNombre.setRequiredIndicatorVisible(true);
        campoFechaNacimiento.setRequiredIndicatorVisible(true);

        campoFechaNacimiento.setMin(LocalDate.now().minusYears(66).plusDays(1));
        campoFechaNacimiento.setMax(LocalDate.now().minusYears(18));

        campoNombre.setWidthFull();
        campoFechaNacimiento.setWidthFull();
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

            LocalDate fechaIngresada = campoFechaNacimiento.getValue();
            boolean fechaInvalida = fechaIngresada == null || fechaIngresada.isAfter(LocalDate.now().minusYears(18)) || !fechaIngresada.isAfter(LocalDate.now().minusYears(66));

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

            campoFechaNacimiento.setInvalid(fechaInvalida);
            campoFechaNacimiento.setErrorMessage("La edad debe estar entre 18 y 65 años.");

            campoPassword.setInvalid(passwordInvalida);
            campoPassword.setErrorMessage(
                    "La contraseña debe tener entre 4 y 20 caracteres."
            );

            if (nombreVacio
                    || fechaInvalida
                    || passwordInvalida) {

                return;
            }

            String passwordParaEnviar
                    = nuevaPassword.isBlank()
                    ? null
                    : nuevaPassword;

            VigilanteDTO datos
                    = new VigilanteDTO(
                            campoNombre.getValue().trim(),
                            passwordParaEnviar,
                            fechaIngresada
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
                        campoFechaNacimiento,
                        campoPassword,
                        botones
                );

        dialogo.add(formulario);
        dialogo.open();
    }

    private void abrirFormularioNuevo() {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Nuevo vigilante"
        );

        TextField campoNombre
                = new TextField("Nombre");

        DatePicker campoFechaNacimiento
                = new DatePicker("Fecha de nacimiento");

        PasswordField campoPassword
                = new PasswordField("Contraseña");

        campoNombre.setRequiredIndicatorVisible(true);
        campoFechaNacimiento.setRequiredIndicatorVisible(true);
        campoPassword.setRequiredIndicatorVisible(true);

        campoFechaNacimiento.setMin(LocalDate.now().minusYears(66).plusDays(1));
        campoFechaNacimiento.setMax(LocalDate.now().minusYears(18));

        campoPassword.setRevealButtonVisible(false);

        campoNombre.setWidthFull();
        campoFechaNacimiento.setWidthFull();
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

            LocalDate fechaIngresada = campoFechaNacimiento.getValue();
            boolean fechaInvalida = fechaIngresada == null || fechaIngresada.isAfter(LocalDate.now().minusYears(18)) || !fechaIngresada.isAfter(LocalDate.now().minusYears(66));

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

            campoFechaNacimiento.setInvalid(fechaInvalida);
            campoFechaNacimiento.setErrorMessage("La edad debe estar entre 18 y 65 años.");

            campoPassword.setInvalid(passwordInvalida);
            campoPassword.setErrorMessage(
                    "La contraseña debe tener entre 4 y 20 caracteres."
            );

            if (nombreVacio
                    || fechaInvalida
                    || passwordInvalida) {

                return;
            }

            VigilanteDTO datos
                    = new VigilanteDTO(
                            campoNombre.getValue().trim(),
                            password,
                            fechaIngresada
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
                        campoFechaNacimiento,
                        campoPassword,
                        botones
                );

        dialogo.add(formulario);
        dialogo.open();
    }

    private void mostrarErrorOperacion(
            RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    private boolean puedeEscribir() {

        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {

            case "Activos" ->
                true;
            case "Inactivos" ->
                false;
            default ->
                null;
        };
    }

    private void cargarVigilantes() {

        try {

            List<VigilanteDTO> vigilantes = vigilanteClient.listar(
                    obtenerEstado(),
                    filtroNombre.getValue()
            );

            paginadorVigilantes.setItems(vigilantes);

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

    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarVigilantes();
    }
}
