package prog2.policia_frontend.views;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import prog2.policia_frontend.utils.NotificacionUtils;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.time.format.DateTimeFormatter;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.clients.UsuarioClient;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.SesionUsuario;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.utils.ErrorBackendUtils;

/*
 * Pantalla donde el usuario puede consultar
 * sus propios datos y cambiar su contraseña.
 */
@Route(value = "mi-perfil", layout = MainLayout.class)
@PageTitle("Mi perfil | Sistema Policial")
public class MiPerfilView extends VerticalLayout
        implements BeforeEnterObserver {

    private static final DateTimeFormatter FORMATO_FECHA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SesionUsuario sesionUsuario;
    private final UsuarioClient usuarioClient;

    /* Encabezado de la tarjeta "Mi cuenta". */
    private final Avatar avatar = new Avatar();
    private final Span nombreDestacado = new Span();
    private final Span etiquetaRol = new Span();
    private final Span etiquetaEstado = new Span();

    /* Datos de solo lectura. */
    private final TextField campoCodigo = new TextField("Código");
    private final TextField campoNombre = new TextField("Nombre");
    private final TextField campoRol = new TextField("Rol");
    private final TextField campoEstado = new TextField("Estado");
    private final TextField campoAlta = new TextField("Fecha de alta");

    public MiPerfilView(
            SesionUsuario sesionUsuario,
            UsuarioClient usuarioClient) {

        this.sesionUsuario = sesionUsuario;
        this.usuarioClient = usuarioClient;

        H2 titulo = new H2("Mi perfil");

        /*
         * Dos tarjetas: una para consultar los datos
         * y otra para la acción de seguridad.
         * Quedan lado a lado y se apilan en pantallas angostas.
         */
        Div tarjetas = new Div(
                crearTarjetaCuenta(),
                crearTarjetaSeguridad()
        );
        tarjetas.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "1.25rem")
                .set("align-items", "flex-start")
                .set("width", "100%")
                .set("max-width", "1000px");

        add(titulo, tarjetas);
    }

    /*
     * Aplica el mismo aspecto sobrio a las dos tarjetas.
     */
    private Div crearTarjeta(int anchoMinimo) {

        Div tarjeta = new Div();

        tarjeta.getStyle()
                .set("flex", "1 1 " + anchoMinimo + "px")
                .set("box-sizing", "border-box")
                .set("padding", "1.5rem")
                .set("border-radius", "16px")
                .set("border", "1px solid rgba(84, 176, 255, 0.25)")
                .set("background", "rgba(8, 23, 37, 0.55)");

        return tarjeta;
    }

    /*
     * Tarjeta "Mi cuenta": avatar, nombre, etiquetas y datos.
     */
    private Div crearTarjetaCuenta() {

        avatar.getElement().getThemeList().add("xlarge");
        avatar.getStyle().set("--vaadin-avatar-size", "4rem");

        nombreDestacado.getStyle()
                .set("font-size", "1.35rem")
                .set("font-weight", "700");

        estilizarEtiqueta(etiquetaRol, "#b9e0ff", "rgba(17, 105, 171, 0.30)");
        estilizarEtiqueta(etiquetaEstado, "#8ff0b4", "rgba(30, 140, 80, 0.30)");

        HorizontalLayout etiquetas
                = new HorizontalLayout(etiquetaRol, etiquetaEstado);
        etiquetas.setSpacing(true);
        etiquetas.setPadding(false);

        VerticalLayout textos
                = new VerticalLayout(nombreDestacado, etiquetas);
        textos.setPadding(false);
        textos.setSpacing(false);
        textos.getStyle().set("gap", "0.4rem");

        HorizontalLayout cabecera
                = new HorizontalLayout(avatar, textos);
        cabecera.setAlignItems(HorizontalLayout.Alignment.CENTER);
        cabecera.setPadding(false);
        cabecera.getStyle().set("margin-bottom", "1rem");

        campoCodigo.setReadOnly(true);
        campoNombre.setReadOnly(true);
        campoRol.setReadOnly(true);
        campoEstado.setReadOnly(true);
        campoAlta.setReadOnly(true);

        FormLayout datos = new FormLayout(
                campoCodigo,
                campoNombre,
                campoRol,
                campoEstado,
                campoAlta
        );
        datos.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("26em", 2)
        );

        H3 subtitulo = new H3("Mi cuenta");
        subtitulo.getStyle().set("margin", "0 0 1rem");

        Div tarjeta = crearTarjeta(380);
        tarjeta.add(subtitulo, cabecera, datos);

        return tarjeta;
    }

    /*
     * Tarjeta "Seguridad": cambio de contraseña.
     */
    private Div crearTarjetaSeguridad() {

        Icon candado = VaadinIcon.LOCK.create();
        candado.getStyle().set("color", "#54b0ff");

        H3 subtitulo = new H3("Seguridad");
        subtitulo.getStyle().set("margin", "0");

        HorizontalLayout encabezado
                = new HorizontalLayout(candado, subtitulo);
        encabezado.setAlignItems(HorizontalLayout.Alignment.CENTER);
        encabezado.setPadding(false);

        Paragraph explicacion = new Paragraph(
                "Cambiá tu contraseña cuando lo necesites. "
                + "Debe tener entre 4 y 20 caracteres."
        );
        explicacion.getStyle()
                .set("margin", "0.75rem 0 1.25rem")
                .set("color", "var(--aura-text-color-secondary, #9fb3c8)");

        Button botonCambiarPassword = new Button(
                "Cambiar contraseña",
                VaadinIcon.KEY.create(),
                evento -> abrirCambioPassword()
        );

        Div tarjeta = crearTarjeta(280);
        tarjeta.add(encabezado, explicacion, botonCambiarPassword);

        return tarjeta;
    }

    /*
     * Da forma de etiqueta redondeada a un Span.
     */
    private void estilizarEtiqueta(
            Span etiqueta,
            String colorTexto,
            String colorFondo) {

        etiqueta.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.15rem 0.75rem")
                .set("border-radius", "999px")
                .set("font-size", "0.85rem")
                .set("font-weight", "600")
                .set("color", colorTexto)
                .set("background", colorFondo);
    }

    private void abrirCambioPassword() {

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Cambiar mi contraseña");

        PasswordField campoActual
                = new PasswordField("Contraseña actual");
        PasswordField campoNueva
                = new PasswordField("Nueva contraseña");
        PasswordField campoConfirmacion
                = new PasswordField("Confirmar nueva contraseña");

        campoActual.setRequiredIndicatorVisible(true);
        campoNueva.setRequiredIndicatorVisible(true);
        campoConfirmacion.setRequiredIndicatorVisible(true);
        campoActual.setRevealButtonVisible(false);
        campoNueva.setRevealButtonVisible(false);
        campoConfirmacion.setRevealButtonVisible(false);
        campoNueva.setHelperText("Debe tener entre 4 y 20 caracteres.");
        campoActual.setWidthFull();
        campoNueva.setWidthFull();
        campoConfirmacion.setWidthFull();

        Button botonGuardar = new Button("Guardar contraseña");
        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        botonGuardar.addClickListener(evento -> {
            String actual = campoActual.getValue();
            String nueva = campoNueva.getValue();
            String confirmacion = campoConfirmacion.getValue();

            boolean actualVacia = actual == null || actual.isBlank();
            boolean nuevaInvalida = nueva == null
                    || nueva.length() < 4
                    || nueva.length() > 20;
            boolean confirmacionDistinta
                    = !nuevaInvalida && !nueva.equals(confirmacion);

            campoActual.setInvalid(actualVacia);
            campoActual.setErrorMessage(
                    "La contraseña actual es obligatoria."
            );
            campoNueva.setInvalid(nuevaInvalida);
            campoNueva.setErrorMessage(
                    "La nueva contraseña debe tener entre 4 y 20 caracteres."
            );
            campoConfirmacion.setInvalid(confirmacionDistinta);
            campoConfirmacion.setErrorMessage(
                    "Las contraseñas nuevas no coinciden."
            );

            if (actualVacia || nuevaInvalida || confirmacionDistinta) {
                return;
            }

            try {
                usuarioClient.cambiarPasswordPropia(actual, nueva);
                sesionUsuario.actualizarContrasena(nueva);
                dialogo.close();
                NotificacionUtils.mostrarExito(
                        "Contraseña modificada correctamente."
                );
            } catch (RestClientResponseException excepcion) {
                mostrarErrorPassword(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion(
                        "No se puede conectar con el backend."
                );
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(new VerticalLayout(
                campoActual,
                campoNueva,
                campoConfirmacion,
                new HorizontalLayout(botonGuardar, botonCancelar)
        ));
        dialogo.open();
    }

    private void mostrarErrorPassword(
            RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    /*
     * Antes de mostrar los datos,
     * comprueba que exista un usuario conectado.
     */
    @Override
    public void beforeEnter(BeforeEnterEvent evento) {

        if (!sesionUsuario.estaAutenticado()) {

            evento.rerouteTo(LoginView.class);
            return;
        }

        mostrarDatos();
    }

    /*
     * Obtiene de la sesión los datos del usuario
     * que inició sesión y los muestra en pantalla.
     */
    private void mostrarDatos() {

        UsuarioDTO usuario
                = sesionUsuario.getUsuario();

        String estado;

        if (usuario.getActivo() == null) {
            estado = "No informado";
        } else if (usuario.getActivo()) {
            estado = "Activo";
        } else {
            estado = "Inactivo";
        }

        avatar.setName(usuario.getNombre());
        nombreDestacado.setText(usuario.getNombre());
        etiquetaRol.setText(usuario.getRol());
        etiquetaEstado.setText(estado);

        /* Rojo si el usuario está inactivo. */
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            etiquetaEstado.getStyle()
                    .set("color", "#ffb3b3")
                    .set("background", "rgba(190, 50, 50, 0.30)");
        }

        campoCodigo.setValue(valorOVacio(usuario.getCodigo()));
        campoNombre.setValue(valorOVacio(usuario.getNombre()));
        campoRol.setValue(valorOVacio(usuario.getRol()));
        campoEstado.setValue(estado);

        /* La fecha de alta solo se muestra si el backend la envió. */
        campoAlta.setVisible(usuario.getFechaCreacion() != null);

        if (usuario.getFechaCreacion() != null) {
            campoAlta.setValue(
                    usuario.getFechaCreacion().format(FORMATO_FECHA)
            );
        }
    }

    private String valorOVacio(String valor) {
        return valor == null ? "" : valor;
    }
}