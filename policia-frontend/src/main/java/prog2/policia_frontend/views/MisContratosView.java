package prog2.policia_frontend.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.VigilanteClient;
import prog2.policia_frontend.dtos.ContratoVigilanciaDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.SesionUsuario;

/**
 * Muestra al vigilante autenticado únicamente sus propios contratos.
 */
@Route(value = "mis-contratos", layout = MainLayout.class)
@PageTitle("Mis contratos | Sistema Policial")
public class MisContratosView extends VerticalLayout
        implements BeforeEnterObserver {

    private final VigilanteClient vigilanteClient;
    private final SesionUsuario sesionUsuario;
    private final Grid<ContratoVigilanciaDTO> tabla
            = new Grid<>(ContratoVigilanciaDTO.class, false);

    public MisContratosView(
            VigilanteClient vigilanteClient,
            SesionUsuario sesionUsuario) {

        this.vigilanteClient = vigilanteClient;
        this.sesionUsuario = sesionUsuario;

        configurarTabla();
        setSizeFull();
        add(new H2("Mis contratos de vigilancia"), tabla);
        expand(tabla);
    }

    private void configurarTabla() {

        tabla.addColumn(ContratoVigilanciaDTO::getCodigo)
                .setHeader("Código").setAutoWidth(true);
        tabla.addColumn(ContratoVigilanciaDTO::getFecha)
                .setHeader("Fecha").setAutoWidth(true);
        tabla.addColumn(ContratoVigilanciaDTO::getSucursalCodigo)
                .setHeader("Sucursal").setAutoWidth(true);
        tabla.addColumn(contrato
                -> Boolean.TRUE.equals(contrato.getConArma()) ? "Sí" : "No")
                .setHeader("Con arma").setAutoWidth(true);
        tabla.addColumn(contrato
                -> Boolean.TRUE.equals(contrato.getActivo())
                ? "Activo" : "Inactivo")
                .setHeader("Estado").setAutoWidth(true);
        tabla.setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        cargarContratos();
    }

    private void cargarContratos() {

        try {
            String codigo = sesionUsuario.getUsuario().getCodigo();
            tabla.setItems(vigilanteClient.listarContratosPropios(codigo));
        } catch (RestClientResponseException excepcion) {
            int estado = excepcion.getStatusCode().value();
            Notification.show(estado == 403
                    ? "No tiene permiso para consultar estos contratos."
                    : "No se pudieron consultar sus contratos.");
        } catch (ResourceAccessException excepcion) {
            Notification.show("No se puede conectar con el backend.");
        }
    }
}
