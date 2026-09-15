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
import com.vaadin.flow.component.datepicker.DatePicker;
import java.util.Locale;
import grupog.sistemapolicia.model.Asalto;
import grupog.sistemapolicia.model.Asaltante;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.service.AsaltoService;
import grupog.sistemapolicia.service.AsaltanteService;
import grupog.sistemapolicia.service.SucursalService;

@Route(value = "asaltos", layout = MainLayout.class)
@PageTitle("Asaltos | Sistema Policial")
public class AsaltosView extends VerticalLayout {

    private final AsaltoService service;
    private final Grid<Asalto> grilla;
    private final TextField identificador = new TextField("Identificador del asalto");
    private final ComboBox<Asaltante> asaltante = new ComboBox<>("Asaltante");
    private final ComboBox<Sucursal> sucursal = new ComboBox<>("Sucursal");
    private final DatePicker fecha = new DatePicker("Fecha");

    public AsaltosView(AsaltoService service, AsaltanteService asaltanteService,
            SucursalService sucursalService) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        identificador.setRequiredIndicatorVisible(true);
        identificador.setMaxLength(20);
        asaltante.setRequiredIndicatorVisible(true);
        asaltante.setItemLabelGenerator(valor ->
                valor.getClave() + " — " + valor.getNombreCompleto());
        var asaltantes = asaltanteService.listar();
        asaltante.setItems(asaltantes);
        if (asaltantes.isEmpty()) {
            asaltante.setHelperText("Primero registrá un asaltante.");
        }
        sucursal.setRequiredIndicatorVisible(true);
        sucursal.setItemLabelGenerator(Sucursal::getCodigo);
        var sucursales = sucursalService.listar();
        sucursal.setItems(sucursales);
        if (sucursales.isEmpty()) {
            sucursal.setHelperText("Primero registrá una sucursal.");
        }
        fecha.setRequiredIndicatorVisible(true);
        fecha.setLocale(Locale.forLanguageTag("es-AR"));

        Button registrar = new Button("Registrar", evento -> guardar());
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                identificador, asaltante, sucursal, fecha, registrar);
        formulario.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formulario.setWidthFull();
        formulario.getStyle().set("flex-wrap", "wrap");

        grilla = new Grid<>(Asalto.class, false);
        grilla.addColumn(Asalto::getIdAsalto).setHeader("Identificador").setSortable(true);
        grilla.addColumn(valor -> valor.getAsaltante().getClave())
                .setHeader("Asaltante").setSortable(true);
        grilla.addColumn(valor -> valor.getSucursal().getCodigo())
                .setHeader("Sucursal").setSortable(true);
        grilla.addColumn(Asalto::getFecha).setHeader("Fecha").setSortable(true);
        grilla.setSizeFull();

        add(new H2("Gestión de asaltos"), formulario, grilla);

        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            if (fecha.isInvalid()) {
                throw new IllegalArgumentException("La fecha no es válida");
            }
            service.guardar(new Asalto(identificador.getValue().trim(),
                    asaltante.getValue(), sucursal.getValue(), fecha.getValue()));

            identificador.clear();
            asaltante.clear();
            sucursal.clear();
            fecha.clear();
            actualizarGrilla();

            Notification aviso = Notification.show("Asalto registrado correctamente");
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

