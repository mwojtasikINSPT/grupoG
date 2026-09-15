package grupog.sistemapolicia.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import grupog.sistemapolicia.model.EntidadBancaria;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.service.EntidadBancariaService;
import grupog.sistemapolicia.service.SucursalService;
import org.springframework.dao.DataAccessException;

@Route(value = "sucursales", layout = MainLayout.class)
@PageTitle("Sucursales | Sistema Policial")
public class SucursalesView extends VerticalLayout {

    private final SucursalService service;
    private final Grid<Sucursal> grilla;
    private final TextField codigo;
    private final TextField domicilio;
    private final IntegerField numeroEmpleados;
    private final ComboBox<EntidadBancaria> entidad;

    public SucursalesView(SucursalService service,
            EntidadBancariaService entidadService) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        codigo = new TextField("Código");
        codigo.setRequiredIndicatorVisible(true);
        codigo.setMaxLength(20);

        domicilio = new TextField("Domicilio");
        domicilio.setRequiredIndicatorVisible(true);
        domicilio.setMaxLength(150);

        numeroEmpleados = new IntegerField("Número de empleados");
        numeroEmpleados.setRequiredIndicatorVisible(true);
        numeroEmpleados.setMin(0);

        entidad = new ComboBox<>("Entidad bancaria");
        entidad.setRequiredIndicatorVisible(true);
        entidad.setItemLabelGenerator(EntidadBancaria::getCodigo);
        var entidades = entidadService.listar();
        entidad.setItems(entidades);
        if (entidades.isEmpty()) {
            entidad.setHelperText("Primero registrá una entidad bancaria.");
        }

        Button registrar = new Button("Registrar", evento -> guardar());
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                codigo, domicilio, numeroEmpleados, entidad, registrar);
        formulario.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formulario.setWidthFull();
        formulario.getStyle().set("flex-wrap", "wrap");

        grilla = new Grid<>(Sucursal.class, false);
        grilla.addColumn(Sucursal::getCodigo)
                .setHeader("Código").setSortable(true);
        grilla.addColumn(Sucursal::getDomicilio)
                .setHeader("Domicilio").setSortable(true);
        grilla.addColumn(Sucursal::getNumeroEmpleados)
                .setHeader("Empleados").setSortable(true);
        grilla.addColumn(sucursal -> sucursal.getEntidad() == null
                ? "" : sucursal.getEntidad().getCodigo())
                .setHeader("Entidad bancaria").setSortable(true);
        grilla.setSizeFull();

        add(new H2("Gestión de sucursales"), formulario, grilla);
        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            if (numeroEmpleados.getValue() == null) {
                throw new IllegalArgumentException(
                        "La cantidad de empleados es obligatoria");
            }

            service.guardar(new Sucursal(codigo.getValue().trim(),
                    domicilio.getValue().trim(), numeroEmpleados.getValue(),
                    entidad.getValue()));

            codigo.clear();
            domicilio.clear();
            numeroEmpleados.clear();
            entidad.clear();
            actualizarGrilla();

            Notification aviso = Notification.show(
                    "Sucursal registrada correctamente");
            aviso.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (IllegalArgumentException excepcion) {
            Notification aviso = Notification.show(excepcion.getMessage());
            aviso.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (DataAccessException excepcion) {
            Notification aviso = Notification.show(
                    "No se pudo guardar la sucursal. Revisá la conexión con MySQL.");
            aviso.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void actualizarGrilla() {
        grilla.setItems(service.listar());
    }
}
