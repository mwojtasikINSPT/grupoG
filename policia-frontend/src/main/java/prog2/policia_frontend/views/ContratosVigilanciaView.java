package prog2.policia_frontend.views;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import prog2.policia_frontend.clients.ContratoVigilanciaClient;
import prog2.policia_frontend.clients.SucursalClient;
import prog2.policia_frontend.clients.UsuarioClient;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.ContratoVigilanciaDTO;
import prog2.policia_frontend.dtos.SucursalDTO;
import prog2.policia_frontend.dtos.UsuarioDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.NormalizadorBusqueda;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

// Pantalla para consultar y administrar contratos de vigilancia.
@Route(value = "contratos-vigilancia", layout = MainLayout.class)
@PageTitle("Contratos de vigilancia | Sistema Policial")
public class ContratosVigilanciaView extends VerticalLayout implements BeforeEnterObserver {

    private final ContratoVigilanciaClient contratoClient;
    private final UsuarioClient usuarioClient;
    private final SucursalClient sucursalClient;
    private final PermisosInterfaz permisosInterfaz;

    private final Grid<ContratoVigilanciaDTO> tabla = new Grid<>(ContratoVigilanciaDTO.class, false);
    private final PaginadorGrid<ContratoVigilanciaDTO> paginadorContratos = new PaginadorGrid<>(tabla, 10);
    private final Select<String> criterioBusqueda = new Select<>();
    private final com.vaadin.flow.component.textfield.TextField valorBusqueda = new com.vaadin.flow.component.textfield.TextField("Buscar");
    private List<ContratoVigilanciaDTO> datosBusqueda = List.of();
    private List<UsuarioDTO> vigilantesBusqueda = List.of();
    private final Select<String> filtroEstado = new Select<>();
    private final Button botonNuevoContrato = new Button("Nuevo contrato");
    private String vigilantePendiente;
    private final Span avisoPendientes = new Span();

    public ContratosVigilanciaView(ContratoVigilanciaClient contratoClient, UsuarioClient usuarioClient, SucursalClient sucursalClient, PermisosInterfaz permisosInterfaz) {
        this.contratoClient = contratoClient;
        this.usuarioClient = usuarioClient;
        this.sucursalClient = sucursalClient;
        this.permisosInterfaz = permisosInterfaz;
        configurarFiltros();
        configurarTabla();
        configurarVista();
    }

    // Configura las búsquedas por vigilante, sucursal y estado.
    private void configurarFiltros() {
        configurarBusqueda();
        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todos", "Activos", "Finalizados");
        filtroEstado.setValue("Todos");
    }

    // Define las columnas sin exponer información sensible.
    private void configurarTabla() {
        tabla.addColumn(ContratoVigilanciaDTO::getCodigo).setHeader("Código").setAutoWidth(true);
        tabla.addColumn(ContratoVigilanciaDTO::getFecha).setHeader("Fecha").setAutoWidth(true);
        tabla.addColumn(contrato -> Boolean.TRUE.equals(contrato.getConArma()) ? "Sí" : "No").setHeader("Con arma").setAutoWidth(true);
        tabla.addColumn(ContratoVigilanciaDTO::getVigilanteCodigo).setHeader("Vigilante").setAutoWidth(true);
        tabla.addColumn(ContratoVigilanciaDTO::getSucursalCodigo).setHeader("Sucursal").setAutoWidth(true);
        tabla.addColumn(this::obtenerEstadoVisible).setHeader("Estado").setAutoWidth(true);
        tabla.addColumn(contrato -> contrato.getMotivoBaja() != null ? contrato.getMotivoBaja() : "-").setHeader("Motivo de baja").setAutoWidth(true);
        tabla.addComponentColumn(this::crearBotonesAccion).setHeader("Acciones").setAutoWidth(true);
        tabla.setSizeFull();
    }

    // Solo permite modificar o cancelar contratos activos de hoy o futuros.
    private Component crearBotonesAccion(ContratoVigilanciaDTO contrato) {
        if (!puedeEscribir()) {
            return new Span("Solo lectura");
        }

        Button botonModificar = new Button("Modificar");
        Button botonCancelarContrato = new Button("Dar de baja");

        boolean puedeOperarse = Boolean.TRUE.equals(contrato.getActivo())
                && contrato.getFecha() != null
                && !contrato.getFecha().isBefore(LocalDate.now());

        botonModificar.setEnabled(puedeOperarse);
        botonCancelarContrato.setEnabled(puedeOperarse);

        if (puedeOperarse) {
            botonModificar.addClickListener(evento -> abrirFormulario(contrato));
            botonCancelarContrato.addClickListener(evento -> abrirConfirmacionCancelacion(contrato));
        } else {
            botonModificar.setTooltipText("Los contratos cancelados o cumplidos no pueden modificarse.");
            botonCancelarContrato.setTooltipText("Los contratos cancelados o cumplidos no pueden cancelarse.");
        }

        return new HorizontalLayout(botonModificar, botonCancelarContrato);
    }

    // Organiza los componentes de la pantalla.
    private void configurarVista() {
        H2 titulo = new H2("Contratos de vigilancia");
        HorizontalLayout filtros = crearBarraBusqueda();
        filtros.setAlignItems(Alignment.END);

        botonNuevoContrato.addClickListener(evento -> abrirFormulario(null));
        botonNuevoContrato.setVisible(puedeEscribir());

        setSizeFull();
        avisoPendientes.setVisible(false);
        add(titulo, botonNuevoContrato, filtros, avisoPendientes, tabla, paginadorContratos.getControles());
        expand(tabla);
    }

    // Solicita confirmación antes de cancelar definitivamente un contrato.
    private void abrirConfirmacionCancelacion(ContratoVigilanciaDTO contrato) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Dar de baja " + contrato.getCodigo());
        Paragraph mensaje = new Paragraph("El contrato quedará cancelado y no podrá reactivarse. ¿Desea continuar?");
        Select<String> campoMotivo = new Select<>();
        campoMotivo.setLabel("Motivo de baja");
        campoMotivo.setRequiredIndicatorVisible(true);
        try {
            campoMotivo.setItems(contratoClient.listarMotivosBaja());
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar los motivos de baja.");
            return;
        }
        Button botonConfirmar = new Button("Confirmar baja");
        Button botonVolver = new Button("Volver", evento -> dialogo.close());
        botonConfirmar.addClickListener(evento -> {
            boolean motivoVacio = campoMotivo.getValue() == null || campoMotivo.getValue().isBlank();
            campoMotivo.setInvalid(motivoVacio);
            campoMotivo.setErrorMessage("Debe seleccionar un motivo de baja.");
            if (motivoVacio) {
                return;
            }
            try {
                contratoClient.cancelar(contrato.getCodigo(), campoMotivo.getValue());
                dialogo.close();
                cargarContratos();
                NotificacionUtils.mostrarExito("Contrato dado de baja correctamente.");
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });
        dialogo.add(new VerticalLayout(mensaje, campoMotivo, new HorizontalLayout(botonConfirmar, botonVolver)));
        dialogo.open();
    }

    // Registra o modifica contratos utilizando vigilantes y sucursales activos.
    private void abrirFormulario(ContratoVigilanciaDTO contrato) {
        boolean esCreacion = contrato == null;
        List<SucursalDTO> sucursalesActivas;

        try {
            sucursalesActivas = sucursalClient.listar(true);
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar los datos necesarios.");
            return;
        }

        if (esCreacion && sucursalesActivas.isEmpty()) {
            NotificacionUtils.mostrarInformacion("No existen sucursales activas.");
            return;
        }

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(esCreacion ? "Nuevo contrato de vigilancia" : "Modificar " + contrato.getCodigo());

        DatePicker campoFecha = new DatePicker("Fecha");
        Select<UsuarioDTO> campoVigilante = new Select<>();
        Select<SucursalDTO> campoSucursal = new Select<>();
        Select<String> campoConArma = new Select<>();

        campoFecha.setMin(LocalDate.now());
        campoFecha.setRequiredIndicatorVisible(true);

        campoVigilante.setLabel("Vigilante");
        campoVigilante.setItemLabelGenerator(vigilante -> vigilante.getCodigo() + " - " + vigilante.getNombre());
        campoVigilante.setRequiredIndicatorVisible(true);

        campoSucursal.setLabel("Sucursal");
        campoSucursal.setItems(sucursalesActivas);
        campoSucursal.setItemLabelGenerator(sucursal -> sucursal.getCodigo() + " - " + sucursal.getDomicilio());
        campoSucursal.setRequiredIndicatorVisible(true);

        campoConArma.setLabel("¿Con arma?");
        campoConArma.setItems("Sí", "No");
        campoConArma.setRequiredIndicatorVisible(true);

        if (!esCreacion) {
            campoFecha.setValue(contrato.getFecha());

            for (SucursalDTO sucursal : sucursalesActivas) {
                if (sucursal.getCodigo() != null && sucursal.getCodigo().equalsIgnoreCase(contrato.getSucursalCodigo())) {
                    campoSucursal.setValue(sucursal);
                    break;
                }
            }

            if (contrato.getConArma() != null) {
                campoConArma.setValue(Boolean.TRUE.equals(contrato.getConArma()) ? "Sí" : "No");
            }
        }

        cargarVigilantesDisponibles(campoFecha.getValue(), campoVigilante,
                esCreacion ? null : contrato.getCodigo(), esCreacion ? null : contrato.getVigilanteCodigo());
        campoFecha.addValueChangeListener(evento -> {
            String seleccionado = campoVigilante.getValue() == null ? null : campoVigilante.getValue().getCodigo();
            cargarVigilantesDisponibles(evento.getValue(), campoVigilante,
                    esCreacion ? null : contrato.getCodigo(), seleccionado);
        });

        campoFecha.setWidthFull();
        campoVigilante.setWidthFull();
        campoSucursal.setWidthFull();
        campoConArma.setWidthFull();

        Button botonGuardar = new Button(esCreacion ? "Guardar" : "Guardar cambios");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());

        botonGuardar.addClickListener(evento -> {
            boolean fechaInvalida = campoFecha.getValue() == null || campoFecha.getValue().isBefore(LocalDate.now());
            boolean vigilanteVacio = campoVigilante.getValue() == null;
            boolean sucursalVacia = campoSucursal.getValue() == null;
            boolean armaVacia = campoConArma.getValue() == null;

            campoFecha.setInvalid(fechaInvalida);
            campoFecha.setErrorMessage("La fecha debe ser de hoy o futura.");
            campoVigilante.setInvalid(vigilanteVacio);
            campoVigilante.setErrorMessage("Debe seleccionar un vigilante.");
            campoSucursal.setInvalid(sucursalVacia);
            campoSucursal.setErrorMessage("Debe seleccionar una sucursal.");
            campoConArma.setInvalid(armaVacia);
            campoConArma.setErrorMessage("Debe indicar si el contrato requiere arma.");

            if (fechaInvalida || vigilanteVacio || sucursalVacia || armaVacia) {
                return;
            }

            ContratoVigilanciaDTO datos = new ContratoVigilanciaDTO(campoFecha.getValue(), "Sí".equals(campoConArma.getValue()), campoVigilante.getValue().getCodigo(), campoSucursal.getValue().getCodigo());

            try {
                if (esCreacion) {
                    contratoClient.crear(datos);
                } else {
                    contratoClient.actualizar(contrato.getCodigo(), datos);
                }

                dialogo.close();
                cargarContratos();
                NotificacionUtils.mostrarExito(esCreacion ? "Contrato creado correctamente." : "Contrato modificado correctamente.");

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(new VerticalLayout(campoFecha, campoVigilante, campoSucursal, campoConArma, new HorizontalLayout(botonGuardar, botonCancelar)));
        dialogo.open();
    }

    // Actualiza las opciones al cambiar la fecha y conserva solo selecciones disponibles.
    private void cargarVigilantesDisponibles(LocalDate fecha, Select<UsuarioDTO> selector,
            String contratoCodigo, String codigoSeleccionado) {
        selector.clear();
        selector.setItems(List.of());
        selector.setEnabled(false);
        selector.setHelperText("Seleccione una fecha de hoy o futura.");
        if (fecha == null || fecha.isBefore(LocalDate.now())) {
            return;
        }
        try {
            List<UsuarioDTO> disponibles = contratoClient.listarVigilantesDisponibles(fecha, contratoCodigo);
            selector.setItems(disponibles);
            selector.setEnabled(!disponibles.isEmpty());
            selector.setHelperText(disponibles.isEmpty() ? "No hay vigilantes activos disponibles para esta fecha."
                    : "Solo se muestran vigilantes activos y disponibles para esta fecha.");
            for (UsuarioDTO vigilante : disponibles) {
                if (vigilante.getCodigo().equals(codigoSeleccionado)) {
                    selector.setValue(vigilante);
                    break;
                }
            }
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar los vigilantes disponibles.");
        }
    }

    // Convierte los errores HTTP en mensajes comprensibles.
    private void mostrarErrorOperacion(RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    // Convierte el estado seleccionado al Boolean esperado por el backend.
    private Boolean obtenerEstado() {
        return switch (filtroEstado.getValue()) {
            case "Activos" ->
                true;
            case "Finalizados" ->
                false;
            default ->
                null;
        };
    }

    // Determina el estado visible según actividad, motivo de baja y fecha.
    private String obtenerEstadoVisible(ContratoVigilanciaDTO contrato) {
        if (Boolean.TRUE.equals(contrato.getActivo())) {
            return "ACTIVO";
        }

        if ("CANCELACION".equals(contrato.getMotivoBaja())) {
            return "CANCELADO";
        }

        if ("CIERRE_SUCURSAL".equals(contrato.getMotivoBaja())) {
            return "BAJA POR CIERRE DE SUCURSAL";
        }

        if (contrato.getFecha() != null && contrato.getFecha().isBefore(LocalDate.now())) {
            return "CUMPLIDO";
        }

        return "FINALIZADO";
    }

    // Consulta los contratos aplicando los filtros seleccionados.
    private void cargarContratos() {
        datosBusqueda = List.of();
        aplicarFiltros();
        try {
            datosBusqueda = contratoClient.listar();
            aplicarFiltros();
        } catch (RestClientResponseException excepcion) {
            datosBusqueda = List.of();
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            datosBusqueda = List.of();
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalStateException excepcion) {
            datosBusqueda = List.of();
            NotificacionUtils.mostrarInformacion("Debe iniciar sesión.");
        }
    }

    // Carga vigilantes y sucursales para seleccionar sin escribir códigos.
    private void cargarOpcionesFiltros() {
        vigilantesBusqueda = List.of();
        if (!permisosInterfaz.puedeConsultarUsuarios()) { return; }
        try { vigilantesBusqueda = listarVigilantes(null); }
        catch (RestClientResponseException excepcion) { mostrarErrorOperacion(excepcion); }
        catch (ResourceAccessException excepcion) { NotificacionUtils.mostrarInformacion("No se pudieron cargar los vigilantes."); }
    }

    // Obtiene únicamente los usuarios con rol VIGILANTE.
    private List<UsuarioDTO> listarVigilantes(Boolean activo) {
        List<UsuarioDTO> usuarios = usuarioClient.listar(activo);
        List<UsuarioDTO> vigilantes = new ArrayList<>();

        for (UsuarioDTO usuario : usuarios) {
            if (usuario.getRol() != null && usuario.getRol().equalsIgnoreCase("VIGILANTE")) {
                vigilantes.add(usuario);
            }
        }

        return vigilantes;
    }

    private boolean puedeEscribir() {
        return permisosInterfaz.puedeModificarContratos();
    }

    // Recarga las opciones y los contratos al ingresar a la vista.
    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        vigilantePendiente = null;
        avisoPendientes.setVisible(false);
        java.util.Map<String, List<String>> parametros = evento.getLocation().getQueryParameters().getParameters();
        List<String> vigilantes = parametros.get("vigilante");
        if (parametros.getOrDefault("pendientes", List.of()).contains("true")
                && vigilantes != null && !vigilantes.isEmpty() && !vigilantes.get(0).isBlank()) {
            vigilantePendiente = vigilantes.get(0);
            criterioBusqueda.setValue("Vigilante");
            valorBusqueda.setValue(vigilantePendiente);
            filtroEstado.setValue("Activos");
            avisoPendientes.setText("Contratos pendientes de " + vigilantePendiente
                    + ". Reasigne cada contrato y luego solicite nuevamente la baja. Limpiar muestra todos los contratos.");
            avisoPendientes.setVisible(true);
        }
        cargarOpcionesFiltros();
        cargarContratos();
    }

    private void configurarBusqueda() {
        criterioBusqueda.setLabel("Criterio de búsqueda");
        criterioBusqueda.setItems("Todas", "Código", "Fecha", "Vigilante", "Sucursal", "Con arma", "Motivo de baja");
        criterioBusqueda.setValue("Todas");
        valorBusqueda.setPlaceholder("Ingrese una coincidencia");
        valorBusqueda.setClearButtonVisible(true);
        valorBusqueda.setVisible(false);
        criterioBusqueda.addValueChangeListener(new com.vaadin.flow.component.HasValue.ValueChangeListener<com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<String>, String>>() {
            @Override public void valueChanged(com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<Select<String>, String> evento) {
                valorBusqueda.clear();
                valorBusqueda.setVisible(!"Todas".equals(evento.getValue()));
                valorBusqueda.setLabel(evento.getValue());
            }
        });
    }

    private HorizontalLayout crearBarraBusqueda() {
        Button buscar = new Button("Buscar", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
            @Override public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) { aplicarFiltros(); }
        });
        Button limpiar = new Button("Limpiar", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
            @Override public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) {
                criterioBusqueda.setValue("Todas");
                vigilantePendiente = null;
                avisoPendientes.setVisible(false);
                valorBusqueda.clear();
                filtroEstado.setValue("Todos");
                aplicarFiltros();
            }
        });
        HorizontalLayout barra = new HorizontalLayout(criterioBusqueda, valorBusqueda, filtroEstado, buscar, limpiar);
        barra.setAlignItems(HorizontalLayout.Alignment.END);
        return barra;
    }

    private void aplicarFiltros() {
        String criterio = criterioBusqueda.getValue();
        String texto = NormalizadorBusqueda.normalizar(valorBusqueda.getValue());
        List<ContratoVigilanciaDTO> filtrados = new java.util.ArrayList<>();
        for (ContratoVigilanciaDTO dato : datosBusqueda) {
            if (vigilantePendiente != null && (!vigilantePendiente.equalsIgnoreCase(dato.getVigilanteCodigo())
                    || !Boolean.TRUE.equals(dato.getActivo()) || dato.getFecha() == null
                    || dato.getFecha().isBefore(LocalDate.now()))) {
                continue;
            }
            if ((obtenerEstado() == null || obtenerEstado().equals(dato.getActivo())) && coincideCriterio(dato, criterio, texto)) {
                filtrados.add(dato);
            }
        }
        paginadorContratos.setItems(filtrados);
    }

    private boolean coincideCriterio(ContratoVigilanciaDTO dato, String criterio, String texto) {
        if ("Código".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getCodigo()).contains(texto); }
        if ("Fecha".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getFecha()).contains(texto); }
        if ("Vigilante".equals(criterio)) { return NormalizadorBusqueda.normalizar(textoVigilante(dato)).contains(texto); }
        if ("Sucursal".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getSucursalCodigo()).contains(texto); }
        if ("Con arma".equals(criterio)) { return NormalizadorBusqueda.normalizar(Boolean.TRUE.equals(dato.getConArma()) ? "Sí" : "No").contains(texto); }
        if ("Motivo de baja".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getMotivoBaja()).contains(texto); }
        return true;
    }


    private String textoVigilante(ContratoVigilanciaDTO contrato) {
        String codigo = contrato.getVigilanteCodigo();
        for (UsuarioDTO usuario : vigilantesBusqueda) {
            if (usuario.getCodigo() != null && usuario.getCodigo().equals(codigo)) {
                return codigo + " " + usuario.getNombre();
            }
        }
        return codigo;
    }
}
