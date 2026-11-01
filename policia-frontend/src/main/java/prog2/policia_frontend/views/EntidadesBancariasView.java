
package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.EntidadBancariaClient;
import prog2.policia_frontend.dtos.EntidadBancariaDTO;
import prog2.policia_frontend.layouts.MainLayout;
import com.vaadin.flow.component.dialog.Dialog;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;

/*
 * Pantalla utilizada para administrar
 * las entidades bancarias.
 */
@Route(
        value = "entidades-bancarias",
        layout = MainLayout.class
)
@PageTitle("Entidades bancarias | Sistema Policial")
public class EntidadesBancariasView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final EntidadBancariaClient entidadClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<EntidadBancariaDTO> tabla
            = new Grid<>(
                    EntidadBancariaDTO.class,
                    false
            );

    /*
     * Permite elegir si el texto escrito
     * busca por nombre o domicilio.
     */
    private final Select<String> tipoBusqueda
            = new Select<>();

    private final TextField textoBusqueda
            = new TextField("Buscar");

    private final Select<String> filtroEstado
            = new Select<>();

    private final Button botonBuscar
            = new Button("Buscar");

    private final Button botonLimpiar
            = new Button("Limpiar");
    
    private final Button botonNuevaEntidad
        = new Button("Nueva entidad bancaria");

    public EntidadesBancariasView(
            EntidadBancariaClient entidadClient,
            SesionUsuario sesionUsuario) {

        this.entidadClient = entidadClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltros();
        configurarVista();
        configurarTabla();
    }

    /*
     * Configura las opciones de búsqueda.
     */
    private void configurarFiltros() {

        tipoBusqueda.setLabel("Buscar por");
        tipoBusqueda.setItems(
                "Nombre",
                "Domicilio"
        );
        tipoBusqueda.setValue("Nombre");

        textoBusqueda.setPlaceholder(
                "Ingrese una coincidencia"
        );

        textoBusqueda.setClearButtonVisible(true);

        filtroEstado.setLabel("Estado");
        filtroEstado.setItems(
                "Todos",
                "Activos",
                "Inactivos"
        );
        filtroEstado.setValue("Todos");

        botonBuscar.addClickListener(
                evento -> cargarEntidades()
        );

        botonLimpiar.addClickListener(evento -> {

            tipoBusqueda.setValue("Nombre");
            textoBusqueda.clear();
            filtroEstado.setValue("Todos");

            cargarEntidades();
        });
    }

    /*
     * Organiza visualmente la pantalla.
     */
    private void configurarVista() {

        H2 titulo = new H2(
                "Administración de entidades bancarias"
        );

        HorizontalLayout filtros
                = new HorizontalLayout(
                        tipoBusqueda,
                        textoBusqueda,
                        filtroEstado,
                        botonBuscar,
                        botonLimpiar
                );

        filtros.setAlignItems(
                HorizontalLayout.Alignment.END
        );
        botonNuevaEntidad.addClickListener(
        evento -> abrirFormularioNuevo()
        );
        botonNuevaEntidad.setVisible(puedeEscribir());

        setSizeFull();

        add(
                
        titulo,
        botonNuevaEntidad,
        filtros,
        tabla
        );

        expand(tabla);
    }

    /*
     * Define las columnas de la tabla.
     */
    private void configurarTabla() {

        tabla.addColumn(
                EntidadBancariaDTO::getCodigo
        ).setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(
                EntidadBancariaDTO::getNombre
        ).setHeader("Nombre")
                .setAutoWidth(true);

        tabla.addColumn(
                EntidadBancariaDTO::getDomicilioCentral
        ).setHeader("Domicilio central")
                .setAutoWidth(true)
                .setFlexGrow(1);

        tabla.addColumn(entidad ->
                Boolean.TRUE.equals(
                        entidad.getActivo()
                )
                        ? "Activa"
                        : "Inactiva"
        ).setHeader("Estado")
                .setAutoWidth(true);

        /*
         * Permite modificar solamente
         * entidades que están activas.
         */
        tabla.addComponentColumn(entidad -> {

            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            Button botonModificar
                    = new Button("Modificar");

            botonModificar.setEnabled(
                    Boolean.TRUE.equals(
                            entidad.getActivo()
                    )
            );

            if (!Boolean.TRUE.equals(
                    entidad.getActivo()
            )) {

                botonModificar.setTooltipText(
                        "Debe reactivar la entidad antes de modificarla."
                );
            }

            botonModificar.addClickListener(
                    evento
                            -> abrirFormularioEdicion(entidad)
            );

            Button botonEstado = new Button();

            if (Boolean.TRUE.equals(entidad.getActivo())) {

                botonEstado.setText("Dar de baja");
                botonEstado.addClickListener(
                        evento -> abrirDialogoBaja(entidad)
                );

            } else {

                botonEstado.setText("Reactivar");
                botonEstado.addClickListener(
                        evento -> reactivarEntidad(entidad)
                );
            }

            return new HorizontalLayout(
                    botonModificar,
                    botonEstado
            );

        }).setHeader("Acciones")
                .setAutoWidth(true);

        tabla.setSizeFull();
    }

    /*
     * Abre el formulario con los datos actuales
     * de la entidad bancaria seleccionada.
     */
    private void abrirFormularioEdicion(
            EntidadBancariaDTO entidad) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Modificar "
                        + entidad.getCodigo()
        );

        TextField campoNombre
                = new TextField("Nombre");

        TextField campoDomicilio
                = new TextField("Domicilio central");

        campoNombre.setValue(
                entidad.getNombre() != null
                        ? entidad.getNombre()
                        : ""
        );

        campoDomicilio.setValue(
                entidad.getDomicilioCentral() != null
                        ? entidad.getDomicilioCentral()
                        : ""
        );

        campoNombre.setRequiredIndicatorVisible(true);
        campoDomicilio.setRequiredIndicatorVisible(true);

        campoNombre.setWidthFull();
        campoDomicilio.setWidthFull();

        Button botonGuardar
                = new Button("Guardar cambios");

        Button botonCancelar
                = new Button(
                        "Cancelar",
                        evento -> dialogo.close()
                );

        botonGuardar.addClickListener(evento -> {

            boolean nombreVacio
                    = campoNombre.getValue().isBlank();

            boolean domicilioVacio
                    = campoDomicilio.getValue().isBlank();

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage(
                    "El nombre es obligatorio."
            );

            campoDomicilio.setInvalid(domicilioVacio);
            campoDomicilio.setErrorMessage(
                    "El domicilio es obligatorio."
            );

            if (nombreVacio || domicilioVacio) {
                return;
            }

            EntidadBancariaDTO datos
                    = new EntidadBancariaDTO(
                            campoNombre.getValue().trim(),
                            campoDomicilio.getValue().trim()
                    );

            try {

                entidadClient.actualizar(
                        entidad.getCodigo(),
                        datos
                );

                dialogo.close();
                cargarEntidades();

                Notification.show(
                        "Entidad bancaria modificada correctamente."
                );

            } catch (RestClientResponseException excepcion) {

                mostrarErrorOperacion(excepcion);

            } catch (ResourceAccessException excepcion) {

                Notification.show(
                        "No se puede conectar con el backend."
                );

            } catch (IllegalArgumentException excepcion) {

                Notification.show(
                        excepcion.getMessage()
                );
            }
        });

        HorizontalLayout botones
                = new HorizontalLayout(
                        botonGuardar,
                        botonCancelar
                );

        VerticalLayout formulario
                = new VerticalLayout(
                        campoNombre,
                        campoDomicilio,
                        botones
                );

        dialogo.add(formulario);
        dialogo.open();
    }

    /*
     * Solicita el motivo antes de dar de baja
     * una entidad bancaria activa.
     */
    private void abrirDialogoBaja(
            EntidadBancariaDTO entidad) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Dar de baja a "
                        + entidad.getNombre()
        );

        Select<String> campoMotivo
                = new Select<>();

        campoMotivo.setLabel("Motivo de baja");
        campoMotivo.setItems(
                "CIERRE_DEFINITIVO",
                "FUSION",
                "QUIEBRA",
                "INSOLVENCIA",
                "REVOCACION_DE_LICENCIA",
                "DELITOS_FINANCIEROS"
        );
        campoMotivo.setRequiredIndicatorVisible(true);

        Button botonConfirmar
                = new Button("Confirmar baja");

        Button botonCancelar
                = new Button(
                        "Cancelar",
                        evento -> dialogo.close()
                );

        botonConfirmar.addClickListener(evento -> {

            String motivo
                    = campoMotivo.getValue();

            if (motivo == null) {

                campoMotivo.setInvalid(true);
                campoMotivo.setErrorMessage(
                        "Debe seleccionar un motivo."
                );
                return;
            }

            try {

                entidadClient.darDeBaja(
                        entidad.getCodigo(),
                        motivo
                );

                dialogo.close();
                cargarEntidades();

                Notification.show(
                        "Entidad bancaria dada de baja correctamente."
                );

            } catch (RestClientResponseException excepcion) {

                mostrarErrorOperacion(excepcion);

            } catch (ResourceAccessException excepcion) {

                Notification.show(
                        "No se puede conectar con el backend."
                );

            } catch (IllegalArgumentException excepcion) {

                Notification.show(
                        excepcion.getMessage()
                );
            }
        });

        HorizontalLayout botones
                = new HorizontalLayout(
                        botonConfirmar,
                        botonCancelar
                );

        dialogo.add(
                campoMotivo,
                botones
        );
        dialogo.open();
    }

    /*
     * Reactiva una entidad bancaria inactiva.
     */
    private void reactivarEntidad(
            EntidadBancariaDTO entidad) {

        try {

            entidadClient.reactivar(
                    entidad.getCodigo()
            );

            cargarEntidades();

            Notification.show(
                    "Entidad bancaria reactivada correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            Notification.show(
                    excepcion.getMessage()
            );
        }
    }
    
    /*
 * Abre el formulario para registrar
 * una nueva entidad bancaria.
 */
private void abrirFormularioNuevo() {

    Dialog dialogo = new Dialog();

    dialogo.setHeaderTitle(
            "Nueva entidad bancaria"
    );

    TextField campoNombre
            = new TextField("Nombre");

    TextField campoDomicilio
            = new TextField("Domicilio central");

    campoNombre.setRequiredIndicatorVisible(true);
    campoDomicilio.setRequiredIndicatorVisible(true);

    campoNombre.setWidthFull();
    campoDomicilio.setWidthFull();

    Button botonGuardar
            = new Button("Guardar");

    Button botonCancelar
            = new Button(
                    "Cancelar",
                    evento -> dialogo.close()
            );

    botonGuardar.addClickListener(evento -> {

        boolean nombreVacio
                = campoNombre.getValue().isBlank();

        boolean domicilioVacio
                = campoDomicilio.getValue().isBlank();

        campoNombre.setInvalid(nombreVacio);
        campoNombre.setErrorMessage(
                "El nombre es obligatorio."
        );

        campoDomicilio.setInvalid(domicilioVacio);
        campoDomicilio.setErrorMessage(
                "El domicilio es obligatorio."
        );

        if (nombreVacio || domicilioVacio) {
            return;
        }

        EntidadBancariaDTO datos
                = new EntidadBancariaDTO(
                        campoNombre.getValue().trim(),
                        campoDomicilio.getValue().trim()
                );

        try {

            entidadClient.crear(datos);

            dialogo.close();
            cargarEntidades();

            Notification.show(
                    "Entidad bancaria creada correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            Notification.show(
                    excepcion.getMessage()
            );
        }
    });

    HorizontalLayout botones
            = new HorizontalLayout(
                    botonGuardar,
                    botonCancelar
            );

    VerticalLayout formulario
            = new VerticalLayout(
                    campoNombre,
                    campoDomicilio,
                    botones
            );

    dialogo.add(formulario);
    dialogo.open();
}

/*
 * Convierte los estados HTTP en mensajes
 * comprensibles para el usuario.
 */
private void mostrarErrorOperacion(
        RestClientResponseException excepcion) {
    Notification.show(ErrorBackendUtils.obtenerMensaje(excepcion));
}

    /*
     * Carga los datos cuando se abre la vista.
     */
    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarEntidades();
    }

    /*
     * Convierte la opción visual en el valor
     * que espera el backend.
     */
    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {

            case "Activos" -> true;
            case "Inactivos" -> false;
            default -> null;
        };
    }

    /*
     * Consulta las entidades bancarias
     * utilizando los filtros elegidos.
     */
    private void cargarEntidades() {

        try {

            String texto
                    = textoBusqueda.getValue();

            String nombre = null;
            String domicilio = null;

            if ("Nombre".equals(
                    tipoBusqueda.getValue()
            )) {

                nombre = texto;

            } else {

                domicilio = texto;
            }

            tabla.setItems(
                    entidadClient.listar(
                            obtenerEstado(),
                            nombre,
                            domicilio
                    )
            );

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalStateException excepcion) {

            Notification.show(
                    "Debe iniciar sesión."
            );
        }
    }

    private boolean puedeEscribir() {
        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }
}
