
package prog2.policia_frontend.views;

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
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

@Route(value = "mis-contratos", layout = MainLayout.class)
@PageTitle("Mis contratos | Sistema Policial")
public class MisContratosView extends VerticalLayout implements BeforeEnterObserver {

    private final ContratoVigilanciaClient contratoClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<ContratoVigilanciaDTO> tabla = new Grid<>(ContratoVigilanciaDTO.class, false);
    private final PaginadorGrid<ContratoVigilanciaDTO> paginadorContratos = new PaginadorGrid<>(tabla, 10);

    public MisContratosView(ContratoVigilanciaClient contratoClient, SesionUsuario sesionUsuario) {
        this.contratoClient = contratoClient;
        this.sesionUsuario = sesionUsuario;

        configurarTabla();
        setSizeFull();

        add(new H2("Mis contratos de vigilancia"), tabla, paginadorContratos.getControles());

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
        try {
            String codigo = sesionUsuario.getUsuario().getCodigo();

            List<ContratoVigilanciaDTO> contratos = contratoClient.listarPorVigilante(codigo);

            paginadorContratos.setItems(contratos);

        } catch (RestClientResponseException excepcion) {
            NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));

        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");

        } catch (IllegalStateException excepcion) {
            NotificacionUtils.mostrarInformacion("Debe iniciar sesión.");
        }
    }
}
