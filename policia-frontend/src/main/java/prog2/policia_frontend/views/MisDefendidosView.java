package prog2.policia_frontend.views;

import java.util.List;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.DefensaClient;
import prog2.policia_frontend.clients.PruebaDefensaClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.components.PruebasDefensaDialog;
import prog2.policia_frontend.dtos.CasoDefensaDTO;
import prog2.policia_frontend.dtos.DefendidoDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

@Route(value = "mis-defendidos", layout = MainLayout.class)
@PageTitle("Mis defendidos | Sistema Policial")
public class MisDefendidosView extends VerticalLayout implements BeforeEnterObserver {
    private final Select<String> criterioBusqueda = new Select<>();
    private final com.vaadin.flow.component.textfield.TextField valorBusqueda = new com.vaadin.flow.component.textfield.TextField("Buscar");
    private List<DefendidoDTO> datosBusqueda = List.of();
    private final Select<String> filtroEstado = new Select<>();

    private final DefensaClient client;
    private final PruebaDefensaClient pruebas;
    private final PermisosInterfaz permisos;
    private final Grid<DefendidoDTO> tabla = new Grid<>(DefendidoDTO.class, false);
    private final PaginadorGrid<DefendidoDTO> paginador = new PaginadorGrid<>(tabla, 10);

    public MisDefendidosView(DefensaClient client, PruebaDefensaClient pruebas, PermisosInterfaz permisos) {
        this.client = client; this.pruebas = pruebas; this.permisos = permisos;
        tabla.addColumn(new ValueProvider<DefendidoDTO, String>() {
            @Override public String apply(DefendidoDTO d) { return d.getCodigo(); }
        }).setHeader("Código");
        tabla.addColumn(new ValueProvider<DefendidoDTO, String>() {
            @Override public String apply(DefendidoDTO d) { return d.getNombre(); }
        }).setHeader("Nombre");
        tabla.addColumn(new ValueProvider<DefendidoDTO, String>() {
            @Override public String apply(DefendidoDTO d) { return Boolean.TRUE.equals(d.getActivo()) ? "Activo" : "Inactivo"; }
        }).setHeader("Estado");
        tabla.addComponentColumn(new ValueProvider<DefendidoDTO, Component>() {
            @Override public Component apply(DefendidoDTO d) {
                return new Button("Datos y casos", new ComponentEventListener<ClickEvent<Button>>() {
                    @Override public void onComponentEvent(ClickEvent<Button> evento) { abrirDefendido(d.getCodigo()); }
                });
            }
        }).setHeader("Consulta");
        Button actualizar = new Button("Actualizar", new ComponentEventListener<ClickEvent<Button>>() {
            @Override public void onComponentEvent(ClickEvent<Button> evento) { cargar(); }
        });
        configurarBusqueda();
        setSizeFull(); add(new H2("Mis defendidos"), actualizar, crearBarraBusqueda(), tabla, paginador.getControles()); expand(tabla);
    }

    private void cargar() {
        if (!permisos.puedeConsultarDefensa()) { return; }
        datosBusqueda = List.of();
        aplicarFiltros();
        try { datosBusqueda = client.listarDefendidos(); aplicarFiltros(); }
        catch (RestClientResponseException | ResourceAccessException ex) { paginador.setItems(java.util.List.of()); mostrarError(ex); }
    }

    private void abrirDefendido(String codigo) {
        try {
            DefendidoDTO d = client.consultarDefendido(codigo);
            Dialog dialogo = new Dialog(); dialogo.setHeaderTitle(d.getCodigo() + " · " + d.getNombre());
            dialogo.setWidth("1050px"); dialogo.setMaxWidth("95vw");
            Grid<CasoDefensaDTO> casos = new Grid<>(CasoDefensaDTO.class, false);
            casos.addColumn(new ValueProvider<CasoDefensaDTO, String>() {
                @Override public String apply(CasoDefensaDTO c) { return c.getCodigo(); }
            }).setHeader("Caso");
            casos.addColumn(new ValueProvider<CasoDefensaDTO, String>() {
                @Override public String apply(CasoDefensaDTO c) { return c.getEstado(); }
            }).setHeader("Estado del caso");
            casos.addColumn(new ValueProvider<CasoDefensaDTO, String>() {
                @Override public String apply(CasoDefensaDTO c) { return c.getSituacionJudicial(); }
            }).setHeader("Situación individual");
            casos.addColumn(new ValueProvider<CasoDefensaDTO, String>() {
                @Override public String apply(CasoDefensaDTO c) {
                    if (!Boolean.TRUE.equals(c.getSentenciado())) { return "Sin sentencia"; }
                    return Boolean.TRUE.equals(c.getCondenado()) ? "Condenado · " + c.getTiempoCarcel() + " meses" : "No condenado";
                }
            }).setHeader("Sentencia individual");
            casos.addComponentColumn(new ValueProvider<CasoDefensaDTO, Component>() {
                @Override public Component apply(CasoDefensaDTO c) {
                    Button boton = new Button("Pruebas de defensa", new ComponentEventListener<ClickEvent<Button>>() {
                        @Override public void onComponentEvent(ClickEvent<Button> evento) {
                            try {
                                CasoDefensaDTO actual = client.consultarCaso(codigo, c.getCodigo());
                                new PruebasDefensaDialog(pruebas, permisos, codigo, actual.getCodigo(),
                                        Boolean.TRUE.equals(actual.getActivo()) && !"CERRADO".equals(actual.getEstado())).open();
                            } catch (RestClientResponseException | ResourceAccessException ex) { mostrarError(ex); }
                        }
                    });
                    boton.setVisible(permisos.puedeConsultarPruebas()); return boton;
                }
            }).setHeader("Pruebas");
            casos.setItems(client.listarCasos(codigo)); casos.setHeight("330px"); casos.setWidthFull();
            Button cerrar = new Button("Cerrar", new ComponentEventListener<ClickEvent<Button>>() {
                @Override public void onComponentEvent(ClickEvent<Button> evento) { dialogo.close(); }
            });
            dialogo.add(new Paragraph("Estado: " + (Boolean.TRUE.equals(d.getActivo()) ? "Activo" : "Inactivo")
                    + (d.getMotivoBaja() == null ? "" : " · Motivo: " + d.getMotivoBaja())), casos);
            dialogo.getFooter().add(cerrar); dialogo.open();
        } catch (RestClientResponseException | ResourceAccessException ex) { mostrarError(ex); }
    }

    private void mostrarError(RuntimeException ex) {
        NotificacionUtils.mostrarAdvertencia(ex instanceof RestClientResponseException
                ? ErrorBackendUtils.obtenerMensaje((RestClientResponseException) ex) : "No se puede conectar con el backend.");
    }

    @Override public void beforeEnter(BeforeEnterEvent evento) {
        if (!permisos.puedeConsultarDefensa()) { evento.rerouteTo(InicioView.class); return; }
        cargar();
    }

    private void configurarBusqueda() {
        criterioBusqueda.setLabel("Criterio de búsqueda");
        criterioBusqueda.setItems("Todas", "Código", "Nombre", "Motivo de baja");
        criterioBusqueda.setValue("Todas");
        valorBusqueda.setClearButtonVisible(true);
        valorBusqueda.setPlaceholder("Ingrese una coincidencia");
        valorBusqueda.setVisible(false);
        criterioBusqueda.addValueChangeListener(new com.vaadin.flow.component.HasValue.ValueChangeListener<com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<String>, String>>() {
            @Override public void valueChanged(com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<String>, String> evento) {
                valorBusqueda.clear();
                valorBusqueda.setVisible(!"Todas".equals(evento.getValue()));
                valorBusqueda.setLabel(evento.getValue());
            }
        });
        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todos", "Activos", "Inactivos");
        filtroEstado.setValue("Todos");
    }

    private HorizontalLayout crearBarraBusqueda() {
        Button buscar = new Button("Buscar", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
            @Override public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) { aplicarFiltros(); }
        });
        Button limpiar = new Button("Limpiar", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
            @Override public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) {
                criterioBusqueda.setValue("Todas");
                valorBusqueda.clear();
                filtroEstado.setValue("Todos");
                aplicarFiltros();
            }
        });
        HorizontalLayout barra = new HorizontalLayout(criterioBusqueda, valorBusqueda, filtroEstado, buscar, limpiar);
        barra.setAlignItems(HorizontalLayout.Alignment.END);
        return barra;
    }

    private void aplicarFiltros() {
        String criterio = criterioBusqueda.getValue();
        String texto = normalizarBusqueda(valorBusqueda.getValue());
        List<DefendidoDTO> filtrados = new java.util.ArrayList<>();
        for (DefendidoDTO dato : datosBusqueda) {
            if ("Activos".equals(filtroEstado.getValue()) && !Boolean.TRUE.equals(dato.getActivo())) { continue; }
            if ("Inactivos".equals(filtroEstado.getValue()) && !Boolean.FALSE.equals(dato.getActivo())) { continue; }
            if (coincideCriterio(dato, criterio, texto)) { filtrados.add(dato); }
        }
        paginador.setItems(filtrados);
    }

    private boolean coincideCriterio(DefendidoDTO dato, String criterio, String texto) {
        if ("Código".equals(criterio)) { return normalizarBusqueda(dato.getCodigo()).contains(texto); }
        if ("Nombre".equals(criterio)) { return normalizarBusqueda(dato.getNombre()).contains(texto); }
        if ("Motivo de baja".equals(criterio)) { return normalizarBusqueda(dato.getMotivoBaja()).contains(texto); }
        return true;
    }

    private String normalizarBusqueda(Object valor) {
        if (valor == null) { return ""; }
        String texto = valor.toString();
        if (valor instanceof java.time.LocalDate) {
            texto += " " + ((java.time.LocalDate) valor).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else if (valor instanceof java.time.LocalDateTime) {
            texto += " " + ((java.time.LocalDateTime) valor).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT).trim();
    }
}
