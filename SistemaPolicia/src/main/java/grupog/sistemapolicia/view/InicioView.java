package grupog.sistemapolicia.view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Inicio | Sistema Policial")
public class InicioView extends VerticalLayout {

    public InicioView() {
        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");

        H1 titulo = new H1("Sistema de Gestión Policial");
        Paragraph descripcion = new Paragraph(
                "Administración de entidades bancarias, sucursales, "
                + "vigilantes, bandas, asaltos y casos judiciales.");

        add(titulo, descripcion);
    }
}
