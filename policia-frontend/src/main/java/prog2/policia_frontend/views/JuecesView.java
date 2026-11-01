package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.JuezClient;
import prog2.policia_frontend.dtos.JuezDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;

/*
 * Pantalla utilizada para administrar jueces.
 * Por ahora se accede escribiendo /jueces.
 */
@Route(
        value = "jueces",
        layout = MainLayout.class
)
@PageTitle("Jueces | Sistema Policial")
public class JuecesView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final JuezClient juezClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<JuezDTO> tabla
            = new Grid<>(JuezDTO.class, false);

    private final TextField filtroNombre
            = new TextField("Buscar por nombre");

    private final Select<String> filtroEstado
            = new Select<>();

    public JuecesView(
            JuezClient juezClient,
            SesionUsuario sesionUsuario) {

        this.juezClient = juezClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltros();
        configurarTabla();
        configurarVista();
    }

    private void configurarFiltros() {

        filtroNombre.setPlaceholder("Ingrese una coincidencia");
        filtroNombre.setClearButtonVisible(true);

        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todos", "Activos", "Inactivos");
        filtroEstado.setValue("Todos");
    }

    private void configurarTabla() {

        tabla.addColumn(JuezDTO::getCodigo)
                .setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(JuezDTO::getNombre)
                .setHeader("Nombre")
                .setAutoWidth(true)
                .setFlexGrow(1);

        tabla.addColumn(JuezDTO::getJuezDesde)
                .setHeader("Juez desde")
                .setAutoWidth(true);

        tabla.addColumn(juez ->
                juez.getAniosServicio() != null
                        ? juez.getAniosServicio()
                        : 0
        ).setHeader("Años de servicio")
                .setAutoWidth(true);

        tabla.addColumn(juez ->
                Boolean.TRUE.equals(juez.getActivo())
                        ? "Activo"
                        : "Inactivo"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addColumn(juez ->
                juez.getMotivoBaja() != null
                        ? juez.getMotivoBaja()
                        : "-"
        ).setHeader("Motivo de baja")
                .setAutoWidth(true);

        tabla.addComponentColumn(juez -> {

            /*
             * La lectura permite consultar jueces, pero las acciones
             * que cambian datos requieren permiso de escritura.
             */
            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            boolean estaActivo
                    = Boolean.TRUE.equals(juez.getActivo());

            Button botonModificar
                    = new Button("Modificar");

            botonModificar.setEnabled(estaActivo);

            if (estaActivo) {

                botonModificar.addClickListener(
                        evento -> abrirFormulario(juez)
                );

            } else {

                botonModificar.setTooltipText(
                        "Debe reactivar al juez antes de modificarlo."
                );
            }

            Button botonEstado = new Button();

            if (estaActivo) {

                botonEstado.setText("Dar de baja");
                botonEstado.addClickListener(
                        evento -> abrirDialogoBaja(juez)
                );

            } else if ("FALLECIMIENTO".equals(juez.getMotivoBaja())) {

                botonEstado.setText("No reactivable");
                botonEstado.setEnabled(false);
                botonEstado.setTooltipText(
                        "El motivo de baja fue fallecimiento."
                );

            } else {

                botonEstado.setText("Reactivar");
                botonEstado.addClickListener(
                        evento -> reactivarJuez(juez)
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

        H2 titulo = new H2("Administración de jueces");

        Button botonNuevo = new Button(
                "Nuevo juez",
                evento -> abrirFormulario(null)
        );

        botonNuevo.setVisible(
                puedeEscribir()
        );

        Button botonBuscar = new Button(
                "Buscar",
                evento -> cargarJueces()
        );

        Button botonLimpiar = new Button(
                "Limpiar",
                evento -> {
                    filtroNombre.clear();
                    filtroEstado.setValue("Todos");
                    cargarJueces();
                }
        );

        HorizontalLayout filtros = new HorizontalLayout(
                filtroNombre,
                filtroEstado,
                botonBuscar,
                botonLimpiar
        );

        filtros.setAlignItems(HorizontalLayout.Alignment.END);

        setSizeFull();
        add(titulo, botonNuevo, filtros, tabla);
        expand(tabla);
    }

    /*
     * El mismo formulario sirve para crear y modificar.
     * El código y los años de servicio los calcula el backend.
     */
    private void abrirFormulario(JuezDTO juez) {

        boolean esCreacion = juez == null;

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                esCreacion
                        ? "Nuevo juez"
                        : "Modificar " + juez.getCodigo()
        );

        TextField campoNombre = new TextField("Nombre");
        DatePicker campoJuezDesde = new DatePicker("Juez desde");

        campoNombre.setRequiredIndicatorVisible(true);
        campoJuezDesde.setRequiredIndicatorVisible(true);
        campoJuezDesde.setMax(LocalDate.now());

        campoNombre.setWidthFull();
        campoJuezDesde.setWidthFull();

        if (!esCreacion) {

            campoNombre.setValue(
                    juez.getNombre() != null
                            ? juez.getNombre()
                            : ""
            );

            campoJuezDesde.setValue(juez.getJuezDesde());
        }

        Button botonGuardar = new Button(
                esCreacion
                        ? "Guardar"
                        : "Guardar cambios"
        );

        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        botonGuardar.addClickListener(evento -> {

            boolean nombreVacio
                    = campoNombre.getValue().isBlank();

            boolean fechaInvalida
                    = campoJuezDesde.getValue() == null
                    || campoJuezDesde.getValue().isAfter(LocalDate.now());

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage("El nombre es obligatorio.");

            campoJuezDesde.setInvalid(fechaInvalida);
            campoJuezDesde.setErrorMessage(
                    "La fecha debe ser de hoy o pasada."
            );

            if (nombreVacio || fechaInvalida) {
                return;
            }

            JuezDTO datos = new JuezDTO(
                    campoNombre.getValue().trim(),
                    campoJuezDesde.getValue()
            );

            try {

                if (esCreacion) {
                    juezClient.crear(datos);
                } else {
                    juezClient.actualizar(juez.getCodigo(), datos);
                }

                dialogo.close();
                cargarJueces();

                Notification.show(
                        esCreacion
                                ? "Juez creado correctamente."
                                : "Juez modificado correctamente."
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
                        campoJuezDesde,
                        new HorizontalLayout(
                                botonGuardar,
                                botonCancelar
                        )
                )
        );

        dialogo.open();
    }

    private void abrirDialogoBaja(JuezDTO juez) {

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Dar de baja " + juez.getCodigo());

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
        campoMotivo.setWidthFull();

        Button botonConfirmar = new Button("Confirmar baja");
        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        botonConfirmar.addClickListener(evento -> {

            String motivo = campoMotivo.getValue();
            boolean motivoVacio = motivo == null || motivo.isBlank();

            campoMotivo.setInvalid(motivoVacio);
            campoMotivo.setErrorMessage("Debe seleccionar un motivo.");

            if (motivoVacio) {
                return;
            }

            try {

                juezClient.darDeBaja(juez.getCodigo(), motivo);

                dialogo.close();
                cargarJueces();

                Notification.show("Juez dado de baja correctamente.");

            } catch (RestClientResponseException excepcion) {

                mostrarErrorOperacion(excepcion);

            } catch (ResourceAccessException excepcion) {

                Notification.show("No se puede conectar con el backend.");
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

    private void reactivarJuez(JuezDTO juez) {

        try {

            juezClient.reactivar(juez.getCodigo());
            cargarJueces();

            Notification.show("Juez reactivado correctamente.");

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show("No se puede conectar con el backend.");
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
     * Comprueba una sola vez la regla utilizada para mostrar
     * el alta, la modificación, la baja y la reactivación.
     */
    private boolean puedeEscribir() {

        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    private void cargarJueces() {

        try {

            tabla.setItems(
                    juezClient.listar(
                            obtenerEstado(),
                            filtroNombre.getValue()
                    )
            );

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

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {

        cargarJueces();
    }
}
