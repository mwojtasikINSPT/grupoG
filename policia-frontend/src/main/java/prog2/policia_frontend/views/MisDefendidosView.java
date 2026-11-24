package prog2.policia_frontend.views;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
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
        setSizeFull(); add(new H2("Mis defendidos"), actualizar, tabla, paginador.getControles()); expand(tabla);
    }

    private void cargar() {
        if (!permisos.puedeConsultarDefensa()) { return; }
        try { paginador.setItems(client.listarDefendidos()); }
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
}
