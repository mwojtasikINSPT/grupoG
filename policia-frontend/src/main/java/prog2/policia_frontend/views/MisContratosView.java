
package prog2.policia_frontend.views;

import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.ContratoVigilanciaClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.ContratoVigilanciaDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.NormalizadorBusqueda;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

@Route(value = "mis-contratos", layout = MainLayout.class)
@PageTitle("Mis contratos | Sistema Policial")
public class MisContratosView extends VerticalLayout implements BeforeEnterObserver {
    private final Select<String> criterioBusqueda = new Select<>();
    private final com.vaadin.flow.component.textfield.TextField valorBusqueda = new com.vaadin.flow.component.textfield.TextField("Buscar");
    private List<ContratoVigilanciaDTO> datosBusqueda = List.of();
    private final Select<String> filtroEstado = new Select<>();


    private final ContratoVigilanciaClient contratoClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<ContratoVigilanciaDTO> tabla = new Grid<>(ContratoVigilanciaDTO.class, false);
    private final PaginadorGrid<ContratoVigilanciaDTO> paginadorContratos = new PaginadorGrid<>(tabla, 10);

    public MisContratosView(ContratoVigilanciaClient contratoClient, SesionUsuario sesionUsuario) {
        this.contratoClient = contratoClient;
        this.sesionUsuario = sesionUsuario;

        configurarTabla();
        setSizeFull();

        configurarBusqueda();
        add(new H2("Mis contratos de vigilancia"), crearBarraBusqueda(), tabla, paginadorContratos.getControles());

        expand(tabla);
    }

    private void configurarTabla() {
        tabla.addColumn(ContratoVigilanciaDTO::getCodigo).setHeader("Código").setAutoWidth(true);
        tabla.addColumn(ContratoVigilanciaDTO::getFecha).setHeader("Fecha").setAutoWidth(true);
        tabla.addColumn(ContratoVigilanciaDTO::getSucursalCodigo).setHeader("Sucursal").setAutoWidth(true);

        tabla.addColumn(contrato -> Boolean.TRUE.equals(contrato.getConArma()) ? "Sí" : "No")
                .setHeader("Con arma").setAutoWidth(true);

        tabla.addColumn(contrato -> Boolean.TRUE.equals(contrato.getActivo()) ? "Activo" : "Inactivo")
                .setHeader("Estado").setAutoWidth(true);

        tabla.setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarContratos();
    }

    private void cargarContratos() {
        datosBusqueda = List.of();
        aplicarFiltros();
        try {
            String codigo = sesionUsuario.getUsuario().getCodigo();

            List<ContratoVigilanciaDTO> contratos = contratoClient.listarPorVigilante(codigo);

            datosBusqueda = contratos;
            aplicarFiltros();

        } catch (RestClientResponseException excepcion) {
            NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));

        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");

        } catch (IllegalStateException excepcion) {
            NotificacionUtils.mostrarInformacion("Debe iniciar sesión.");
        }
    }

    private void configurarBusqueda() {
        criterioBusqueda.setLabel("Criterio de búsqueda");
        criterioBusqueda.setItems("Todas", "Código", "Fecha", "Sucursal", "Con arma");
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
        String texto = NormalizadorBusqueda.normalizar(valorBusqueda.getValue());
        List<ContratoVigilanciaDTO> filtrados = new java.util.ArrayList<>();
        for (ContratoVigilanciaDTO dato : datosBusqueda) {
            if ("Activos".equals(filtroEstado.getValue()) && !Boolean.TRUE.equals(dato.getActivo())) { continue; }
            if ("Inactivos".equals(filtroEstado.getValue()) && !Boolean.FALSE.equals(dato.getActivo())) { continue; }
            if (coincideCriterio(dato, criterio, texto)) { filtrados.add(dato); }
        }
        paginadorContratos.setItems(filtrados);
    }

    private boolean coincideCriterio(ContratoVigilanciaDTO dato, String criterio, String texto) {
        if ("Código".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getCodigo()).contains(texto); }
        if ("Fecha".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getFecha()).contains(texto); }
        if ("Sucursal".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getSucursalCodigo()).contains(texto); }
        if ("Con arma".equals(criterio)) { return NormalizadorBusqueda.normalizar(Boolean.TRUE.equals(dato.getConArma()) ? "Sí" : "No").contains(texto); }
        return true;
    }

}
