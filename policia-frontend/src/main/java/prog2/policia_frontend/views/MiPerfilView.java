package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.dtos.RespuestaErrorDTO;
import prog2.policia_frontend.clients.UsuarioClient;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.SesionUsuario;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/*
 * Pantalla donde el usuario puede consultar
 * sus propios datos.
 */
@Route(value = "mi-perfil", layout = MainLayout.class)
@PageTitle("Mi perfil | Sistema Policial")
public class MiPerfilView extends VerticalLayout
        implements BeforeEnterObserver {

    private final SesionUsuario sesionUsuario;
    private final UsuarioClient usuarioClient;

    private final Paragraph datoCodigo = new Paragraph();
    private final Paragraph datoNombre = new Paragraph();
    private final Paragraph datoRol = new Paragraph();
    private final Paragraph datoEstado = new Paragraph();

    public MiPerfilView(
            SesionUsuario sesionUsuario,
            UsuarioClient usuarioClient) {

        this.sesionUsuario = sesionUsuario;
        this.usuarioClient = usuarioClient;

        H2 titulo = new H2("Mi perfil");
        Button botonCambiarPassword = new Button(
                "Cambiar contraseña",
                evento -> abrirCambioPassword()
        );

        add(
                titulo,
                datoCodigo,
                datoNombre,
                datoRol,
                datoEstado,
                botonCambiarPassword
        );
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
                Notification.show(
                        "Contraseña modificada correctamente."
                );
            } catch (RestClientResponseException excepcion) {
                mostrarErrorPassword(excepcion);
            } catch (ResourceAccessException excepcion) {
                Notification.show(
                        "No se puede conectar con el backend."
                );
            } catch (IllegalArgumentException excepcion) {
                Notification.show(excepcion.getMessage());
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

        try {
            RespuestaErrorDTO respuesta = excepcion.getResponseBodyAs(
                    RespuestaErrorDTO.class
            );
            if (respuesta != null
                    && respuesta.getMensaje() != null
                    && !respuesta.getMensaje().isBlank()) {
                Notification.show(respuesta.getMensaje());
                return;
            }
        } catch (RuntimeException ignorada) {
            // Se utiliza el mensaje general de abajo.
        }

        Notification.show("No se pudo modificar la contraseña.");
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

        datoCodigo.setText(
                "Código: " + usuario.getCodigo()
        );

        datoNombre.setText(
                "Nombre: " + usuario.getNombre()
        );

        datoRol.setText(
                "Rol: " + usuario.getRol()
        );

        String estado;

        if (usuario.getActivo() == null) {
            estado = "No informado";
        } else if (usuario.getActivo()) {
            estado = "Activo";
        } else {
            estado = "Inactivo";
        }

        datoEstado.setText(
                "Estado: " + estado
        );
    }
}
