package prog2.policia_frontend.views;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.button.Button;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import prog2.policia_frontend.clients.AsaltanteClient;
import prog2.policia_frontend.clients.AsaltoClient;
import prog2.policia_frontend.clients.SucursalClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.AsaltanteDTO;
import prog2.policia_frontend.dtos.AsaltoDTO;
import prog2.policia_frontend.dtos.SucursalDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

// Pantalla para administrar asaltos.
@Route(value = "asaltos", layout = MainLayout.class)
@PageTitle("Asaltos | Sistema Policial")
public class AsaltosView extends VerticalLayout implements BeforeEnterObserver {

    private final AsaltoClient asaltoClient;
    private final AsaltanteClient asaltanteClient;
    private final SucursalClient sucursalClient;
    private final PermisosInterfaz permisosInterfaz;
    private final Grid<AsaltoDTO> tabla = new Grid<>(AsaltoDTO.class, false);
    private final PaginadorGrid<AsaltoDTO> paginadorAsaltos = new PaginadorGrid<>(tabla, 10);
    private final Button botonNuevo = new Button("Nuevo asalto");
    private final Select<String> filtroTipoBusqueda = new Select<>();
    private final Select<AsaltanteDTO> filtroAsaltante = new Select<>();
    private final Select<SucursalDTO> filtroSucursal = new Select<>();

    public AsaltosView(AsaltoClient asaltoClient, AsaltanteClient asaltanteClient, SucursalClient sucursalClient, PermisosInterfaz permisosInterfaz) {
        this.asaltoClient = asaltoClient;
        this.asaltanteClient = asaltanteClient;
        this.sucursalClient = sucursalClient;
        this.permisosInterfaz = permisosInterfaz;
        configurarFiltros();
        configurarTabla();
        configurarVista();
    }

    private void configurarFiltros() {
        filtroTipoBusqueda.setLabel("Buscar asaltos");
        filtroTipoBusqueda.setItems("Todos");
        filtroTipoBusqueda.setValue("Todos");
        filtroAsaltante.setLabel("Asaltante");
        filtroAsaltante.setItemLabelGenerator(asaltante -> asaltante.getCodigo() + " - " + asaltante.getNombre());
        filtroAsaltante.setVisible(false);
        filtroSucursal.setLabel("Sucursal");
        filtroSucursal.setItemLabelGenerator(sucursal -> sucursal.getCodigo() + " - " + sucursal.getDomicilio());
        filtroSucursal.setVisible(false);
        filtroTipoBusqueda.addValueChangeListener(evento -> {
            boolean porAsaltante = "Por asaltante".equals(evento.getValue());
            boolean porSucursal = "Por sucursal".equals(evento.getValue());
            filtroAsaltante.setVisible(porAsaltante);
            filtroSucursal.setVisible(porSucursal);
            filtroAsaltante.clear();
            filtroSucursal.clear();
        });
    }

    private void configurarTabla() {
        tabla.addColumn(AsaltoDTO::getCodigo).setHeader("Código").setAutoWidth(true);
        tabla.addColumn(AsaltoDTO::getFecha).setHeader("Fecha").setAutoWidth(true);
        tabla.addColumn(AsaltoDTO::getSucursalCodigo).setHeader("Sucursal").setAutoWidth(true);
        tabla.addColumn(asalto -> {
            List<String> codigos = asalto.getAsaltantesCodigos();
            return codigos == null || codigos.isEmpty() ? "Sin participantes" : String.join(", ", codigos);
        }).setHeader("Asaltantes").setAutoWidth(true).setFlexGrow(1);
        if (permisosInterfaz.puedeConsultarAsaltantes()) {
            tabla.addComponentColumn(asalto -> new Button("Ver nombres", evento -> mostrarAsaltantes(asalto))).setHeader("Participantes").setAutoWidth(true);
        }
        tabla.addComponentColumn(asalto -> {
            if (!permisosInterfaz.puedeModificarAsaltos()) {
                return new Span("Solo lectura");
            }
            Button botonModificar = new Button("Modificar");
            botonModificar.addClickListener(evento -> abrirFormulario(asalto));
            return botonModificar;
        }).setHeader("Acciones").setAutoWidth(true);
        tabla.setSizeFull();
    }

    private void mostrarAsaltantes(AsaltoDTO asalto) {
        try {
            List<String> codigos = asalto.getAsaltantesCodigos();
            Dialog dialogo = new Dialog();
            dialogo.setHeaderTitle("Asaltantes de " + asalto.getCodigo());
            dialogo.setWidth("420px");
            VerticalLayout contenido = new VerticalLayout();
            contenido.setPadding(false);
            if (codigos == null || codigos.isEmpty()) {
                contenido.add(new Span("Sin participantes."));
            } else {
                List<AsaltanteDTO> asaltantes = asaltanteClient.listar(null, null);
                for (String codigo : codigos) {
                    String nombre = null;
                    for (AsaltanteDTO asaltante : asaltantes) {
                        if (codigo.equalsIgnoreCase(asaltante.getCodigo())) {
                            nombre = asaltante.getNombre();
                            break;
                        }
                    }
                    String detalle = nombre == null || nombre.isBlank() ? "Nombre no disponible" : nombre;
                    contenido.add(new Span(detalle + " (" + codigo + ")"));
                }
            }
            contenido.add(new Button("Cerrar", evento -> dialogo.close()));
            dialogo.add(contenido);
            dialogo.open();
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pudieron cargar los asaltantes.");
        }
    }

    private void configurarVista() {
        H2 titulo = new H2("Asaltos");
        Button botonActualizar = new Button("Actualizar", evento -> cargarAsaltos());
        Button botonBuscar = new Button("Buscar", evento -> cargarAsaltos());
        Button botonLimpiar = new Button(
                "Limpiar",
                evento -> {
                    filtroTipoBusqueda.setValue("Todos");
                    filtroAsaltante.clear();
                    filtroSucursal.clear();
                    cargarAsaltos();
                }
        );
        botonNuevo.addClickListener(evento -> abrirFormulario(null));
        botonNuevo.setVisible(permisosInterfaz.puedeModificarAsaltos());
        setSizeFull();
        HorizontalLayout filtros = new HorizontalLayout(filtroTipoBusqueda, filtroAsaltante, filtroSucursal, botonBuscar, botonLimpiar);
        filtros.setAlignItems(HorizontalLayout.Alignment.END);
        add(titulo, new HorizontalLayout(botonNuevo, botonActualizar), filtros, tabla, paginadorAsaltos.getControles());
        expand(tabla);
    }

    private void abrirFormulario(AsaltoDTO asalto) {
        boolean esCreacion = asalto == null;
        List<AsaltanteDTO> asaltantesActivos;
        List<SucursalDTO> sucursalesActivas;
        try {
            asaltantesActivos = asaltanteClient.listar(true, null);
            sucursalesActivas = sucursalClient.listar(true, null);
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar los datos necesarios.");
            return;
        }
        if (sucursalesActivas.isEmpty()) {
            NotificacionUtils.mostrarInformacion("No existen sucursales activas.");
            return;
        }
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(esCreacion ? "Nuevo asalto" : "Modificar " + asalto.getCodigo());
        DatePicker campoFecha = new DatePicker("Fecha");
        Select<SucursalDTO> campoSucursal = new Select<>();
        MultiSelectComboBox<AsaltanteDTO> campoAsaltantes = new MultiSelectComboBox<>("Asaltantes");
        campoFecha.setMax(LocalDate.now());
        campoFecha.setRequiredIndicatorVisible(true);
        campoSucursal.setLabel("Sucursal");
        campoSucursal.setItems(sucursalesActivas);
        campoSucursal.setItemLabelGenerator(sucursal -> sucursal.getCodigo() + " - " + sucursal.getDomicilio());
        campoSucursal.setRequiredIndicatorVisible(true);
        campoAsaltantes.setItems(asaltantesActivos);
        campoAsaltantes.setItemLabelGenerator(asaltante -> asaltante.getCodigo() + " - " + asaltante.getNombre());
        campoAsaltantes.setPlaceholder("Puede quedar sin participantes");
        Set<String> participantesActivosOriginales = new HashSet<>();
        if (!esCreacion) {
            campoFecha.setValue(asalto.getFecha());
            campoFecha.setEnabled(false);
            SucursalDTO sucursalActual = null;
            for (SucursalDTO sucursal : sucursalesActivas) {
                if (sucursal.getCodigo() != null && sucursal.getCodigo().equalsIgnoreCase(asalto.getSucursalCodigo())) {
                    sucursalActual = sucursal;
                    break;
                }
            }
            if (sucursalActual != null) {
                campoSucursal.setValue(sucursalActual);
            }
            Set<AsaltanteDTO> seleccionActual = new HashSet<>();
            List<String> codigosActuales = asalto.getAsaltantesCodigos();
            if (codigosActuales != null) {
                for (AsaltanteDTO candidato : asaltantesActivos) {
                    boolean participa = false;
                    for (String codigo : codigosActuales) {
                        if (codigo.equalsIgnoreCase(candidato.getCodigo())) {
                            participa = true;
                            break;
                        }
                    }
                    if (participa) {
                        seleccionActual.add(candidato);
                        participantesActivosOriginales.add(candidato.getCodigo());
                    }
                }
            }
            campoAsaltantes.select(seleccionActual);
        }
        campoFecha.setWidthFull();
        campoSucursal.setWidthFull();
        campoAsaltantes.setWidthFull();
        Button botonGuardar = new Button(esCreacion ? "Guardar" : "Guardar cambios");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());
        botonGuardar.addClickListener(evento -> {
            boolean fechaInvalida = campoFecha.getValue() == null || campoFecha.getValue().isAfter(LocalDate.now());
            boolean sucursalVacia = campoSucursal.getValue() == null;
            campoFecha.setInvalid(fechaInvalida);
            campoFecha.setErrorMessage("La fecha debe ser de hoy o pasada.");
            campoSucursal.setInvalid(sucursalVacia);
            campoSucursal.setErrorMessage("Debe seleccionar una sucursal.");
            if (fechaInvalida || sucursalVacia) {
                return;
            }
            List<String> codigosSeleccionados = new java.util.ArrayList<>();
            for (AsaltanteDTO seleccionado : campoAsaltantes.getSelectedItems()) {
                codigosSeleccionados.add(seleccionado.getCodigo());
            }
            AsaltoDTO datos = new AsaltoDTO();
            datos.setSucursalCodigo(campoSucursal.getValue().getCodigo());
            if (esCreacion) {
                datos.setFecha(campoFecha.getValue());
                datos.setAsaltantesCodigos(codigosSeleccionados);
            } else {
                Set<String> seleccionNueva = new HashSet<>(codigosSeleccionados);
                boolean participantesModificados = !seleccionNueva.equals(participantesActivosOriginales);
                datos.setAsaltantesCodigos(participantesModificados ? codigosSeleccionados : null);
            }
            try {
                if (esCreacion) {
                    asaltoClient.crear(datos);
                } else {
                    asaltoClient.actualizar(asalto.getCodigo(), datos);
                }
                dialogo.close();
                cargarAsaltos();
                NotificacionUtils.mostrarExito(esCreacion ? "Asalto creado correctamente." : "Asalto modificado correctamente.");
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });
        dialogo.add(new VerticalLayout(campoFecha, campoSucursal, campoAsaltantes, new HorizontalLayout(botonGuardar, botonCancelar)));
        dialogo.open();
    }

    private void cargarAsaltos() {
        try {
            String tipoBusqueda = filtroTipoBusqueda.getValue();
            List<AsaltoDTO> asaltos;
            if ("Por asaltante".equals(tipoBusqueda)) {
                AsaltanteDTO asaltante = filtroAsaltante.getValue();
                if (asaltante == null) {
                    NotificacionUtils.mostrarInformacion("Seleccione un asaltante.");
                    return;
                }
                asaltos = asaltoClient.listarPorAsaltante(asaltante.getCodigo());
            } else if ("Por sucursal".equals(tipoBusqueda)) {
                SucursalDTO sucursal = filtroSucursal.getValue();
                if (sucursal == null) {
                    NotificacionUtils.mostrarInformacion("Seleccione una sucursal.");
                    return;
                }
                asaltos = asaltoClient.listarPorSucursal(sucursal.getCodigo());
            } else {
                asaltos = asaltoClient.listar();
            }
            paginadorAsaltos.setItems(asaltos);
            if (!"Todos".equals(tipoBusqueda) && asaltos.isEmpty()) {
                String recurso = "Por asaltante".equals(tipoBusqueda)
                        ? "El asaltante seleccionado"
                        : "La sucursal seleccionada";
                NotificacionUtils.mostrarInformacion(recurso + " no tiene asaltos registrados.");
            }
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalStateException excepcion) {
            NotificacionUtils.mostrarInformacion("Debe iniciar sesión.");
        } catch (IllegalArgumentException excepcion) {
            NotificacionUtils.mostrarInformacion(excepcion.getMessage());
        }
    }

    private void cargarOpcionesFiltros() {
        List<String> opciones = new java.util.ArrayList<>();
        opciones.add("Todos");
        if (permisosInterfaz.puedeConsultarAsaltantes()) {
            try {
                filtroAsaltante.setItems(asaltanteClient.listar(null, null));
                opciones.add("Por asaltante");
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se pudieron cargar los asaltantes.");
            }
        }
        if (permisosInterfaz.puedeConsultarSucursales()) {
            try {
                filtroSucursal.setItems(sucursalClient.listar(null, null));
                opciones.add("Por sucursal");
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se pudieron cargar las sucursales.");
            }
        }
        filtroTipoBusqueda.setItems(opciones);
        filtroTipoBusqueda.setValue("Todos");
    }

    private void mostrarErrorOperacion(RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarOpcionesFiltros();
        cargarAsaltos();
    }
}
