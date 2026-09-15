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
import com.vaadin.flow.component.checkbox.Checkbox;
import java.util.Locale;
import grupog.sistemapolicia.model.ContratoVigilancia;
import grupog.sistemapolicia.model.Sucursal;
import grupog.sistemapolicia.model.Vigilante;
import grupog.sistemapolicia.service.ContratoVigilanciaService;
import grupog.sistemapolicia.service.SucursalService;
import grupog.sistemapolicia.service.VigilanteService;

@Route(value = "contratos-vigilancia", layout = MainLayout.class)
@PageTitle("Contratos de vigilancia | Sistema Policial")
public class ContratosVigilanciaView extends VerticalLayout {

    private final ContratoVigilanciaService service;
    private final Grid<ContratoVigilancia> grilla;
    private final TextField codigo = new TextField("Código");
    private final ComboBox<Sucursal> sucursal = new ComboBox<>("Sucursal");
    private final ComboBox<Vigilante> vigilante = new ComboBox<>("Vigilante");
    private final DatePicker fecha = new DatePicker("Fecha");
    private final Checkbox conArma = new Checkbox("Con arma");

    public ContratosVigilanciaView(ContratoVigilanciaService service, SucursalService sucursalService,
            VigilanteService vigilanteService) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        codigo.setRequiredIndicatorVisible(true);
        codigo.setMaxLength(20);
        sucursal.setRequiredIndicatorVisible(true);
        sucursal.setItemLabelGenerator(Sucursal::getCodigo);
        var sucursales = sucursalService.listar();
        sucursal.setItems(sucursales);
        if (sucursales.isEmpty()) {
            sucursal.setHelperText("Primero registrá una sucursal.");
        }
        vigilante.setRequiredIndicatorVisible(true);
        vigilante.setItemLabelGenerator(Vigilante::getCodigo);
        var vigilantes = vigilanteService.listar();
        vigilante.setItems(vigilantes);
        if (vigilantes.isEmpty()) {
            vigilante.setHelperText("Primero registrá un vigilante.");
        }
        fecha.setRequiredIndicatorVisible(true);
        fecha.setLocale(Locale.forLanguageTag("es-AR"));

        Button registrar = new Button("Registrar", evento -> guardar());
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                codigo, sucursal, vigilante, fecha, conArma, registrar);
        formulario.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formulario.setWidthFull();
        formulario.getStyle().set("flex-wrap", "wrap");

        grilla = new Grid<>(ContratoVigilancia.class, false);
        grilla.addColumn(ContratoVigilancia::getCodigo).setHeader("Código").setSortable(true);
        grilla.addColumn(valor -> valor.getSucursal().getCodigo())
                .setHeader("Sucursal").setSortable(true);
        grilla.addColumn(valor -> valor.getVigilante().getCodigo())
                .setHeader("Vigilante").setSortable(true);
        grilla.addColumn(ContratoVigilancia::getFecha).setHeader("Fecha").setSortable(true);
        grilla.addColumn(valor -> valor.isConArma() ? "Sí" : "No")
                .setHeader("Con arma").setSortable(true);
        grilla.setSizeFull();

        add(new H2("Gestión de contratos de vigilancia"), formulario, grilla);

        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            if (fecha.isInvalid()) {
                throw new IllegalArgumentException("La fecha no es válida");
            }
            service.guardar(new ContratoVigilancia(codigo.getValue().trim(),
                    sucursal.getValue(), vigilante.getValue(), fecha.getValue(),
                    conArma.getValue()));

            codigo.clear();
            sucursal.clear();
            vigilante.clear();
            fecha.clear();
            conArma.setValue(false);
            actualizarGrilla();

            Notification aviso = Notification.show("Contrato de vigilancia registrado correctamente");
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

