package grupog.sistemapolicia.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.dao.DataAccessException;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.IntegerField;
import grupog.sistemapolicia.model.Banda;
import grupog.sistemapolicia.service.BandaService;

@Route(value = "bandas", layout = MainLayout.class)
@PageTitle("Bandas | Sistema Policial")
public class BandasView extends VerticalLayout {

    private final BandaService service;
    private final Grid<Banda> grilla;
    private final TextField numero = new TextField("Número de banda");
    private final IntegerField miembros = new IntegerField("Cantidad de miembros");

    public BandasView(BandaService service) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        numero.setRequiredIndicatorVisible(true);
        numero.setMaxLength(20);
        miembros.setRequiredIndicatorVisible(true);
        miembros.setMin(0);

        Button registrar = new Button("Registrar", evento -> guardar());
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                numero, miembros, registrar);
        formulario.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formulario.setWidthFull();
        formulario.getStyle().set("flex-wrap", "wrap");

        grilla = new Grid<>(Banda.class, false);
        grilla.addColumn(Banda::getNumeroBanda).setHeader("Número de banda").setSortable(true);
        grilla.addColumn(Banda::getCantMiembros).setHeader("Miembros").setSortable(true);
        grilla.setSizeFull();

        add(new H2("Gestión de bandas"), formulario, grilla);

        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            if (miembros.getValue() == null) {
                throw new IllegalArgumentException("La cantidad de miembros es obligatoria");
            }
            service.guardar(new Banda(numero.getValue().trim(), miembros.getValue()));

            numero.clear();
            miembros.clear();
            actualizarGrilla();

            Notification aviso = Notification.show("Banda registrada correctamente");
            aviso.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (IllegalArgumentException excepcion) {
            Notification aviso = Notification.show(excepcion.getMessage());
            aviso.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataAccessException excepcion) {
            Notification aviso = Notification.show(
                    "No se pudo guardar. Revisá la conexión con MySQL.");
            aviso.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void actualizarGrilla() {
        grilla.setItems(service.listar());
    }
}

