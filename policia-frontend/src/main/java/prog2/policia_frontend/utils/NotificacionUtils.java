package prog2.policia_frontend.utils;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Presenta notificaciones uniformes utilizando los estilos nativos de Vaadin.
 */
public final class NotificacionUtils {

    private static final int DURACION_MILISEGUNDOS = 6000;

    private NotificacionUtils() {
    }

    public static void mostrarExito(String mensaje) {
        mostrar(
                mensaje,
                NotificationVariant.LUMO_SUCCESS,
                VaadinIcon.CHECK_CIRCLE,
                "var(--lumo-success-color-10pct)",
                "var(--lumo-success-color)",
                "var(--lumo-success-text-color)"
        );
    }

    public static void mostrarAdvertencia(String mensaje) {
        mostrar(
                mensaje,
                NotificationVariant.LUMO_WARNING,
                VaadinIcon.WARNING,
                "var(--lumo-warning-color-10pct)",
                "var(--lumo-warning-color)",
                "var(--lumo-warning-text-color)"
        );
    }

    public static void mostrarInformacion(String mensaje) {
        mostrar(
                mensaje,
                NotificationVariant.LUMO_PRIMARY,
                VaadinIcon.INFO_CIRCLE,
                "var(--lumo-primary-color-10pct)",
                "var(--lumo-primary-color)",
                "var(--lumo-primary-text-color)"
        );
    }

    private static void mostrar(
            String mensaje,
            NotificationVariant variante,
            VaadinIcon tipoIcono,
            String colorFondo,
            String colorBorde,
            String colorTexto) {

        Notification notificacion = new Notification();
        notificacion.setDuration(DURACION_MILISEGUNDOS);
        notificacion.setPosition(Notification.Position.MIDDLE);

        Icon icono = tipoIcono.create();
        icono.setSize("var(--lumo-icon-size-m)");
        icono.getStyle().set("color", colorBorde);

        Span texto = new Span(new Text(mensaje));
        texto.getStyle()
                .set("color", colorTexto)
                .set("font-weight", "600")
                .set("line-height", "var(--lumo-line-height-s)")
                .set("white-space", "normal");

        Button cerrar = new Button(VaadinIcon.CLOSE_SMALL.create());
        cerrar.addThemeVariants(
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_ICON
        );
        cerrar.getElement().setAttribute("aria-label", "Cerrar mensaje");
        cerrar.addClickListener(evento -> notificacion.close());

        HorizontalLayout contenido = new HorizontalLayout(
                icono,
                texto,
                cerrar
        );
        contenido.setAlignItems(FlexComponent.Alignment.CENTER);
        contenido.setSpacing(true);
        contenido.setPadding(true);
        contenido.setWidthFull();
        contenido.getStyle()
                .set("min-width", "320px")
                .set("max-width", "560px")
                .set("background", colorFondo)
                .set("border", "2px solid " + colorBorde)
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-l)");
        contenido.expand(texto);

        notificacion.add(contenido);

        notificacion.addThemeVariants(variante);

        notificacion.open();
    }
}
