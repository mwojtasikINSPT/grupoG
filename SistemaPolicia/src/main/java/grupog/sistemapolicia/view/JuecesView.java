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
import grupog.sistemapolicia.model.Juez;
import grupog.sistemapolicia.service.JuezService;

@Route(value = "jueces", layout = MainLayout.class)
@PageTitle("Jueces | Sistema Policial")
public class JuecesView extends VerticalLayout {

    private final JuezService service;
    private final Grid<Juez> grilla;
    private final TextField clave = new TextField("Clave interna");
    private final TextField nombre = new TextField("Nombre");
    private final IntegerField anios = new IntegerField("Años de servicio");

    public JuecesView(JuezService service) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        clave.setRequiredIndicatorVisible(true);
        clave.setMaxLength(20);
        nombre.setRequiredIndicatorVisible(true);
        nombre.setMaxLength(100);
        anios.setRequiredIndicatorVisible(true);
        anios.setMin(0);

        Button registrar = new Button("Registrar", evento -> guardar());
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                clave, nombre, anios, registrar);
        formulario.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formulario.setWidthFull();
        formulario.getStyle().set("flex-wrap", "wrap");

        grilla = new Grid<>(Juez.class, false);
        grilla.addColumn(Juez::getClaveInterna).setHeader("Clave interna").setSortable(true);
        grilla.addColumn(Juez::getNombre).setHeader("Nombre").setSortable(true);
        grilla.addColumn(Juez::getAniosServicio).setHeader("Años de servicio").setSortable(true);
        grilla.setSizeFull();

        add(new H2("Gestión de jueces"), formulario, grilla);

        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            if (anios.getValue() == null) {
                throw new IllegalArgumentException("Los años de servicio son obligatorios");
            }
            service.guardar(new Juez(clave.getValue().trim(), anios.getValue(),
                    nombre.getValue().trim()));

            clave.clear();
            nombre.clear();
            anios.clear();
            actualizarGrilla();

            Notification aviso = Notification.show("Juez registrado correctamente");
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

