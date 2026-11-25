package prog2.policia_frontend.views;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
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
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.AsaltanteDTO;
import prog2.policia_frontend.dtos.BandaDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.NormalizadorBusqueda;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

/*
 * Pantalla utilizada para administrar bandas.
 */
@Route(value = "bandas", layout = MainLayout.class)
@PageTitle("Bandas | Sistema Policial")
public class BandasView extends VerticalLayout implements BeforeEnterObserver {

    private final BandaClient bandaClient;
    private final PermisosInterfaz permisosInterfaz;

    private final Grid<BandaDTO> tabla = new Grid<>(BandaDTO.class, false);

    private final PaginadorGrid<BandaDTO> paginadorBandas = new PaginadorGrid<>(tabla, 10);

    private final Select<String> criterioBusqueda = new Select<>();
    private final com.vaadin.flow.component.textfield.TextField valorBusqueda = new com.vaadin.flow.component.textfield.TextField("Buscar");
    private List<BandaDTO> datosBusqueda = List.of();
    private final Select<String> filtroEstado = new Select<>();

    private final Button botonNueva = new Button("Nueva banda");

    public BandasView(BandaClient bandaClient, PermisosInterfaz permisosInterfaz) {
        this.bandaClient = bandaClient;
        this.permisosInterfaz = permisosInterfaz;
        configurarFiltro();
        configurarTabla();
        configurarVista();
    }

    private void configurarFiltro() {
        configurarBusqueda();
        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todas", "Activas", "Inactivas");
        filtroEstado.setValue("Todas");
    }

    private void configurarTabla() {

        tabla.addColumn(BandaDTO::getCodigo).setHeader("Código").setAutoWidth(true);

        tabla.addColumn(banda -> banda.getCantMiembros() != null ? banda.getCantMiembros() : 0).setHeader("Cantidad de miembros").setAutoWidth(true);

        tabla.addColumn(banda -> Boolean.TRUE.equals(banda.getActivo()) ? "Activa" : "Inactiva").setHeader("Estado").setAutoWidth(true);

        tabla.addComponentColumn(banda -> {

            Button botonIntegrantes = new Button("Mostrar integrantes", evento -> mostrarIntegrantes(banda));
            botonIntegrantes.setVisible(permisosInterfaz.puedeConsultarAsaltantes());

            if (!puedeEscribir()) {
                return botonIntegrantes;
            }

            Button botonEstado = new Button();

            if (Boolean.TRUE.equals(banda.getActivo())) {
                botonEstado.setText("Dar de baja");
                botonEstado.addClickListener(evento -> abrirConfirmacionBaja(banda));

            } else {
                botonEstado.setText("Reactivar");
                botonEstado.addClickListener(evento -> reactivarBanda(banda));
            }

            return new HorizontalLayout(botonIntegrantes, botonEstado);

        }).setHeader("Acciones").setAutoWidth(true);

        tabla.setSizeFull();
    }

    private void mostrarIntegrantes(BandaDTO banda) {
        try {
            List<AsaltanteDTO> integrantes = bandaClient.listarIntegrantes(banda.getCodigo());

            Dialog dialogo = new Dialog();
            dialogo.setHeaderTitle("Integrantes de " + banda.getCodigo());

            VerticalLayout contenido = new VerticalLayout();
            contenido.setPadding(false);

            if (integrantes.isEmpty()) {
                contenido.add(new Paragraph("La banda no tiene integrantes activos."));
            } else {
                Grid<AsaltanteDTO> tablaIntegrantes = new Grid<>(AsaltanteDTO.class, false);

                tablaIntegrantes.addColumn(AsaltanteDTO::getCodigo).setHeader("Código").setAutoWidth(true);
                tablaIntegrantes.addColumn(AsaltanteDTO::getNombre).setHeader("Nombre").setAutoWidth(true);
                tablaIntegrantes.setItems(integrantes);
                tablaIntegrantes.setAllRowsVisible(true);

                contenido.add(tablaIntegrantes);
            }

            contenido.add(new Button("Cerrar", evento -> dialogo.close()));

            dialogo.add(contenido);
            dialogo.open();

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalArgumentException excepcion) {
            NotificacionUtils.mostrarInformacion(excepcion.getMessage());
        }
    }

    private void configurarVista() {

        H2 titulo = new H2("Bandas");

        botonNueva.addClickListener(evento -> crearBanda());
        botonNueva.setVisible(puedeEscribir());
        HorizontalLayout filtros = crearBarraBusqueda();
        filtros.setAlignItems(HorizontalLayout.Alignment.END);
        setSizeFull();
        add(titulo, botonNueva, filtros, tabla, paginadorBandas.getControles());
        expand(tabla);
    }

    private void crearBanda() {

        try {

            bandaClient.crear();
            cargarBandas();
            NotificacionUtils.mostrarExito("Banda creada correctamente.");

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        }
    }

    private void abrirConfirmacionBaja(BandaDTO banda) {

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Dar de baja " + banda.getCodigo());
        Paragraph mensaje = new Paragraph("Solo podrá darse de baja si no posee asaltantes activos.");
        Button botonConfirmar = new Button("Confirmar baja");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());
        botonConfirmar.addClickListener(evento -> {

            try {

                bandaClient.darDeBaja(banda.getCodigo());
                dialogo.close();
                cargarBandas();
                NotificacionUtils.mostrarExito("Banda dada de baja correctamente.");

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);

            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            }
        });

        dialogo.add(new VerticalLayout(mensaje, new HorizontalLayout(botonConfirmar, botonCancelar)));

        dialogo.open();
    }

    private void reactivarBanda(BandaDTO banda) {

        try {

            bandaClient.reactivar(banda.getCodigo());
            cargarBandas();
            NotificacionUtils.mostrarExito("Banda reactivada correctamente.");

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        }
    }

    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {

            case "Activas" ->
                true;
            case "Inactivas" ->
                false;
            default ->
                null;
        };
    }

    private boolean puedeEscribir() {
        return permisosInterfaz.puedeModificarBandas();
    }

    private void cargarBandas() {
        datosBusqueda = List.of();
        aplicarFiltros();
        try {
            datosBusqueda = bandaClient.listar(null);
            aplicarFiltros();
        } catch (RestClientResponseException excepcion) {
            datosBusqueda = List.of();
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            datosBusqueda = List.of();
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalStateException excepcion) {
            datosBusqueda = List.of();
            NotificacionUtils.mostrarInformacion("Debe iniciar sesión.");
        }
    }

    private void mostrarErrorOperacion(RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarBandas();
    }

    private void configurarBusqueda() {
        criterioBusqueda.setLabel("Criterio de búsqueda");
        criterioBusqueda.setItems("Todas", "Código", "Cantidad de miembros");
        criterioBusqueda.setValue("Todas");
        valorBusqueda.setPlaceholder("Ingrese una coincidencia");
        valorBusqueda.setClearButtonVisible(true);
        valorBusqueda.setVisible(false);
        criterioBusqueda.addValueChangeListener(new com.vaadin.flow.component.HasValue.ValueChangeListener<com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<String>, String>>() {
            @Override public void valueChanged(com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<String>, String> evento) {
                valorBusqueda.clear();
                valorBusqueda.setVisible(!"Todas".equals(evento.getValue()));
                valorBusqueda.setLabel(evento.getValue());
            }
        });
    }

    private HorizontalLayout crearBarraBusqueda() {
        Button buscar = new Button("Buscar", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
            @Override public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) { aplicarFiltros(); }
        });
        Button limpiar = new Button("Limpiar", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
            @Override public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) {
                criterioBusqueda.setValue("Todas");
                valorBusqueda.clear();
                filtroEstado.setValue("Todas");
                aplicarFiltros();
            }
        });
        HorizontalLayout barra = new HorizontalLayout(criterioBusqueda, valorBusqueda, filtroEstado, buscar, limpiar);
        barra.setAlignItems(HorizontalLayout.Alignment.END);
        return barra;
    }

    private void aplicarFiltros() {
        String criterio = criterioBusqueda.getValue();
        String texto = NormalizadorBusqueda.normalizar(valorBusqueda.getValue());
        List<BandaDTO> filtrados = new java.util.ArrayList<>();
        for (BandaDTO dato : datosBusqueda) {
            if ((obtenerEstado() == null || obtenerEstado().equals(dato.getActivo())) && coincideCriterio(dato, criterio, texto)) {
                filtrados.add(dato);
            }
        }
        paginadorBandas.setItems(filtrados);
    }

    private boolean coincideCriterio(BandaDTO dato, String criterio, String texto) {
        if ("Código".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getCodigo()).contains(texto); }
        if ("Cantidad de miembros".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getCantMiembros()).contains(texto); }
        return true;
    }

}
