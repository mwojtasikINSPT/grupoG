package prog2.policia_frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.Map;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.vaadin.flow.component.html.Span;

import prog2.policia_frontend.clients.AsaltanteClient;
import prog2.policia_frontend.clients.BandaClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.AsaltanteDTO;
import prog2.policia_frontend.dtos.BandaDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

/*
 * Pantalla utilizada para administrar asaltantes.
 */
@Route(
        value = "asaltantes",
        layout = MainLayout.class
)
@PageTitle("Asaltantes | Sistema Policial")
public class AsaltantesView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final AsaltanteClient asaltanteClient;
    private final BandaClient bandaClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<AsaltanteDTO> tabla
            = new Grid<>(AsaltanteDTO.class, false);

    private final PaginadorGrid<AsaltanteDTO> paginador
            = new PaginadorGrid<>(tabla, 10);

    private final TextField filtroNombre
            = new TextField("Buscar por nombre");

    private final Select<String> filtroEstado
            = new Select<>();

    private final Button botonNuevo
            = new Button("Nuevo asaltante");

    public AsaltantesView(
            AsaltanteClient asaltanteClient,
            BandaClient bandaClient,
            SesionUsuario sesionUsuario) {

        this.asaltanteClient = asaltanteClient;
        this.bandaClient = bandaClient;
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
                AsaltanteDTO::getCodigo
        ).setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(
                AsaltanteDTO::getNombre
        ).setHeader("Nombre")
                .setAutoWidth(true)
                .setFlexGrow(1);

        tabla.addColumn(asaltante
                -> asaltante.getBandaCodigo() != null
                ? asaltante.getBandaCodigo()
                : "Sin banda"
        ).setHeader("Banda")
                .setAutoWidth(true);

        tabla.addColumn(asaltante
                -> Boolean.TRUE.equals(
                        asaltante.getActivo()
                )
                ? "Activo"
                : "Inactivo"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addColumn(asaltante
                -> asaltante.getMotivoBaja() != null
                ? asaltante.getMotivoBaja()
                : "-"
        ).setHeader("Motivo de baja")
                .setAutoWidth(true);

        tabla.addComponentColumn(asaltante -> {

            Button botonVerCasos = new Button(
                    "Ver casos",
                    evento -> verCasos(asaltante)
            );

            botonVerCasos.setVisible(
                    sesionUsuario.tieneRol("ADMINISTRADOR")
                    || sesionUsuario.tienePermiso("LECTURA_CASO_JUDICIAL")
            );

            if (!puedeEscribir()) {
                return botonVerCasos;
            }

            boolean estaActivo
                    = Boolean.TRUE.equals(
                            asaltante.getActivo()
                    );

            Button botonModificar
                    = new Button("Modificar");

            Button botonBaja
                    = new Button(
                            estaActivo
                                    ? "Dar de baja"
                                    : "Baja definitiva"
                    );

            botonModificar.setEnabled(estaActivo);
            botonBaja.setEnabled(estaActivo);

            if (estaActivo) {

                botonModificar.addClickListener(
                        evento
                        -> abrirFormulario(asaltante)
                );

                botonBaja.addClickListener(
                        evento
                        -> abrirConfirmacionBaja(asaltante)
                );

            } else {

                botonModificar.setTooltipText(
                        "Los asaltantes inactivos no pueden modificarse."
                );

                botonBaja.setTooltipText(
                        "El backend no permite reactivar asaltantes."
                );
            }

            return new HorizontalLayout(
                    botonVerCasos,
                    botonModificar,
                    botonBaja
            );

        }).setHeader("Acciones")
                .setAutoWidth(true);

        if (sesionUsuario.tieneRol("ADMINISTRADOR")
                || sesionUsuario.tienePermiso("LECTURA_BANDA")) {

            tabla.addComponentColumn(asaltante -> {
                Button boton = new Button("Ver banda", evento -> verBanda(asaltante));
                boton.setEnabled(asaltante.getBandaCodigo() != null
                        && !asaltante.getBandaCodigo().isBlank());
                return boton;
            }).setAutoWidth(true);
        }
        tabla.setSizeFull();
        tabla.addThemeVariants(
                GridVariant.LUMO_COMPACT
        );
    }

    private void verCasos(AsaltanteDTO asaltante) {
        UI.getCurrent().navigate(
                CasosJudicialesView.class,
                QueryParameters.simple(
                        Map.of("asaltante", asaltante.getCodigo())
                )
        );
    }

    private void configurarVista() {

        H2 titulo = new H2(
                "Asaltantes"
        );

        Button botonBuscar = new Button(
                "Buscar",
                evento -> cargarAsaltantes()
        );

        Button botonLimpiar = new Button(
                "Limpiar",
                evento -> {

                    filtroNombre.clear();
                    filtroEstado.setValue("Todos");
                    cargarAsaltantes();
                }
        );

        botonNuevo.addClickListener(
                evento -> abrirFormulario(null)
        );

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
                tabla,
                paginador.getControles()
        );

        expand(tabla);
    }

    private void abrirFormulario(
            AsaltanteDTO asaltante) {

        boolean esCreacion
                = asaltante == null;

        List<BandaDTO> bandasActivas;

        try {

            bandasActivas = bandaClient.listar(true);

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);
            return;

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se pueden consultar las bandas."
            );
            return;
        }

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                esCreacion
                        ? "Nuevo asaltante"
                        : "Modificar " + asaltante.getCodigo()
        );

        TextField campoNombre
                = new TextField("Nombre");

        ComboBox<BandaDTO> campoBanda
                = new ComboBox<>("Banda");

        campoBanda.setItems(bandasActivas);
        campoBanda.setItemLabelGenerator(
                BandaDTO::getCodigo
        );
        campoBanda.setClearButtonVisible(true);
        campoBanda.setPlaceholder("Sin banda");

        if (!esCreacion) {

            campoNombre.setValue(
                    asaltante.getNombre() != null
                    ? asaltante.getNombre()
                    : ""
            );

            BandaDTO bandaActual
                    = bandasActivas.stream()
                            .filter(banda
                                    -> banda.getCodigo() != null
                            && banda.getCodigo()
                                    .equalsIgnoreCase(
                                            asaltante.getBandaCodigo()
                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (bandaActual != null) {
                campoBanda.setValue(bandaActual);
            }
        }

        campoNombre.setRequiredIndicatorVisible(true);
        campoNombre.setWidthFull();
        campoBanda.setWidthFull();

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

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage(
                    "El nombre es obligatorio."
            );

            if (nombreVacio) {
                return;
            }

            BandaDTO bandaSeleccionada
                    = campoBanda.getValue();

            AsaltanteDTO datos
                    = new AsaltanteDTO(
                            campoNombre.getValue().trim(),
                            bandaSeleccionada != null
                                    ? bandaSeleccionada.getCodigo()
                                    : null
                    );

            boolean quitarBanda
                    = !esCreacion
                    && asaltante.getBandaCodigo() != null
                    && bandaSeleccionada == null;

            datos.setQuitarDeBanda(quitarBanda);

            try {

                AsaltanteDTO asaltanteGuardado;

                if (esCreacion) {

                    asaltanteGuardado = asaltanteClient.crear(datos);

                } else {

                    asaltanteGuardado = asaltanteClient.actualizar(
                            asaltante.getCodigo(),
                            datos
                    );
                }

                dialogo.close();
                cargarAsaltantes();

                NotificacionUtils.mostrarExito(
                        esCreacion
                                ? "Asaltante "
                                + asaltanteGuardado.getCodigo()
                                + " creado correctamente."
                                : "Asaltante "
                                + asaltanteGuardado.getCodigo()
                                + " - "
                                + asaltanteGuardado.getNombre()
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

        dialogo.add(
                new VerticalLayout(
                        campoNombre,
                        campoBanda,
                        new HorizontalLayout(
                                botonGuardar,
                                botonCancelar
                        )
                )
        );

        dialogo.open();
    }

    private void abrirConfirmacionBaja(
            AsaltanteDTO asaltante) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Baja definitiva de "
                + asaltante.getCodigo()
        );

        Paragraph mensaje = new Paragraph(
                "La única baja permitida es por fallecimiento "
                + "y el asaltante no podrá reactivarse."
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

                asaltanteClient.darDeBaja(
                        asaltante.getCodigo()
                );

                dialogo.close();
                cargarAsaltantes();

                NotificacionUtils.mostrarInformacion(
                        "Asaltante dado de baja definitivamente."
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

    private boolean puedeEscribir() {

        return sesionUsuario.tieneRol("ADMINISTRADOR") || sesionUsuario.tienePermiso(PermisosApp.ESCRITURA + "_ASALTANTE");
    }

    private void cargarAsaltantes() {

        try {

            paginador.setItems(
                    asaltanteClient.listar(
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

    private void mostrarErrorOperacion(
            RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarAsaltantes();
    }

    private void verBanda(AsaltanteDTO asaltante) {
        try {
            for (BandaDTO banda : bandaClient.listar(null)) {
                if (asaltante.getBandaCodigo().equalsIgnoreCase(banda.getCodigo())) {
                    Dialog dialogo = new Dialog();
                    dialogo.setHeaderTitle("Banda " + banda.getCodigo());

                    String estado = Boolean.TRUE.equals(banda.getActivo()) ? "Activa" : "Inactiva";
                    int miembros = banda.getCantMiembros() != null ? banda.getCantMiembros() : 0;

                    VerticalLayout contenido = new VerticalLayout();
                    contenido.add(new Span("Estado: " + estado));
                    contenido.add(new Span("Cantidad de miembros: " + miembros));
                    contenido.add(new Span("Integrantes activos:"));

                    List<AsaltanteDTO> integrantes = bandaClient.listarIntegrantes(banda.getCodigo());

                    if (integrantes.isEmpty()) {
                        contenido.add(new Span("Sin integrantes activos."));
                    } else {
                        for (AsaltanteDTO integrante : integrantes) {
                            contenido.add(new Span(integrante.getNombre() + " (" + integrante.getCodigo() + ")"));
                        }
                    }

                    contenido.add(new Button("Cerrar", evento -> dialogo.close()));
                    dialogo.add(contenido);
                    dialogo.open();
                    return;
                }
            }

            NotificacionUtils.mostrarInformacion("No se encontró la banda.");
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pudo consultar la banda.");
        }
    }
}
