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
import com.vaadin.flow.component.combobox.ComboBox;
import grupog.sistemapolicia.model.Asaltante;
import grupog.sistemapolicia.model.Banda;
import grupog.sistemapolicia.service.AsaltanteService;
import grupog.sistemapolicia.service.BandaService;

@Route(value = "asaltantes", layout = MainLayout.class)
@PageTitle("Asaltantes | Sistema Policial")
public class AsaltantesView extends VerticalLayout {

    private final AsaltanteService service;
    private final Grid<Asaltante> grilla;
    private final TextField clave = new TextField("Clave");
    private final TextField nombre = new TextField("Nombre completo");
    private final ComboBox<Banda> banda = new ComboBox<>("Banda");

    public AsaltantesView(AsaltanteService service, BandaService bandaService) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();
        addClassName("gestion-view");

        clave.setRequiredIndicatorVisible(true);
        clave.setMaxLength(20);
        nombre.setRequiredIndicatorVisible(true);
        nombre.setMaxLength(100);
        banda.setRequiredIndicatorVisible(true);
        banda.setItemLabelGenerator(Banda::getNumeroBanda);
        var bandas = bandaService.listar();
        banda.setItems(bandas);
        if (bandas.isEmpty()) {
            banda.setHelperText("Primero registrá una banda.");
        }

        Button registrar = new Button("Registrar", evento -> guardar());
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                clave, nombre, banda, registrar);
        formulario.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formulario.setWidthFull();
        formulario.getStyle().set("flex-wrap", "wrap");
        formulario.addClassName("gestion-formulario");

        grilla = new Grid<>(Asaltante.class, false);
        grilla.addColumn(Asaltante::getClave).setHeader("Clave").setSortable(true);
        grilla.addColumn(Asaltante::getNombreCompleto).setHeader("Nombre completo").setSortable(true);
        grilla.addColumn(asaltante -> asaltante.getBanda().getNumeroBanda())
                .setHeader("Banda").setSortable(true);
        grilla.setSizeFull();
        grilla.addClassName("gestion-grilla");

        add(new H2("Gestión de asaltantes"), formulario, grilla);

        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            service.guardar(new Asaltante(clave.getValue().trim(),
                    nombre.getValue().trim(), banda.getValue()));

            clave.clear();
            nombre.clear();
            banda.clear();
            actualizarGrilla();

            Notification aviso = Notification.show("Asaltante registrado correctamente");
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
