package grupog.sistemapolicia.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import grupog.sistemapolicia.model.Vigilante;
import grupog.sistemapolicia.service.VigilanteService;

@Route(value = "vigilantes", layout = MainLayout.class)
@PageTitle("Vigilantes | Sistema Policial")
public class VigilantesView extends VerticalLayout {

    private final VigilanteService service;
    private final Grid<Vigilante> grilla;
    private final TextField codigo;
    private final IntegerField edad;

    public VigilantesView(VigilanteService service) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        codigo = new TextField("Código");
        codigo.setRequiredIndicatorVisible(true);
        codigo.setMaxLength(20);

        edad = new IntegerField("Edad");
        edad.setRequiredIndicatorVisible(true);
        edad.setMin(18);
        edad.setMax(100);

        Button guardar = new Button("Registrar", evento -> guardar());
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                codigo, edad, guardar);
        formulario.setDefaultVerticalComponentAlignment(
                Alignment.BASELINE);

        grilla = new Grid<>(Vigilante.class, false);
        grilla.addColumn(Vigilante::getCodigo)
                .setHeader("Código")
                .setSortable(true);
        grilla.addColumn(Vigilante::getEdad)
                .setHeader("Edad")
                .setSortable(true);
        grilla.setSizeFull();

        add(new H2("Gestión de vigilantes"), formulario, grilla);
        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            int valorEdad = edad.getValue() == null ? 0 : edad.getValue();
            service.guardar(new Vigilante(codigo.getValue(), valorEdad));

            codigo.clear();
            edad.clear();
            actualizarGrilla();

            Notification aviso = Notification.show(
                    "Vigilante registrado correctamente");
            aviso.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (IllegalArgumentException excepcion) {
            Notification aviso = Notification.show(excepcion.getMessage());
            aviso.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void actualizarGrilla() {
        grilla.setItems(service.listar());
    }
}
