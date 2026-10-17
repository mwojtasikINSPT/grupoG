package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.InvestigadorClient;
import prog2.policia_frontend.dtos.InvestigadorDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Pantalla utilizada para administrar investigadores.
 * Por ahora se accede escribiendo /investigadores.
 */
@Route(
        value = "investigadores",
        layout = MainLayout.class
)
@PageTitle("Investigadores | Sistema Policial")
public class InvestigadoresView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final InvestigadorClient investigadorClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<InvestigadorDTO> tabla
            = new Grid<>(InvestigadorDTO.class, false);

    private final TextField filtroNombre
            = new TextField("Buscar por nombre");

    private final Select<String> filtroEstado
            = new Select<>();

    private final Button botonNuevo
            = new Button("Nuevo investigador");

    public InvestigadoresView(
            InvestigadorClient investigadorClient,
            SesionUsuario sesionUsuario) {

        this.investigadorClient = investigadorClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltros();
        configurarTabla();
        configurarVista();
    }

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

    private void configurarTabla() {

        tabla.addColumn(
                InvestigadorDTO::getCodigo
        ).setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(
                InvestigadorDTO::getNombre
        ).setHeader("Nombre")
                .setAutoWidth(true)
                .setFlexGrow(1);

        tabla.addColumn(
                InvestigadorDTO::getRol
        ).setHeader("Rol")
                .setAutoWidth(true);

        tabla.addColumn(investigador ->
                Boolean.TRUE.equals(
                        investigador.getActivo()
                )
                        ? "Activo"
                        : "Inactivo"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addColumn(investigador ->
                investigador.getMotivoBaja() != null
                        ? investigador.getMotivoBaja()
                        : "-"
        ).setHeader("Motivo de baja")
                .setAutoWidth(true);

        tabla.addComponentColumn(investigador -> {

            /*
             * Los permisos de lectura permiten consultar la tabla,
             * pero no deben mostrar acciones que cambien información.
             */
            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            boolean estaActivo
                    = Boolean.TRUE.equals(
                            investigador.getActivo()
                    );

            Button botonModificar
                    = new Button("Modificar");

            botonModificar.setEnabled(estaActivo);

            if (estaActivo) {

                botonModificar.addClickListener(
                        evento
                                -> abrirFormulario(investigador)
                );

            } else {

                botonModificar.setTooltipText(
                        "Debe reactivar al investigador antes de modificarlo."
                );
            }

            Button botonEstado = new Button();

            if (estaActivo) {

                botonEstado.setText("Dar de baja");
                botonEstado.addClickListener(
                        evento
                                -> abrirDialogoBaja(investigador)
                );

            } else if ("FALLECIMIENTO".equals(
                    investigador.getMotivoBaja()
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
                                -> reactivarInvestigador(investigador)
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

    private void configurarVista() {

        H2 titulo = new H2(
                "Administración de investigadores"
        );

        Button botonBuscar = new Button(
                "Buscar",
                evento -> cargarInvestigadores()
        );

        Button botonLimpiar = new Button(
                "Limpiar",
                evento -> {

                    filtroNombre.clear();
                    filtroEstado.setValue("Todos");
                    cargarInvestigadores();
                }
        );

        botonNuevo.addClickListener(
                evento -> abrirFormulario(null)
        );

        /*
         * El alta de investigadores solamente se muestra
         * cuando la sesión posee permiso de escritura.
         */
        botonNuevo.setVisible(
                puedeEscribir()
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

        setSizeFull();

        add(
                titulo,
                botonNuevo,
                filtros,
                tabla
        );

        expand(tabla);
    }

    /*
     * Utiliza un mismo formulario para crear o modificar.
     * Si investigador es null, se trata de un alta nueva.
     */
    private void abrirFormulario(
            InvestigadorDTO investigador) {

        boolean esCreacion
                = investigador == null;

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                esCreacion
                        ? "Nuevo investigador"
                        : "Modificar " + investigador.getCodigo()
        );

        TextField campoNombre
                = new TextField("Nombre");

        PasswordField campoPassword
                = new PasswordField(
                        esCreacion
                                ? "Contraseña"
                                : "Nueva contraseña"
                );

        if (!esCreacion) {

            campoNombre.setValue(
                    investigador.getNombre() != null
                            ? investigador.getNombre()
                            : ""
            );

            campoPassword.setPlaceholder(
                    "Dejar vacío para conservar la actual"
            );
        }

        campoNombre.setRequiredIndicatorVisible(true);
        campoPassword.setRequiredIndicatorVisible(esCreacion);
        campoPassword.setRevealButtonVisible(false);

        campoNombre.setWidthFull();
        campoPassword.setWidthFull();

        Button botonGuardar
                = new Button(
                        esCreacion
                                ? "Guardar"
                                : "Guardar cambios"
                );

        Button botonCancelar
                = new Button(
                        "Cancelar",
                        evento -> dialogo.close()
                );

        botonGuardar.addClickListener(evento -> {

            boolean nombreVacio
                    = campoNombre.getValue().isBlank();

            String password
                    = campoPassword.getValue();

            boolean passwordObligatoriaVacia
                    = esCreacion
                    && password.isBlank();

            boolean passwordConLargoInvalido
                    = !password.isBlank()
                    && (password.length() < 4
                    || password.length() > 20);

            boolean passwordInvalida
                    = passwordObligatoriaVacia
                    || passwordConLargoInvalido;

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

            InvestigadorDTO datos
                    = new InvestigadorDTO(
                            campoNombre.getValue().trim(),
                            password.isBlank()
                                    ? null
                                    : password
                    );

            try {

                if (esCreacion) {

                    investigadorClient.crear(datos);

                } else {

                    investigadorClient.actualizar(
                            investigador.getCodigo(),
                            datos
                    );
                }

                dialogo.close();
                cargarInvestigadores();

                Notification.show(
                        esCreacion
                                ? "Investigador creado correctamente."
                                : "Investigador modificado correctamente."
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

        dialogo.add(
                new VerticalLayout(
                        campoNombre,
                        campoPassword,
                        botones
                )
        );

        dialogo.open();
    }

    private void abrirDialogoBaja(
            InvestigadorDTO investigador) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Dar de baja "
                        + investigador.getCodigo()
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

                investigadorClient.darDeBaja(
                        investigador.getCodigo(),
                        motivo
                );

                dialogo.close();
                cargarInvestigadores();

                Notification.show(
                        "Investigador dado de baja correctamente."
                );

            } catch (RestClientResponseException excepcion) {

                mostrarErrorOperacion(excepcion);

            } catch (ResourceAccessException excepcion) {

                Notification.show(
                        "No se puede conectar con el backend."
                );
            }
        });

        dialogo.add(
                new VerticalLayout(
                        campoMotivo,
                        new HorizontalLayout(
                                botonConfirmar,
                                botonCancelar
                        )
                )
        );

        dialogo.open();
    }

    private void reactivarInvestigador(
            InvestigadorDTO investigador) {

        try {

            investigadorClient.reactivar(
                    investigador.getCodigo()
            );

            cargarInvestigadores();

            Notification.show(
                    "Investigador reactivado correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );
        }
    }

    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {

            case "Activos" -> true;
            case "Inactivos" -> false;
            default -> null;
        };
    }

    /*
     * Centraliza la comprobación del permiso utilizado
     * para crear, modificar, dar de baja o reactivar.
     */
    private boolean puedeEscribir() {

        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    private void cargarInvestigadores() {

        try {

            tabla.setItems(
                    investigadorClient.listar(
                            obtenerEstado(),
                            filtroNombre.getValue()
                    )
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

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

    private void mostrarErrorOperacion(
            RestClientResponseException excepcion) {

        int estado
                = excepcion.getStatusCode().value();

        if (estado == 400) {

            Notification.show(
                    "Los datos ingresados no son válidos."
            );

        } else if (estado == 401) {

            Notification.show(
                    "La sesión no es válida."
            );

        } else if (estado == 403) {

            Notification.show(
                    "No tiene permiso para realizar esta operación."
            );

        } else if (estado == 404) {

            Notification.show(
                    "No se encontró el investigador solicitado."
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

    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarInvestigadores();
    }
}
