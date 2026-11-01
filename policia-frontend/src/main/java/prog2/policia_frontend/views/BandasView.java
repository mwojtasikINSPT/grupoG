package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import prog2.policia_frontend.utils.NotificacionUtils;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.BandaClient;
import prog2.policia_frontend.dtos.BandaDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;

/*
 * Pantalla utilizada para administrar bandas.
 * Por ahora se accede escribiendo /bandas.
 */
@Route(
        value = "bandas",
        layout = MainLayout.class
)
@PageTitle("Bandas | Sistema Policial")
public class BandasView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final BandaClient bandaClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<BandaDTO> tabla
            = new Grid<>(BandaDTO.class, false);

    private final Select<String> filtroEstado
            = new Select<>();

    private final Button botonNueva
            = new Button("Nueva banda");

    public BandasView(
            BandaClient bandaClient,
            SesionUsuario sesionUsuario) {

        this.bandaClient = bandaClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltro();
        configurarTabla();
        configurarVista();
    }

    private void configurarFiltro() {

        filtroEstado.setLabel("Estado");
        filtroEstado.setItems(
                "Todas",
                "Activas",
                "Inactivas"
        );
        filtroEstado.setValue("Todas");
    }

    private void configurarTabla() {

        tabla.addColumn(
                BandaDTO::getCodigo
        ).setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(banda ->
                banda.getCantMiembros() != null
                        ? banda.getCantMiembros()
                        : 0
        ).setHeader("Cantidad de miembros")
                .setAutoWidth(true);

        tabla.addColumn(banda ->
                Boolean.TRUE.equals(
                        banda.getActivo()
                )
                        ? "Activa"
                        : "Inactiva"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addComponentColumn(banda -> {

            /*
             * La consulta continúa disponible con permiso de lectura,
             * pero cambiar el estado requiere permiso de escritura.
             */
            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            Button botonEstado = new Button();

            if (Boolean.TRUE.equals(
                    banda.getActivo()
            )) {

                botonEstado.setText("Dar de baja");
                botonEstado.addClickListener(
                        evento
                                -> abrirConfirmacionBaja(banda)
                );

            } else {

                botonEstado.setText("Reactivar");
                botonEstado.addClickListener(
                        evento
                                -> reactivarBanda(banda)
                );
            }

            return botonEstado;

        }).setHeader("Acciones")
                .setAutoWidth(true);

        tabla.setSizeFull();
    }

    private void configurarVista() {

        H2 titulo = new H2(
                "Administración de bandas"
        );

        Button botonBuscar = new Button(
                "Buscar",
                evento -> cargarBandas()
        );

        Button botonLimpiar = new Button(
                "Limpiar",
                evento -> {

                    filtroEstado.setValue("Todas");
                    cargarBandas();
                }
        );

        botonNueva.addClickListener(
                evento -> crearBanda()
        );

        botonNueva.setVisible(
                puedeEscribir()
        );

        HorizontalLayout filtros
                = new HorizontalLayout(
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
                botonNueva,
                filtros,
                tabla
        );

        expand(tabla);
    }

    /*
     * No se abre un formulario porque una banda
     * nueva todavía no posee datos para ingresar.
     */
    private void crearBanda() {

        try {

            bandaClient.crear();
            cargarBandas();

            NotificacionUtils.mostrarExito(
                    "Banda creada correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );
        }
    }

    private void abrirConfirmacionBaja(
            BandaDTO banda) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Dar de baja " + banda.getCodigo()
        );

        Paragraph mensaje = new Paragraph(
                "Solo podrá darse de baja si no posee asaltantes activos."
        );

        Button botonConfirmar
                = new Button("Confirmar baja");

        Button botonCancelar
                = new Button(
                        "Cancelar",
                        evento -> dialogo.close()
                );

        botonConfirmar.addClickListener(evento -> {

            try {

                bandaClient.darDeBaja(
                        banda.getCodigo()
                );

                dialogo.close();
                cargarBandas();

                NotificacionUtils.mostrarExito(
                        "Banda dada de baja correctamente."
                );

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);

            } catch (ResourceAccessException excepcion) {

                NotificacionUtils.mostrarInformacion(
                        "No se puede conectar con el backend."
                );
            }
        });

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

    private void reactivarBanda(
            BandaDTO banda) {

        try {

            bandaClient.reactivar(
                    banda.getCodigo()
            );

            cargarBandas();

            NotificacionUtils.mostrarExito(
                    "Banda reactivada correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );
        }
    }

    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {

            case "Activas" -> true;
            case "Inactivas" -> false;
            default -> null;
        };
    }

    /*
     * Centraliza el permiso necesario para crear,
     * dar de baja o reactivar una banda.
     */
    private boolean puedeEscribir() {

        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    private void cargarBandas() {

        try {

            tabla.setItems(
                    bandaClient.listar(
                            obtenerEstado()
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

    private void mostrarErrorOperacion(
            RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarBandas();
    }
}
