package prog2.policia_frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import prog2.policia_frontend.clients.JuezClient;
import prog2.policia_frontend.dtos.JuezDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.NormalizadorBusqueda;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

@Route(value = "jueces", layout = MainLayout.class)
@PageTitle("Jueces | Sistema Policial")
public class JuecesView extends VerticalLayout implements BeforeEnterObserver {

    private static final int PAGE_SIZE = 10;

    private final JuezClient juezClient;
    private final PermisosInterfaz permisosInterfaz;
    private final Grid<JuezDTO> tabla = new Grid<>(JuezDTO.class, false);
    private final Select<String> criterioBusqueda = new Select<>();
    private final com.vaadin.flow.component.textfield.TextField valorBusqueda = new com.vaadin.flow.component.textfield.TextField("Buscar");
    private List<JuezDTO> datosBusqueda = List.of();
    private final Select<String> filtroEstado = new Select<>();
    private final Button btnAnterior = new Button("Anterior");
    private final Button btnSiguiente = new Button("Siguiente");
    private final Span lblPaginacion = new Span();

    private int paginaActual = 0;
    private List<JuezDTO> listaCompletaJueces = new ArrayList<>();

    public JuecesView(JuezClient juezClient, PermisosInterfaz permisosInterfaz) {
        this.juezClient = juezClient;
        this.permisosInterfaz = permisosInterfaz;
        configurarFiltros();
        configurarTabla();
        configurarPaginacion();
        configurarVista();
    }

    private void configurarFiltros() {
        configurarBusqueda();
        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todos", "Activos", "Inactivos");
        filtroEstado.setValue("Todos");
    }

    private void configurarTabla() {
        tabla.addColumn(JuezDTO::getCodigo).setHeader("Código").setAutoWidth(true);
        tabla.addColumn(JuezDTO::getNombre).setHeader("Nombre").setAutoWidth(true).setFlexGrow(1);
        tabla.addColumn(JuezDTO::getJuezDesde).setHeader("Juez desde").setAutoWidth(true);
        tabla.addColumn(juez -> juez.getAniosServicio() != null ? juez.getAniosServicio() : 0).setHeader("Años de servicio").setAutoWidth(true);
        tabla.addColumn(juez -> Boolean.TRUE.equals(juez.getActivo()) ? "Activo" : "Inactivo").setHeader("Estado").setAutoWidth(true);
        tabla.addColumn(juez -> juez.getMotivoBaja() != null ? juez.getMotivoBaja() : "-").setHeader("Motivo de baja").setAutoWidth(true);
        tabla.addComponentColumn(this::crearBotonesAccion).setHeader("Acciones").setAutoWidth(true);
        tabla.setSizeFull();
    }

    private com.vaadin.flow.component.Component crearBotonesAccion(JuezDTO juez) {
        Button botonVerCasos = new Button("Ver casos", evento -> verCasos(juez));
        botonVerCasos.setVisible(permisosInterfaz.puedeConsultarCasosJudiciales());

        if (!puedeEscribir()) {
            return botonVerCasos;
        }

        boolean estaActivo = Boolean.TRUE.equals(juez.getActivo());

        Button botonModificar = new Button("Modificar");
        botonModificar.setEnabled(estaActivo);

        if (estaActivo) {
            botonModificar.addClickListener(evento -> abrirFormulario(juez));
        } else {
            botonModificar.setTooltipText("Debe reactivar al juez antes de modificarlo.");
        }

        Button botonEstado = new Button();

        if (estaActivo) {
            botonEstado.setText("Dar de baja");
            botonEstado.addClickListener(evento -> abrirDialogoBaja(juez));
        } else if ("FALLECIMIENTO".equals(juez.getMotivoBaja())) {
            botonEstado.setText("No reactivable");
            botonEstado.setEnabled(false);
            botonEstado.setTooltipText("El motivo de baja fue fallecimiento.");
        } else {
            botonEstado.setText("Reactivar");
            botonEstado.addClickListener(evento -> reactivarJuez(juez));
        }

        return new HorizontalLayout(botonVerCasos, botonModificar, botonEstado);
    }

    private void configurarPaginacion() {
        btnAnterior.addClickListener(evento -> {
            if (paginaActual > 0) {
                paginaActual--;
                actualizarTabla();
            }
        });

        btnSiguiente.addClickListener(evento -> {
            int totalPaginas = (listaCompletaJueces.size() + PAGE_SIZE - 1) / PAGE_SIZE;
            if (paginaActual < totalPaginas - 1) {
                paginaActual++;
                actualizarTabla();
            }
        });
    }

    private void verCasos(JuezDTO juez) {
        UI.getCurrent().navigate(CasosJudicialesView.class, QueryParameters.simple(Map.of("juez", juez.getCodigo())));
    }

    private void configurarVista() {
        H2 titulo = new H2("Jueces");

        Button botonNuevo = new Button("Nuevo juez", evento -> abrirFormulario(null));
        botonNuevo.setVisible(puedeEscribir());

        HorizontalLayout filtros = crearBarraBusqueda();
        filtros.setAlignItems(Alignment.END);

        HorizontalLayout paginacion = new HorizontalLayout(btnAnterior, lblPaginacion, btnSiguiente);
        paginacion.setAlignItems(Alignment.CENTER);

        setSizeFull();
        add(titulo, botonNuevo, filtros, tabla, paginacion);
        expand(tabla);
    }

    private void abrirFormulario(JuezDTO juez) {
        boolean esCreacion = juez == null;

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(esCreacion ? "Nuevo juez" : "Modificar " + juez.getCodigo());

        TextField campoNombre = new TextField("Nombre");
        DatePicker campoJuezDesde = new DatePicker("Juez desde");

        campoNombre.setRequiredIndicatorVisible(true);
        campoJuezDesde.setRequiredIndicatorVisible(true);
        campoJuezDesde.setMax(LocalDate.now());

        campoNombre.setWidthFull();
        campoJuezDesde.setWidthFull();

        if (!esCreacion) {
            campoNombre.setValue(juez.getNombre() != null ? juez.getNombre() : "");
            campoJuezDesde.setValue(juez.getJuezDesde());
        }

        Button botonGuardar = new Button(esCreacion ? "Guardar" : "Guardar cambios");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());

        botonGuardar.addClickListener(evento -> {
            boolean nombreVacio = campoNombre.getValue().isBlank();
            boolean fechaInvalida = campoJuezDesde.getValue() == null || campoJuezDesde.getValue().isAfter(LocalDate.now());

            campoNombre.setInvalid(nombreVacio);
            campoNombre.setErrorMessage("El nombre es obligatorio.");
            campoJuezDesde.setInvalid(fechaInvalida);
            campoJuezDesde.setErrorMessage("La fecha debe ser de hoy o pasada.");

            if (nombreVacio || fechaInvalida) {
                return;
            }

            JuezDTO datos = new JuezDTO(campoNombre.getValue().trim(), campoJuezDesde.getValue());

            try {
                if (esCreacion) {
                    juezClient.crear(datos);
                } else {
                    juezClient.actualizar(juez.getCodigo(), datos);
                }

                dialogo.close();
                cargarJueces();
                NotificacionUtils.mostrarExito(esCreacion ? "Juez creado correctamente." : "Juez modificado correctamente.");

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            } catch (IllegalArgumentException excepcion) {
                NotificacionUtils.mostrarInformacion(excepcion.getMessage());
            }
        });

        dialogo.add(new VerticalLayout(campoNombre, campoJuezDesde, new HorizontalLayout(botonGuardar, botonCancelar)));
        dialogo.open();
    }

    private void abrirDialogoBaja(JuezDTO juez) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Dar de baja " + juez.getCodigo());

        Select<String> campoMotivo = new Select<>();
        campoMotivo.setLabel("Motivo de baja");

        try {
            List<String> motivos = juezClient.listarMotivosBaja();
            if (motivos.isEmpty()) {
                NotificacionUtils.mostrarInformacion("No hay motivos de baja disponibles.");
                return;
            }
            campoMotivo.setItems(motivos);
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar los motivos de baja.");
            return;
        }

        campoMotivo.setRequiredIndicatorVisible(true);
        campoMotivo.setWidthFull();

        Button botonConfirmar = new Button("Confirmar baja");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());

        botonConfirmar.addClickListener(evento -> {
            String motivo = campoMotivo.getValue();
            boolean motivoVacio = motivo == null || motivo.isBlank();

            campoMotivo.setInvalid(motivoVacio);
            campoMotivo.setErrorMessage("Debe seleccionar un motivo.");

            if (motivoVacio) {
                return;
            }

            try {
                juezClient.darDeBaja(juez.getCodigo(), motivo);
                dialogo.close();
                cargarJueces();
                NotificacionUtils.mostrarExito("Juez dado de baja correctamente.");

            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            }
        });

        dialogo.add(new VerticalLayout(campoMotivo, new HorizontalLayout(botonConfirmar, botonCancelar)));
        dialogo.open();
    }

    private void reactivarJuez(JuezDTO juez) {
        try {
            juezClient.reactivar(juez.getCodigo());
            cargarJueces();
            NotificacionUtils.mostrarExito("Juez reactivado correctamente.");

        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        }
    }

    private Boolean obtenerEstado() {
        return switch (filtroEstado.getValue()) {
            case "Activos" ->
                true;
            case "Inactivos" ->
                false;
            default ->
                null;
        };
    }

    private boolean puedeEscribir() {
        return permisosInterfaz.puedeModificarJueces();
    }

    private void cargarJueces() {
        datosBusqueda = List.of();
        aplicarFiltros();
        try {
            datosBusqueda = juezClient.listar(null);
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

    private void actualizarTabla() {
        int totalRegistros = listaCompletaJueces.size();
        int totalPaginas = Math.max(1, (totalRegistros + PAGE_SIZE - 1) / PAGE_SIZE);
        int desde = paginaActual * PAGE_SIZE;
        int hasta = Math.min(desde + PAGE_SIZE, totalRegistros);

        tabla.setItems(listaCompletaJueces.subList(desde, hasta));
        btnAnterior.setEnabled(paginaActual > 0);
        btnSiguiente.setEnabled(paginaActual < totalPaginas - 1);
        lblPaginacion.setText(String.format("Página %d de %d · %d registros", paginaActual + 1, totalPaginas, totalRegistros));
    }

    private void mostrarErrorOperacion(RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarJueces();
    }

    private void configurarBusqueda() {
        criterioBusqueda.setLabel("Criterio de búsqueda");
        criterioBusqueda.setItems("Todas", "Código", "Nombre", "Juez desde", "Años de servicio", "Motivo de baja");
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
        List<JuezDTO> filtrados = new java.util.ArrayList<>();
        for (JuezDTO dato : datosBusqueda) {
            if ((obtenerEstado() == null || obtenerEstado().equals(dato.getActivo())) && coincideCriterio(dato, criterio, texto)) {
                filtrados.add(dato);
            }
        }
        listaCompletaJueces = filtrados;
        paginaActual = 0;
        actualizarTabla();
    }

    private boolean coincideCriterio(JuezDTO dato, String criterio, String texto) {
        if ("Código".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getCodigo()).contains(texto); }
        if ("Nombre".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getNombre()).contains(texto); }
        if ("Juez desde".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getJuezDesde()).contains(texto); }
        if ("Años de servicio".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getAniosServicio()).contains(texto); }
        if ("Motivo de baja".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getMotivoBaja()).contains(texto); }
        return true;
    }

}
