package prog2.policia_frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import prog2.policia_frontend.utils.NotificacionUtils;
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
import prog2.policia_frontend.clients.SucursalClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.SucursalDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.clients.EntidadBancariaClient;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.NumberField;
import java.util.List;
import java.util.Map;
import prog2.policia_frontend.dtos.EntidadBancariaDTO;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;

/*
 * Pantalla utilizada para administrar sucursales.
 */
@Route(
        value = "sucursales",
        layout = MainLayout.class
)
@PageTitle("Sucursales | Sistema Policial")
public class SucursalesView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final SucursalClient sucursalClient;
    private final SesionUsuario sesionUsuario;
       /*
 * Se utiliza para consultar las entidades
 * bancarias activas del formulario.
 */ 
    
    private final EntidadBancariaClient entidadClient;

    private final Grid<SucursalDTO> tabla
            = new Grid<>(SucursalDTO.class, false);

    private final PaginadorGrid<SucursalDTO> paginadorSucursales
            = new PaginadorGrid<>(tabla, 10);

    private final TextField filtroDomicilio
            = new TextField("Buscar por domicilio");

    private final Select<String> filtroTipoBusqueda
            = new Select<>();

    private final Select<EntidadBancariaDTO> filtroEntidad
            = new Select<>();

    private final Select<String> filtroEstado
            = new Select<>();

    private final Button botonBuscar
            = new Button("Buscar");

    private final Button botonLimpiar
            = new Button("Limpiar");
    
    private final Button botonNuevaSucursal
        = new Button("Nueva sucursal");

    private final Button botonVolverEntidades
            = new Button("Volver a entidades bancarias");

    private List<EntidadBancariaDTO> entidadesDisponibles = List.of();

   public SucursalesView(
        SucursalClient sucursalClient,
        EntidadBancariaClient entidadClient,
        SesionUsuario sesionUsuario) {

    this.sucursalClient = sucursalClient;
    this.entidadClient = entidadClient;
    this.sesionUsuario = sesionUsuario;

    botonVolverEntidades.setVisible(false);
    botonVolverEntidades.addClickListener(
            evento -> UI.getCurrent().navigate(
                    EntidadesBancariasView.class
            )
    );

    configurarFiltros();
    configurarVista();
    configurarTabla();
}

    /*
     * Configura la búsqueda por domicilio y estado.
     */
    private void configurarFiltros() {

        filtroTipoBusqueda.setLabel("Tipo de búsqueda");
        filtroTipoBusqueda.setItems(
                "General",
                "Por entidad bancaria"
        );
        filtroTipoBusqueda.setValue("General");

        filtroDomicilio.setPlaceholder(
                "Ingrese una coincidencia"
        );

        filtroDomicilio.setClearButtonVisible(true);

        filtroEntidad.setLabel("Entidad bancaria");
        filtroEntidad.setItemLabelGenerator(entidad ->
                entidad.getCodigo()
                        + " - "
                        + entidad.getNombre()
        );
        filtroEntidad.setVisible(false);

        filtroTipoBusqueda.addValueChangeListener(evento -> {

            boolean porEntidad
                    = "Por entidad bancaria".equals(evento.getValue());

            filtroDomicilio.setVisible(!porEntidad);
            filtroEntidad.setVisible(porEntidad);

            filtroDomicilio.clear();
            filtroEntidad.clear();
        });

        filtroEstado.setLabel("Estado");
        filtroEstado.setItems(
                "Todas",
                "Activas",
                "Inactivas"
        );
        filtroEstado.setValue("Todas");

        botonBuscar.addClickListener(
                evento -> cargarSucursales()
        );

        botonLimpiar.addClickListener(evento -> {

            filtroDomicilio.clear();
            filtroTipoBusqueda.setValue("General");
            filtroEntidad.clear();
            filtroEstado.setValue("Todas");

            cargarSucursales();
        });
    }

    /*
     * Organiza los componentes de la pantalla.
     */
    private void configurarVista() {

        H2 titulo = new H2(
                "Administración de sucursales"
        );

        HorizontalLayout filtros
                = new HorizontalLayout(
                        filtroTipoBusqueda,
                        filtroDomicilio,
                        filtroEntidad,
                        filtroEstado,
                        botonBuscar,
                        botonLimpiar
                );

        filtros.setAlignItems(
                HorizontalLayout.Alignment.END
        );
        
        botonNuevaSucursal.addClickListener(
        evento -> abrirFormularioNuevo()
);
        botonNuevaSucursal.setVisible(puedeEscribir());
        setSizeFull();

        add(
                 titulo,
        botonVolverEntidades,
        botonNuevaSucursal,
        filtros,
        tabla,
        paginadorSucursales.getControles()
        );

        expand(tabla);
    }

    /*
     * Define la información visible.
     */
    private void configurarTabla() {

        tabla.addColumn(
                SucursalDTO::getCodigo
        ).setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(
                SucursalDTO::getDomicilio
        ).setHeader("Domicilio")
                .setAutoWidth(true)
                .setFlexGrow(1);

        tabla.addColumn(
                SucursalDTO::getCantEmpleados
        ).setHeader("Empleados")
                .setAutoWidth(true);

        tabla.addColumn(
                SucursalDTO::getEntidadBancariaCodigo
        ).setHeader("Entidad bancaria")
                .setAutoWidth(true);

        tabla.addColumn(sucursal ->
                Boolean.TRUE.equals(
                        sucursal.getActivo()
                )
                        ? "Activa"
                        : "Inactiva"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addColumn(sucursal ->
                sucursal.getMotivoCierre() != null
                        ? sucursal.getMotivoCierre()
                        : "-"
        ).setHeader("Motivo de cierre")
                .setAutoWidth(true);
        /*
         * Muestra las acciones disponibles
         * según el estado de cada sucursal.
         */
        tabla.addComponentColumn(sucursal -> {

            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            boolean estaActiva
                    = Boolean.TRUE.equals(
                            sucursal.getActivo()
                    );

            Button botonModificar
                    = new Button("Modificar");

            botonModificar.setEnabled(estaActiva);

            if (estaActiva) {

                botonModificar.addClickListener(
                        evento
                                -> abrirFormularioEdicion(sucursal)
                );

            } else {

                botonModificar.setTooltipText(
                        "Debe reactivar la sucursal antes de modificarla."
                );
            }

            Button botonEstado = new Button();

            if (estaActiva) {

                /*
                 * Una sucursal activa puede
                 * solicitar su baja lógica.
                 */
                botonEstado.setText("Dar de baja");

                botonEstado.addClickListener(
                        evento
                                -> abrirDialogoBaja(sucursal)
                );

            } else if ("CIERRE_DEFINITIVO".equals(
                    sucursal.getMotivoCierre()
            )) {

                /*
                 * Un cierre definitivo no permite
                 * volver a activar la sucursal.
                 */
                botonEstado.setText("No reactivable");
                botonEstado.setEnabled(false);

                botonEstado.setTooltipText(
                        "La sucursal fue cerrada definitivamente."
                );

            } else {

                /*
                 * Una sucursal cerrada temporalmente
                 * o por mudanza puede reactivarse.
                 */
                botonEstado.setText("Reactivar");

                botonEstado.addClickListener(
                        evento
                                -> reactivarSucursal(sucursal)
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
 * de una sucursal activa.
 */
private void abrirFormularioEdicion(
        SucursalDTO sucursal) {

    List<EntidadBancariaDTO> entidadesActivas;

    try {

        entidadesActivas = entidadClient.listar(
                true,
                null,
                null
        );

    } catch (RestClientResponseException excepcion) {

        mostrarErrorOperacion(excepcion);
        return;

    } catch (ResourceAccessException excepcion) {

        NotificacionUtils.mostrarInformacion(
                "No se pueden consultar las entidades bancarias."
        );
        return;
    }

    Dialog dialogo = new Dialog();

    dialogo.setHeaderTitle(
            "Modificar "
                    + sucursal.getCodigo()
    );

    TextField campoDomicilio
            = new TextField("Domicilio");

    NumberField campoEmpleados
            = new NumberField(
                    "Cantidad de empleados"
            );

    Select<EntidadBancariaDTO> campoEntidad
            = new Select<>();

    campoDomicilio.setValue(
            sucursal.getDomicilio() != null
                    ? sucursal.getDomicilio()
                    : ""
    );

    if (sucursal.getCantEmpleados() != null) {

        campoEmpleados.setValue(
                sucursal.getCantEmpleados()
                        .doubleValue()
        );
    }

    campoEntidad.setLabel("Entidad bancaria");

    campoEntidad.setItemLabelGenerator(entidad ->
            entidad.getCodigo()
                    + " - "
                    + entidad.getNombre()
    );

    campoEntidad.setItems(entidadesActivas);

    EntidadBancariaDTO entidadActual
            = entidadesActivas.stream()
                    .filter(entidad
                            -> entidad.getCodigo() != null
                            && entidad.getCodigo()
                                    .equalsIgnoreCase(
                                            sucursal
                                                    .getEntidadBancariaCodigo()
                                    )
                    )
                    .findFirst()
                    .orElse(null);

    if (entidadActual != null) {
        campoEntidad.setValue(entidadActual);
    }

    campoDomicilio.setRequiredIndicatorVisible(true);
    campoEmpleados.setRequiredIndicatorVisible(true);
    campoEntidad.setRequiredIndicatorVisible(true);

    campoEmpleados.setMin(1);
    campoEmpleados.setStep(1);
    campoEmpleados.setStepButtonsVisible(true);

    campoDomicilio.setWidthFull();
    campoEmpleados.setWidthFull();
    campoEntidad.setWidthFull();

    Button botonGuardar
            = new Button("Guardar cambios");

    Button botonCancelar
            = new Button(
                    "Cancelar",
                    evento -> dialogo.close()
            );

    botonGuardar.addClickListener(evento -> {

        boolean domicilioVacio
                = campoDomicilio.getValue().isBlank();

        Double cantidadIngresada
                = campoEmpleados.getValue();

        boolean cantidadInvalida
                = cantidadIngresada == null
                || cantidadIngresada < 1
                || cantidadIngresada
                        != Math.floor(cantidadIngresada);

        boolean entidadVacia
                = campoEntidad.getValue() == null;

        campoDomicilio.setInvalid(domicilioVacio);
        campoDomicilio.setErrorMessage(
                "El domicilio es obligatorio."
        );

        campoEmpleados.setInvalid(cantidadInvalida);
        campoEmpleados.setErrorMessage(
                "Debe ingresar un número entero mayor que cero."
        );

        campoEntidad.setInvalid(entidadVacia);
        campoEntidad.setErrorMessage(
                "Debe seleccionar una entidad bancaria."
        );

        if (domicilioVacio
                || cantidadInvalida
                || entidadVacia) {

            return;
        }

        SucursalDTO datos
                = new SucursalDTO(
                        campoDomicilio.getValue().trim(),
                        cantidadIngresada.intValue(),
                        campoEntidad.getValue().getCodigo()
                );

        try {

            sucursalClient.actualizar(
                    sucursal.getCodigo(),
                    datos
            );

            dialogo.close();
            cargarSucursales();

            NotificacionUtils.mostrarExito(
                    "Sucursal modificada correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            NotificacionUtils.mostrarInformacion(
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
                    campoDomicilio,
                    campoEmpleados,
                    campoEntidad,
                    botones
            );

    dialogo.add(formulario);
    dialogo.open();
}
        /*
 * Abre el formulario para registrar
 * una nueva sucursal.
 */
private void abrirFormularioNuevo() {

    List<EntidadBancariaDTO> entidadesActivas;

    try {

        entidadesActivas = entidadClient.listar(
                true,
                null,
                null
        );

    } catch (RestClientResponseException excepcion) {

        mostrarErrorOperacion(excepcion);
        return;

    } catch (ResourceAccessException excepcion) {

        NotificacionUtils.mostrarInformacion(
                "No se pueden consultar las entidades bancarias."
        );
        return;
    }

    if (entidadesActivas.isEmpty()) {

        NotificacionUtils.mostrarInformacion(
                "No existen entidades bancarias activas."
        );
        return;
    }

    Dialog dialogo = new Dialog();

    dialogo.setHeaderTitle(
            "Nueva sucursal"
    );

    TextField campoDomicilio
            = new TextField("Domicilio");

    NumberField campoEmpleados
            = new NumberField(
                    "Cantidad de empleados"
            );

    Select<EntidadBancariaDTO> campoEntidad
            = new Select<>();

    campoEntidad.setLabel("Entidad bancaria");

    /*
     * Muestra código y nombre para distinguir
     * correctamente cada entidad.
     */
    campoEntidad.setItemLabelGenerator(entidad ->
            entidad.getCodigo()
                    + " - "
                    + entidad.getNombre()
    );

    campoEntidad.setItems(entidadesActivas);

    campoDomicilio.setRequiredIndicatorVisible(true);
    campoEmpleados.setRequiredIndicatorVisible(true);
    campoEntidad.setRequiredIndicatorVisible(true);

    campoEmpleados.setMin(1);
    campoEmpleados.setStep(1);
    campoEmpleados.setStepButtonsVisible(true);

    campoDomicilio.setWidthFull();
    campoEmpleados.setWidthFull();
    campoEntidad.setWidthFull();

    Button botonGuardar
            = new Button("Guardar");

    Button botonCancelar
            = new Button(
                    "Cancelar",
                    evento -> dialogo.close()
            );

    botonGuardar.addClickListener(evento -> {

        boolean domicilioVacio
                = campoDomicilio.getValue().isBlank();

        Double cantidadIngresada
                = campoEmpleados.getValue();

        boolean cantidadInvalida
                = cantidadIngresada == null
                || cantidadIngresada < 1
                || cantidadIngresada
                        != Math.floor(cantidadIngresada);

        boolean entidadVacia
                = campoEntidad.getValue() == null;

        campoDomicilio.setInvalid(domicilioVacio);
        campoDomicilio.setErrorMessage(
                "El domicilio es obligatorio."
        );

        campoEmpleados.setInvalid(cantidadInvalida);
        campoEmpleados.setErrorMessage(
                "Debe ingresar un número entero mayor que cero."
        );

        campoEntidad.setInvalid(entidadVacia);
        campoEntidad.setErrorMessage(
                "Debe seleccionar una entidad bancaria."
        );

        if (domicilioVacio
                || cantidadInvalida
                || entidadVacia) {

            return;
        }

        EntidadBancariaDTO entidadSeleccionada
                = campoEntidad.getValue();

        SucursalDTO datos
                = new SucursalDTO(
                        campoDomicilio.getValue().trim(),
                        cantidadIngresada.intValue(),
                        entidadSeleccionada.getCodigo()
                );

        try {

            sucursalClient.crear(datos);

            dialogo.close();
            cargarSucursales();

            NotificacionUtils.mostrarExito(
                    "Sucursal creada correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            NotificacionUtils.mostrarInformacion(
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
                    campoDomicilio,
                    campoEmpleados,
                    campoEntidad,
                    botones
            );

    dialogo.add(formulario);
    dialogo.open();
}
/*
 * Abre una ventana para seleccionar
 * el motivo de baja de la sucursal.
 */
private void abrirDialogoBaja(
        SucursalDTO sucursal) {

    Dialog dialogo = new Dialog();

    dialogo.setHeaderTitle(
            "Dar de baja "
                    + sucursal.getCodigo()
    );

    Select<String> campoMotivo
            = new Select<>();

    campoMotivo.setLabel(
            "Motivo del cierre"
    );

    campoMotivo.setItems(
            "CIERRE_TEMPORAL",
            "CIERRE_DEFINITIVO",
            "MUDANZA"
    );

    campoMotivo.setRequiredIndicatorVisible(true);
    campoMotivo.setWidthFull();

    Button botonConfirmar
            = new Button("Confirmar baja");

    Button botonCancelar
            = new Button(
                    "Cancelar",
                    evento -> dialogo.close()
            );

    botonConfirmar.addClickListener(evento -> {

        String motivoSeleccionado
                = campoMotivo.getValue();

        boolean motivoVacio
                = motivoSeleccionado == null
                || motivoSeleccionado.isBlank();

        campoMotivo.setInvalid(motivoVacio);
        campoMotivo.setErrorMessage(
                "Debe seleccionar un motivo."
        );

        /*
         * Si no se eligió un motivo,
         * no se envía la baja al backend.
         */
        if (motivoVacio) {
            return;
        }

        try {

            sucursalClient.darDeBaja(
                    sucursal.getCodigo(),
                    motivoSeleccionado
            );

            dialogo.close();
            cargarSucursales();

            NotificacionUtils.mostrarExito(
                    "Sucursal dada de baja correctamente."
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalArgumentException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    excepcion.getMessage()
            );
        }
    });

    HorizontalLayout botones
            = new HorizontalLayout(
                    botonConfirmar,
                    botonCancelar
            );

    VerticalLayout contenido
            = new VerticalLayout(
                    campoMotivo,
                    botones
            );

    dialogo.add(contenido);
    dialogo.open();
}

/*
 * Convierte errores HTTP en mensajes
 * comprensibles para el usuario.
 */
private void mostrarErrorOperacion(
        RestClientResponseException excepcion) {
    NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
}

/*
 * Reactiva una sucursal cerrada,
 * siempre que el backend lo permita.
 */
private void reactivarSucursal(
        SucursalDTO sucursal) {

    try {

        sucursalClient.reactivar(
                sucursal.getCodigo()
        );

        cargarSucursales();

        NotificacionUtils.mostrarExito(
                "Sucursal reactivada correctamente."
        );

    } catch (RestClientResponseException excepcion) {

        mostrarErrorOperacion(excepcion);

    } catch (ResourceAccessException excepcion) {

        NotificacionUtils.mostrarInformacion(
                "No se puede conectar con el backend."
        );

    } catch (IllegalArgumentException excepcion) {

        NotificacionUtils.mostrarInformacion(
                excepcion.getMessage()
        );
    }
}


    @Override
    public void beforeEnter(
            BeforeEnterEvent evento) {

        cargarOpcionesFiltros();
        aplicarFiltroRecibido(evento);
        cargarSucursales();
    }

    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {

            case "Activas" -> true;
            case "Inactivas" -> false;
            default -> null;
        };
    }

    /*
     * Consulta el backend utilizando
     * los filtros seleccionados.
     */
    private void cargarSucursales() {

        try {

            String tipoBusqueda = filtroTipoBusqueda.getValue();
            Boolean estado = obtenerEstado();
            List<SucursalDTO> sucursales;

            if ("Por entidad bancaria".equals(tipoBusqueda)) {

                EntidadBancariaDTO entidad = filtroEntidad.getValue();

                if (entidad == null) {
                    NotificacionUtils.mostrarInformacion(
                            "Seleccione una entidad bancaria."
                    );
                    return;
                }

                sucursales = sucursalClient.listarPorEntidadBancaria(
                        entidad.getCodigo()
                );

                if (estado != null) {
                    sucursales = sucursales.stream()
                            .filter(sucursal
                                    -> estado.equals(sucursal.getActivo()))
                            .toList();
                }

            } else {

                sucursales = sucursalClient.listar(
                        estado,
                        filtroDomicilio.getValue()
                );
            }

            paginadorSucursales.setItems(sucursales);

            if (sucursales.isEmpty()) {

                if ("Por entidad bancaria".equals(tipoBusqueda)) {

                    String aclaracionEstado = estado != null
                            ? " con el estado seleccionado"
                            : "";

                    NotificacionUtils.mostrarInformacion(
                            "La entidad bancaria seleccionada no tiene sucursales"
                                    + aclaracionEstado
                                    + "."
                    );

                } else if (filtroDomicilio.getValue() != null
                        && !filtroDomicilio.getValue().isBlank()) {

                    NotificacionUtils.mostrarInformacion(
                            "No se encontraron sucursales cuyo domicilio coincida con: "
                                    + filtroDomicilio.getValue().trim()
                                    + "."
                    );

                } else if (estado != null) {

                    NotificacionUtils.mostrarInformacion(
                            "No existen sucursales con el estado seleccionado."
                    );
                }
            }

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );

        } catch (IllegalStateException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "Debe iniciar sesión."
            );

        } catch (IllegalArgumentException excepcion) {

            NotificacionUtils.mostrarInformacion(excepcion.getMessage());
        }
    }

    private void cargarOpcionesFiltros() {

        try {

            entidadesDisponibles
                    = entidadClient.listar(null, null, null);

            filtroEntidad.setItems(entidadesDisponibles);

        } catch (RestClientResponseException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se pudieron cargar las entidades bancarias."
            );

        } catch (ResourceAccessException excepcion) {

            NotificacionUtils.mostrarInformacion(
                    "No se puede conectar con el backend."
            );
        }
    }

    private void aplicarFiltroRecibido(BeforeEnterEvent evento) {
        Map<String, List<String>> parametros = evento.getLocation()
                .getQueryParameters()
                .getParameters();

        List<String> valores = parametros.get("entidad");

        if (valores == null || valores.isEmpty()) {
            return;
        }

        String codigoEntidad = valores.get(0);

        EntidadBancariaDTO entidad = entidadesDisponibles.stream()
                .filter(item -> codigoEntidad.equalsIgnoreCase(
                item.getCodigo()))
                .findFirst()
                .orElse(null);

        if (entidad != null) {
            filtroTipoBusqueda.setValue("Por entidad bancaria");
            filtroEntidad.setValue(entidad);
            botonVolverEntidades.setVisible(true);
        }
    }

    private boolean puedeEscribir() {
        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }
}
