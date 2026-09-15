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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.checkbox.Checkbox;
import grupog.sistemapolicia.model.CasoJudicial;
import grupog.sistemapolicia.model.Asalto;
import grupog.sistemapolicia.model.Juez;
import grupog.sistemapolicia.service.CasoJudicialService;
import grupog.sistemapolicia.service.AsaltoService;
import grupog.sistemapolicia.service.JuezService;

@Route(value = "casos-judiciales", layout = MainLayout.class)
@PageTitle("Casos judiciales | Sistema Policial")
public class CasosJudicialesView extends VerticalLayout {

    private final CasoJudicialService service;
    private final Grid<CasoJudicial> grilla;
    private final ComboBox<Asalto> asalto = new ComboBox<>("Asalto");
    private final ComboBox<Juez> juez = new ComboBox<>("Juez");
    private final Checkbox condenado = new Checkbox("Condenado");
    private final IntegerField meses = new IntegerField("Meses de cárcel");
    private final Checkbox soloCondenados = new Checkbox("Mostrar sólo condenados");

    public CasosJudicialesView(CasoJudicialService service, AsaltoService asaltoService,
            JuezService juezService) {
        this.service = service;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        asalto.setRequiredIndicatorVisible(true);
        asalto.setItemLabelGenerator(Asalto::getIdAsalto);
        var asaltos = asaltoService.listar();
        asalto.setItems(asaltos);
        if (asaltos.isEmpty()) {
            asalto.setHelperText("Primero registrá un asalto.");
        }
        juez.setRequiredIndicatorVisible(true);
        juez.setItemLabelGenerator(valor ->
                valor.getClaveInterna() + " — " + valor.getNombre());
        var jueces = juezService.listar();
        juez.setItems(jueces);
        if (jueces.isEmpty()) {
            juez.setHelperText("Primero registrá un juez.");
        }
        meses.setMin(1);
        meses.setEnabled(false);
        condenado.addValueChangeListener(evento -> {
            meses.setEnabled(evento.getValue());
            meses.setRequiredIndicatorVisible(evento.getValue());
            if (!evento.getValue()) {
                meses.clear();
            }
        });
        soloCondenados.addValueChangeListener(evento -> actualizarGrilla());

        Button registrar = new Button("Registrar", evento -> guardar());
        registrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout formulario = new HorizontalLayout(
                asalto, juez, condenado, meses, registrar);
        formulario.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        formulario.setWidthFull();
        formulario.getStyle().set("flex-wrap", "wrap");

        grilla = new Grid<>(CasoJudicial.class, false);
        grilla.addColumn(CasoJudicial::getIdAsalto).setHeader("Asalto").setSortable(true);
        grilla.addColumn(valor -> valor.getJuez().getClaveInterna())
                .setHeader("Juez").setSortable(true);
        grilla.addColumn(valor -> valor.isCondenado() ? "Sí" : "No")
                .setHeader("Condenado").setSortable(true);
        grilla.addColumn(CasoJudicial::getMesesCarcel)
                .setHeader("Meses de cárcel").setSortable(true);
        grilla.setSizeFull();

        add(new H2("Gestión de casos judiciales"), formulario, grilla);
        addComponentAtIndex(2, soloCondenados);
        expand(grilla);
        actualizarGrilla();
    }

    private void guardar() {
        try {
            if (condenado.getValue()
                    && (meses.getValue() == null || meses.getValue() <= 0)) {
                throw new IllegalArgumentException(
                        "Una condena debe indicar meses de cárcel mayores a cero");
            }
            int cantidadMeses = condenado.getValue() ? meses.getValue() : 0;
            service.guardar(new CasoJudicial(asalto.getValue(), juez.getValue(),
                    condenado.getValue(), cantidadMeses));

            asalto.clear();
            juez.clear();
            condenado.setValue(false);
            meses.clear();
            actualizarGrilla();

            Notification aviso = Notification.show("Caso judicial registrado correctamente");
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
        grilla.setItems(soloCondenados.getValue() ? service.listarDetenidos() : service.listar());
    }
}

