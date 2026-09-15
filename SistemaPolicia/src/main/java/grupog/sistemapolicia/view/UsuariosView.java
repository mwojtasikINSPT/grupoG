package grupog.sistemapolicia.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import grupog.sistemapolicia.dto.UsuarioResponse;
import grupog.sistemapolicia.service.UsuarioService;

@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Usuarios | Sistema Policial")
public class UsuariosView extends VerticalLayout {

    private final UsuarioService service;
    private final Grid<UsuarioResponse> grilla;

    public UsuariosView(UsuarioService service) {
        this.service = service;
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        grilla = new Grid<>(UsuarioResponse.class, false);
        grilla.addColumn(UsuarioResponse::username)
                .setHeader("Usuario").setSortable(true);
        grilla.addColumn(UsuarioResponse::rol)
                .setHeader("Rol").setSortable(true);
        grilla.addColumn(valor -> valor.codigoVigilante() == null
                ? "—" : valor.codigoVigilante())
                .setHeader("Vigilante asociado").setSortable(true);
        grilla.setSizeFull();

        add(new H2("Usuarios"), new Paragraph(
                "Listado sin contraseñas. El login y la gestión segura de "
                + "credenciales están pendientes de integrar con Spring Security."),
                new Button("Actualizar listado", evento -> actualizarGrilla()), grilla);
        expand(grilla);
        actualizarGrilla();
    }

    private void actualizarGrilla() {
        grilla.setItems(service.listar());
    }
}
