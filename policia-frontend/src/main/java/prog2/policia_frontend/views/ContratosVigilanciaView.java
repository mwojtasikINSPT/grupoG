package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
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
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.ContratoVigilanciaClient;
import prog2.policia_frontend.clients.SucursalClient;
import prog2.policia_frontend.clients.VigilanteClient;
import prog2.policia_frontend.dtos.ContratoVigilanciaDTO;
import prog2.policia_frontend.dtos.SucursalDTO;
import prog2.policia_frontend.dtos.VigilanteDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;

/*
 * Pantalla utilizada para consultar y administrar
 * los contratos de vigilancia.
 * Por ahora se accede escribiendo /contratos-vigilancia.
 */
@Route(
        value = "contratos-vigilancia",
        layout = MainLayout.class
)
@PageTitle("Contratos de vigilancia | Sistema Policial")
public class ContratosVigilanciaView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final ContratoVigilanciaClient contratoClient;
    private final VigilanteClient vigilanteClient;
    private final SucursalClient sucursalClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<ContratoVigilanciaDTO> tabla
            = new Grid<>(ContratoVigilanciaDTO.class, false);

    private final TextField filtroVigilante
            = new TextField("Buscar por vigilante");

    private final Select<String> filtroTipoBusqueda
            = new Select<>();

    private final Select<VigilanteDTO> filtroVigilanteExacto
            = new Select<>();

    private final Select<SucursalDTO> filtroSucursalExacta
            = new Select<>();

    private final Select<String> filtroEstado
            = new Select<>();

    private final Button botonBuscar
            = new Button("Buscar");

    private final Button botonLimpiar
            = new Button("Limpiar");

    private final Button botonNuevoContrato
            = new Button("Nuevo contrato");

    public ContratosVigilanciaView(
            ContratoVigilanciaClient contratoClient,
            VigilanteClient vigilanteClient,
            SucursalClient sucursalClient,
            SesionUsuario sesionUsuario) {

        this.contratoClient = contratoClient;
        this.vigilanteClient = vigilanteClient;
        this.sucursalClient = sucursalClient;
        this.sesionUsuario = sesionUsuario;

        configurarFiltros();
        configurarTabla();
        configurarVista();
    }

    /*
     * Configura la búsqueda por nombre
     * del vigilante y estado del contrato.
     */
    private void configurarFiltros() {

        filtroTipoBusqueda.setLabel("Tipo de búsqueda");
        filtroTipoBusqueda.setItems(
                "General",
                "Por vigilante",
                "Por sucursal"
        );
        filtroTipoBusqueda.setValue("General");

        filtroVigilante.setPlaceholder(
                "Ingrese una coincidencia del nombre"
        );

        filtroVigilante.setClearButtonVisible(true);

        filtroVigilanteExacto.setLabel("Vigilante");
        filtroVigilanteExacto.setItemLabelGenerator(vigilante ->
                vigilante.getCodigo()
                        + " - "
                        + vigilante.getNombre()
        );
        filtroVigilanteExacto.setVisible(false);

        filtroSucursalExacta.setLabel("Sucursal");
        filtroSucursalExacta.setItemLabelGenerator(sucursal ->
                sucursal.getCodigo()
                        + " - "
                        + sucursal.getDomicilio()
        );
        filtroSucursalExacta.setVisible(false);

        filtroTipoBusqueda.addValueChangeListener(evento -> {

            boolean general = "General".equals(evento.getValue());
            boolean porVigilante
                    = "Por vigilante".equals(evento.getValue());
            boolean porSucursal
                    = "Por sucursal".equals(evento.getValue());

            filtroVigilante.setVisible(general);
            filtroVigilanteExacto.setVisible(porVigilante);
            filtroSucursalExacta.setVisible(porSucursal);

            filtroVigilante.clear();
            filtroVigilanteExacto.clear();
            filtroSucursalExacta.clear();
        });

        filtroEstado.setLabel("Estado");
        filtroEstado.setItems(
                "Todos",
                "Activos",
                "Inactivos"
        );
        filtroEstado.setValue("Todos");

        botonBuscar.addClickListener(
                evento -> cargarContratos()
        );

        botonLimpiar.addClickListener(evento -> {

            filtroVigilante.clear();
            filtroTipoBusqueda.setValue("General");
            filtroVigilanteExacto.clear();
            filtroSucursalExacta.clear();
            filtroEstado.setValue("Todos");
            cargarContratos();
        });
    }

    /*
     * Define la información que se muestra
     * en la tabla. No expone datos sensibles.
     */
    private void configurarTabla() {

        tabla.addColumn(
                ContratoVigilanciaDTO::getCodigo
        ).setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(
                ContratoVigilanciaDTO::getFecha
        ).setHeader("Fecha")
                .setAutoWidth(true);

        tabla.addColumn(contrato ->
                Boolean.TRUE.equals(
                        contrato.getConArma()
                )
                        ? "Sí"
                        : "No"
        ).setHeader("Con arma")
                .setAutoWidth(true);

        tabla.addColumn(
                ContratoVigilanciaDTO::getVigilanteCodigo
        ).setHeader("Vigilante")
                .setAutoWidth(true);

        tabla.addColumn(
                ContratoVigilanciaDTO::getSucursalCodigo
        ).setHeader("Sucursal")
                .setAutoWidth(true);

        tabla.addColumn(contrato ->
                Boolean.TRUE.equals(
                        contrato.getActivo()
                )
                        ? "Activo"
                        : "Inactivo"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addColumn(contrato ->
                contrato.getMotivoBaja() != null
                        ? contrato.getMotivoBaja()
                        : "-"
        ).setHeader("Motivo de baja")
                .setAutoWidth(true);

        /*
         * Solamente permite modificar o cancelar contratos
         * activos cuya fecha sea de hoy o futura.
         */
        tabla.addComponentColumn(contrato -> {

            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            Button botonModificar
                    = new Button("Modificar");

            Button botonCancelarContrato
                    = new Button("Cancelar");

            boolean puedeOperarse
                    = Boolean.TRUE.equals(
                            contrato.getActivo()
                    )
                    && contrato.getFecha() != null
                    && !contrato.getFecha()
                            .isBefore(LocalDate.now());

            botonModificar.setEnabled(puedeOperarse);
            botonCancelarContrato.setEnabled(puedeOperarse);

            if (puedeOperarse) {

                botonModificar.addClickListener(
                        evento
                                -> abrirFormularioEdicion(contrato)
                );

                botonCancelarContrato.addClickListener(
                        evento
                                -> abrirConfirmacionCancelacion(contrato)
                );

            } else {

                botonModificar.setTooltipText(
                        "Los contratos cancelados o cumplidos no pueden modificarse."
                );

                botonCancelarContrato.setTooltipText(
                        "Los contratos cancelados o cumplidos no pueden cancelarse."
                );
            }

            return new HorizontalLayout(
                    botonModificar,
                    botonCancelarContrato
            );

        }).setHeader("Acciones")
                .setAutoWidth(true);

        tabla.setSizeFull();
    }

    /*
     * Organiza los componentes de la pantalla.
     */
    private void configurarVista() {

        H2 titulo = new H2(
                "Contratos de vigilancia"
        );

        HorizontalLayout filtros
                = new HorizontalLayout(
                        filtroTipoBusqueda,
                        filtroVigilante,
                        filtroVigilanteExacto,
                        filtroSucursalExacta,
                        filtroEstado,
                        botonBuscar,
                        botonLimpiar
                );

        filtros.setAlignItems(
                HorizontalLayout.Alignment.END
        );

        botonNuevoContrato.addClickListener(
                evento -> abrirFormularioNuevo()
        );
        botonNuevoContrato.setVisible(puedeEscribir());

        setSizeFull();

        add(
                titulo,
                botonNuevoContrato,
                filtros,
                tabla
        );

        expand(tabla);
    }

    /*
     * Solicita una confirmación antes de cancelar
     * definitivamente un contrato de vigilancia.
     */
    private void abrirConfirmacionCancelacion(
            ContratoVigilanciaDTO contrato) {

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Cancelar "
                        + contrato.getCodigo()
        );

        Paragraph mensaje = new Paragraph(
                "El contrato quedará inactivo y no podrá reactivarse. "
                + "¿Desea continuar?"
        );

        Button botonConfirmar
                = new Button("Confirmar cancelación");

        Button botonVolver
                = new Button(
                        "Volver",
                        evento -> dialogo.close()
                );

        botonConfirmar.addClickListener(evento -> {

            try {

                contratoClient.cancelar(
                        contrato.getCodigo()
                );

                dialogo.close();
                cargarContratos();

                Notification.show(
                        "Contrato cancelado correctamente."
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
                        botonVolver
                );

        VerticalLayout contenido
                = new VerticalLayout(
                        mensaje,
                        botones
                );

        dialogo.add(contenido);
        dialogo.open();
    }

    /*
     * Abre un formulario con los datos actuales
     * de un contrato activo y no cumplido.
     */
    private void abrirFormularioEdicion(
            ContratoVigilanciaDTO contrato) {

        List<VigilanteDTO> vigilantesActivos;
        List<SucursalDTO> sucursalesActivas;

        try {

            vigilantesActivos = vigilanteClient.listar(
                    true,
                    null
            );

            sucursalesActivas = sucursalClient.listar(
                    true,
                    null
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);
            return;

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se pueden consultar los datos necesarios."
            );
            return;
        }

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Modificar "
                        + contrato.getCodigo()
        );

        DatePicker campoFecha
                = new DatePicker("Fecha");

        Select<VigilanteDTO> campoVigilante
                = new Select<>();

        Select<SucursalDTO> campoSucursal
                = new Select<>();

        Select<String> campoConArma
                = new Select<>();

        campoFecha.setMin(LocalDate.now());
        campoFecha.setValue(contrato.getFecha());
        campoFecha.setRequiredIndicatorVisible(true);

        campoVigilante.setLabel("Vigilante");
        campoVigilante.setItems(vigilantesActivos);
        campoVigilante.setItemLabelGenerator(vigilante ->
                vigilante.getCodigo()
                        + " - "
                        + vigilante.getNombre()
        );
        campoVigilante.setRequiredIndicatorVisible(true);

        VigilanteDTO vigilanteActual
                = vigilantesActivos.stream()
                        .filter(vigilante
                                -> vigilante.getCodigo() != null
                                && vigilante.getCodigo()
                                        .equalsIgnoreCase(
                                                contrato.getVigilanteCodigo()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

        if (vigilanteActual != null) {
            campoVigilante.setValue(vigilanteActual);
        }

        campoSucursal.setLabel("Sucursal");
        campoSucursal.setItems(sucursalesActivas);
        campoSucursal.setItemLabelGenerator(sucursal ->
                sucursal.getCodigo()
                        + " - "
                        + sucursal.getDomicilio()
        );
        campoSucursal.setRequiredIndicatorVisible(true);

        SucursalDTO sucursalActual
                = sucursalesActivas.stream()
                        .filter(sucursal
                                -> sucursal.getCodigo() != null
                                && sucursal.getCodigo()
                                        .equalsIgnoreCase(
                                                contrato.getSucursalCodigo()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

        if (sucursalActual != null) {
            campoSucursal.setValue(sucursalActual);
        }

        campoConArma.setLabel("¿Con arma?");
        campoConArma.setItems("Sí", "No");
        campoConArma.setRequiredIndicatorVisible(true);

        if (contrato.getConArma() != null) {

            campoConArma.setValue(
                    Boolean.TRUE.equals(
                            contrato.getConArma()
                    )
                            ? "Sí"
                            : "No"
            );
        }

        campoFecha.setWidthFull();
        campoVigilante.setWidthFull();
        campoSucursal.setWidthFull();
        campoConArma.setWidthFull();

        Button botonGuardar
                = new Button("Guardar cambios");

        Button botonCancelar
                = new Button(
                        "Cancelar",
                        evento -> dialogo.close()
                );

        botonGuardar.addClickListener(evento -> {

            boolean fechaInvalida
                    = campoFecha.getValue() == null
                    || campoFecha.getValue()
                            .isBefore(LocalDate.now());

            boolean vigilanteVacio
                    = campoVigilante.getValue() == null;

            boolean sucursalVacia
                    = campoSucursal.getValue() == null;

            boolean armaVacia
                    = campoConArma.getValue() == null;

            campoFecha.setInvalid(fechaInvalida);
            campoFecha.setErrorMessage(
                    "La fecha debe ser de hoy o futura."
            );

            campoVigilante.setInvalid(vigilanteVacio);
            campoVigilante.setErrorMessage(
                    "Debe seleccionar un vigilante."
            );

            campoSucursal.setInvalid(sucursalVacia);
            campoSucursal.setErrorMessage(
                    "Debe seleccionar una sucursal."
            );

            campoConArma.setInvalid(armaVacia);
            campoConArma.setErrorMessage(
                    "Debe indicar si el contrato requiere arma."
            );

            if (fechaInvalida
                    || vigilanteVacio
                    || sucursalVacia
                    || armaVacia) {

                return;
            }

            ContratoVigilanciaDTO datos
                    = new ContratoVigilanciaDTO(
                            campoFecha.getValue(),
                            "Sí".equals(
                                    campoConArma.getValue()
                            ),
                            campoVigilante.getValue().getCodigo(),
                            campoSucursal.getValue().getCodigo()
                    );

            try {

                contratoClient.actualizar(
                        contrato.getCodigo(),
                        datos
                );

                dialogo.close();
                cargarContratos();

                Notification.show(
                        "Contrato modificado correctamente."
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
                        campoFecha,
                        campoVigilante,
                        campoSucursal,
                        campoConArma,
                        botones
                );

        dialogo.add(formulario);
        dialogo.open();
    }

    /*
     * Abre un formulario y permite seleccionar
     * solamente vigilantes y sucursales activos.
     */
    private void abrirFormularioNuevo() {

        List<VigilanteDTO> vigilantesActivos;
        List<SucursalDTO> sucursalesActivas;

        try {

            vigilantesActivos = vigilanteClient.listar(
                    true,
                    null
            );

            sucursalesActivas = sucursalClient.listar(
                    true,
                    null
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);
            return;

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se pueden consultar los datos necesarios."
            );
            return;
        }

        if (vigilantesActivos.isEmpty()) {

            Notification.show(
                    "No existen vigilantes activos."
            );
            return;
        }

        if (sucursalesActivas.isEmpty()) {

            Notification.show(
                    "No existen sucursales activas."
            );
            return;
        }

        Dialog dialogo = new Dialog();

        dialogo.setHeaderTitle(
                "Nuevo contrato de vigilancia"
        );

        DatePicker campoFecha
                = new DatePicker("Fecha");

        Select<VigilanteDTO> campoVigilante
                = new Select<>();

        Select<SucursalDTO> campoSucursal
                = new Select<>();

        Select<String> campoConArma
                = new Select<>();

        campoFecha.setMin(LocalDate.now());
        campoFecha.setRequiredIndicatorVisible(true);

        campoVigilante.setLabel("Vigilante");
        campoVigilante.setItems(vigilantesActivos);
        campoVigilante.setItemLabelGenerator(vigilante ->
                vigilante.getCodigo()
                        + " - "
                        + vigilante.getNombre()
        );
        campoVigilante.setRequiredIndicatorVisible(true);

        campoSucursal.setLabel("Sucursal");
        campoSucursal.setItems(sucursalesActivas);
        campoSucursal.setItemLabelGenerator(sucursal ->
                sucursal.getCodigo()
                        + " - "
                        + sucursal.getDomicilio()
        );
        campoSucursal.setRequiredIndicatorVisible(true);

        campoConArma.setLabel("¿Con arma?");
        campoConArma.setItems("Sí", "No");
        campoConArma.setRequiredIndicatorVisible(true);

        campoFecha.setWidthFull();
        campoVigilante.setWidthFull();
        campoSucursal.setWidthFull();
        campoConArma.setWidthFull();

        Button botonGuardar
                = new Button("Guardar");

        Button botonCancelar
                = new Button(
                        "Cancelar",
                        evento -> dialogo.close()
                );

        botonGuardar.addClickListener(evento -> {

            boolean fechaInvalida
                    = campoFecha.getValue() == null
                    || campoFecha.getValue()
                            .isBefore(LocalDate.now());

            boolean vigilanteVacio
                    = campoVigilante.getValue() == null;

            boolean sucursalVacia
                    = campoSucursal.getValue() == null;

            boolean armaVacia
                    = campoConArma.getValue() == null;

            campoFecha.setInvalid(fechaInvalida);
            campoFecha.setErrorMessage(
                    "La fecha debe ser de hoy o futura."
            );

            campoVigilante.setInvalid(vigilanteVacio);
            campoVigilante.setErrorMessage(
                    "Debe seleccionar un vigilante."
            );

            campoSucursal.setInvalid(sucursalVacia);
            campoSucursal.setErrorMessage(
                    "Debe seleccionar una sucursal."
            );

            campoConArma.setInvalid(armaVacia);
            campoConArma.setErrorMessage(
                    "Debe indicar si el contrato requiere arma."
            );

            if (fechaInvalida
                    || vigilanteVacio
                    || sucursalVacia
                    || armaVacia) {

                return;
            }

            ContratoVigilanciaDTO datos
                    = new ContratoVigilanciaDTO(
                            campoFecha.getValue(),
                            "Sí".equals(
                                    campoConArma.getValue()
                            ),
                            campoVigilante.getValue().getCodigo(),
                            campoSucursal.getValue().getCodigo()
                    );

            try {

                contratoClient.crear(datos);

                dialogo.close();
                cargarContratos();

                Notification.show(
                        "Contrato creado correctamente."
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
                        campoFecha,
                        campoVigilante,
                        campoSucursal,
                        campoConArma,
                        botones
                );

        dialogo.add(formulario);
        dialogo.open();
    }

    /*
     * Convierte los estados HTTP del backend
     * en mensajes comprensibles para el usuario.
     */
    private void mostrarErrorOperacion(
            RestClientResponseException excepcion) {
        Notification.show(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    /*
     * Convierte la opción seleccionada al
     * valor Boolean que espera el backend.
     */
    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {

            case "Activos" -> true;
            case "Inactivos" -> false;
            default -> null;
        };
    }

    /*
     * Consulta el backend utilizando
     * los filtros seleccionados.
     */
    private void cargarContratos() {

        try {

            String tipoBusqueda = filtroTipoBusqueda.getValue();
            Boolean estado = obtenerEstado();

            List<ContratoVigilanciaDTO> contratos;

            if ("Por vigilante".equals(tipoBusqueda)) {

                VigilanteDTO vigilante
                        = filtroVigilanteExacto.getValue();

                if (vigilante == null) {
                    Notification.show("Seleccione un vigilante.");
                    return;
                }

                contratos = contratoClient.listarPorVigilante(
                        vigilante.getCodigo()
                );

            } else if ("Por sucursal".equals(tipoBusqueda)) {

                SucursalDTO sucursal
                        = filtroSucursalExacta.getValue();

                if (sucursal == null) {
                    Notification.show("Seleccione una sucursal.");
                    return;
                }

                contratos = contratoClient.listarPorSucursal(
                        sucursal.getCodigo()
                );

            } else {

                contratos = contratoClient.listar(
                        estado,
                        filtroVigilante.getValue()
                );
            }

            /*
             * Las búsquedas exactas devuelven todo el historial.
             * El estado se combina localmente cuando fue seleccionado.
             */
            if (!"General".equals(tipoBusqueda) && estado != null) {

                contratos = contratos.stream()
                        .filter(contrato
                                -> estado.equals(contrato.getActivo()))
                        .toList();
            }

            tabla.setItems(contratos);

            if ("General".equals(tipoBusqueda)
                    && contratos.isEmpty()) {

                String nombreBuscado
                        = filtroVigilante.getValue();

                if (nombreBuscado != null
                        && !nombreBuscado.isBlank()) {

                    Notification.show(
                            "No se encontraron contratos para un vigilante cuyo nombre coincida con: "
                                    + nombreBuscado.trim()
                                    + "."
                    );

                } else if (estado != null) {

                    Notification.show(
                            "No existen contratos con el estado seleccionado."
                    );

                } else {

                    Notification.show(
                            "No existen contratos de vigilancia cargados."
                    );
                }

            } else if (!"General".equals(tipoBusqueda)
                    && contratos.isEmpty()) {

                String recurso = "Por vigilante".equals(tipoBusqueda)
                        ? "El vigilante seleccionado"
                        : "La sucursal seleccionada";

                String aclaracionEstado = estado != null
                        ? " con el estado seleccionado"
                        : "";

                Notification.show(
                        recurso
                                + " no tiene contratos de vigilancia"
                                + aclaracionEstado
                                + "."
                );
            }

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

        } catch (IllegalArgumentException excepcion) {

            Notification.show(excepcion.getMessage());
        }
    }

    /*
     * Carga vigilantes y sucursales existentes para evitar
     * que el usuario tenga que escribir códigos manualmente.
     */
    private void cargarOpcionesFiltros() {

        try {

            filtroVigilanteExacto.setItems(
                    vigilanteClient.listar(null, null)
            );

            filtroSucursalExacta.setItems(
                    sucursalClient.listar(null, null)
            );

        } catch (RestClientResponseException excepcion) {

            Notification.show(
                    "No se pudieron cargar vigilantes y sucursales."
            );

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se puede conectar con el backend."
            );
        }
    }

    private boolean puedeEscribir() {
        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    /*
     * Carga los datos cada vez que
     * se ingresa en esta dirección.
     */
    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarOpcionesFiltros();
        cargarContratos();
    }
}
