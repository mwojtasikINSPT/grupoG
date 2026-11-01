package prog2.policia_frontend.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Controla páginas visibles de un Grid usando componentes oficiales Vaadin.
 */
public class PaginadorGrid<T> {

    private final Grid<T> grid;
    private final int elementosPorPagina;
    private final List<T> elementos = new ArrayList<>();
    private final Button anterior = new Button("Anterior");
    private final Button siguiente = new Button("Siguiente");
    private final Span indicador = new Span();
    private final HorizontalLayout controles;
    private int paginaActual;

    public PaginadorGrid(Grid<T> grid, int elementosPorPagina) {

        if (grid == null) {
            throw new IllegalArgumentException("El Grid es obligatorio.");
        }
        if (elementosPorPagina <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad por página debe ser mayor que cero."
            );
        }

        this.grid = grid;
        this.elementosPorPagina = elementosPorPagina;
        this.controles = new HorizontalLayout(anterior, indicador, siguiente);
        this.controles.setAlignItems(HorizontalLayout.Alignment.CENTER);

        anterior.addClickListener(evento -> cambiarPagina(-1));
        siguiente.addClickListener(evento -> cambiarPagina(1));
        actualizarVista();
    }

    public HorizontalLayout getControles() {
        return controles;
    }

    public void setItems(Collection<T> nuevosElementos) {
        elementos.clear();
        if (nuevosElementos != null) {
            elementos.addAll(nuevosElementos);
        }
        paginaActual = 0;
        actualizarVista();
    }

    private void cambiarPagina(int desplazamiento) {
        int nuevaPagina = paginaActual + desplazamiento;
        if (nuevaPagina >= 0 && nuevaPagina < totalPaginas()) {
            paginaActual = nuevaPagina;
            actualizarVista();
        }
    }

    private void actualizarVista() {
        int totalPaginas = totalPaginas();

        if (elementos.isEmpty()) {
            grid.setItems(List.of());
            indicador.setText("Sin resultados");
            anterior.setEnabled(false);
            siguiente.setEnabled(false);
            return;
        }

        int desde = paginaActual * elementosPorPagina;
        int hasta = Math.min(desde + elementosPorPagina, elementos.size());
        grid.setItems(elementos.subList(desde, hasta));

        indicador.setText(
                "Página " + (paginaActual + 1) + " de " + totalPaginas
                + " · " + elementos.size() + " registros"
        );
        anterior.setEnabled(paginaActual > 0);
        siguiente.setEnabled(paginaActual + 1 < totalPaginas);
    }

    private int totalPaginas() {
        return (int) Math.ceil(
                (double) elementos.size() / elementosPorPagina
        );
    }
}
