package grupog.sistemapolicia.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import grupog.sistemapolicia.model.EntidadBancaria;
import grupog.sistemapolicia.service.EntidadBancariaService;
import org.springframework.dao.DataAccessException;

@Route(value = "entidades-bancarias", layout = MainLayout.class)
@PageTitle("Entidades bancarias | Sistema Policial")
public class EntidadesBancariasView extends VerticalLayout {

    private final EntidadBancariaService service;
    private final Grid<EntidadBancaria> grilla;
    private final TextField codigo;
    private final TextField domicilioCentral;

    public EntidadesBancariasView(EntidadBancariaService service) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        codigo = new TextField("Código");
        codigo.setRequiredIndicatorVisible(true);
        codigo.setMaxLength(20);

        domicilioCentral = new TextField("Domicilio central");
        domicilioCentral.setRequiredIndicatorVisible(true);
        domicilioCentral.setMaxLength(150);
        domicilioCentral.setWidth("350px");

        Button registrar = new Button("Registrar", evento -> guardar());
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                codigo, domicilioCentral, registrar);
        formulario.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formulario.setWidthFull();
        formulario.getStyle().set("flex-wrap", "wrap");

        grilla = new Grid<>(EntidadBancaria.class, false);
        grilla.addColumn(EntidadBancaria::getCodigo)
                .setHeader("Código")
                .setSortable(true);
        grilla.addColumn(EntidadBancaria::getDomicilioCentral)
                .setHeader("Domicilio central")
                .setSortable(true);
        grilla.setSizeFull();

        add(new H2("Gestión de entidades bancarias"), formulario, grilla);
        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            service.guardar(new EntidadBancaria(
                    codigo.getValue().trim(), domicilioCentral.getValue().trim()));

            codigo.clear();
            domicilioCentral.clear();
            actualizarGrilla();

            Notification aviso = Notification.show(
                    "Entidad bancaria registrada correctamente");
            aviso.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (IllegalArgumentException excepcion) {
            Notification aviso = Notification.show(excepcion.getMessage());
            aviso.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataAccessException excepcion) {
            Notification aviso = Notification.show(
                    "No se pudo guardar la entidad bancaria. Revisá la conexión con MySQL.");
            aviso.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void actualizarGrilla() {
        grilla.setItems(service.listar());
    }
}
