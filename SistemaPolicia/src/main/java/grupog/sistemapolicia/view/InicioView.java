package grupog.sistemapolicia.view;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Inicio | Sistema Policial")
public class InicioView extends VerticalLayout {

    public InicioView() {
        setPadding(false);
        setSpacing(false);
        setSizeFull();
        addClassName("inicio-view");

        H1 titulo = new H1("Sistema de Gestión Policial");
        Paragraph descripcion = new Paragraph(
                "Administración de entidades bancarias, sucursales, "
                + "vigilantes, bandas, asaltos y casos judiciales.");

        Div portada = new Div(
                new Span("PROYECTO ACADÉMICO"), titulo, descripcion);
        portada.addClassName("inicio-portada");

        Div accesos = new Div(
                new RouterLink("Asaltantes", AsaltantesView.class),
                new RouterLink("Asaltos", AsaltosView.class),
                new RouterLink("Jueces", JuecesView.class));
        accesos.addClassName("inicio-accesos");

        Div creditos = new Div(
                new Span("Fotografías: "),
                new Anchor("https://commons.wikimedia.org/wiki/File:Patrullero_de_la_Polic%C3%ADa_de_la_Ciudad.jpg",
                        "Gobierno de la Ciudad de Buenos Aires"),
                new Span(" ("),
                new Anchor("https://creativecommons.org/licenses/by/2.0/", "CC BY 2.0"),
                new Span(") y "),
                new Anchor("https://commons.wikimedia.org/wiki/File:Palacio_de_Justicia_de_la_Naci%C3%B3n.jpg",
                        "Jrivell"),
                new Span(" ("),
                new Anchor("https://creativecommons.org/licenses/by-sa/3.0/", "CC BY-SA 3.0"),
                new Span("). Recorte y oscurecimiento mediante CSS. Sitio académico no oficial."));
        creditos.addClassName("inicio-creditos");

        add(portada, new H2("Accesos rápidos"), accesos, creditos);
    }
}
