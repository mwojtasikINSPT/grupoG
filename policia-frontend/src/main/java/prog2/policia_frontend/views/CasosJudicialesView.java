package prog2.policia_frontend.views;

import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import prog2.policia_frontend.clients.AsaltanteClient;
import prog2.policia_frontend.clients.AsaltoClient;
import prog2.policia_frontend.clients.CasoJudicialClient;
import prog2.policia_frontend.clients.JuezClient;
import prog2.policia_frontend.clients.PruebaDefensaClient;
import prog2.policia_frontend.components.PruebasDefensaDialog;
import prog2.policia_frontend.components.PaginadorGrid;
import prog2.policia_frontend.dtos.AsaltanteDTO;
import prog2.policia_frontend.dtos.AsaltoDTO;
import prog2.policia_frontend.dtos.CasoJudicialDTO;
import prog2.policia_frontend.dtos.JuezDTO;
import prog2.policia_frontend.dtos.SentenciaAsaltanteDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

// Administra los casos judiciales y sus sentencias.
@Route(value = "casos-judiciales", layout = MainLayout.class)
@PageTitle("Casos judiciales | Sistema Policial")
public class CasosJudicialesView extends VerticalLayout implements BeforeEnterObserver {

    private final CasoJudicialClient casoJudicialClient;
    private final AsaltoClient asaltoClient;
    private final AsaltanteClient asaltanteClient;
    private final JuezClient juezClient;
    private final PruebaDefensaClient pruebaDefensaClient;
    private final PermisosInterfaz permisosInterfaz;
    private final Grid<CasoJudicialDTO> tabla = new Grid<>(CasoJudicialDTO.class, false);
    private final PaginadorGrid<CasoJudicialDTO> paginadorCasos = new PaginadorGrid<>(tabla, 10);
    private final Select<String> criterioBusqueda = new Select<>();
    private final com.vaadin.flow.component.textfield.TextField valorBusqueda = new com.vaadin.flow.component.textfield.TextField("Buscar");
    private List<CasoJudicialDTO> datosBusqueda = List.of();
    private final Select<String> filtroEstado = new Select<>();
    private final Button botonVolver = new Button("Volver");
    private String rutaVolver;

    public CasosJudicialesView(CasoJudicialClient casoJudicialClient, AsaltoClient asaltoClient, AsaltanteClient asaltanteClient, JuezClient juezClient, PermisosInterfaz permisosInterfaz, PruebaDefensaClient pruebaDefensaClient) {
        this.casoJudicialClient = casoJudicialClient;
        this.asaltoClient = asaltoClient;
        this.asaltanteClient = asaltanteClient;
        this.juezClient = juezClient;
        this.pruebaDefensaClient = pruebaDefensaClient;
        this.permisosInterfaz = permisosInterfaz;
        botonVolver.setVisible(false);
        botonVolver.addClickListener(evento -> {
            if (rutaVolver != null) {
                UI.getCurrent().navigate(rutaVolver);
            }
        });
        configurarFiltros();
        configurarTabla();
        configurarVista();
    }

    private void configurarFiltros() {
        configurarBusqueda();
        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todos", "En trámite", "Cerrados", "Reabiertos por apelación");
        filtroEstado.setValue("Todos");
    }

    private void configurarTabla() {
        tabla.addColumn(CasoJudicialDTO::getCodigo).setHeader("Código").setAutoWidth(true);
        tabla.addColumn(CasoJudicialDTO::getAsaltoCodigo).setHeader("Asalto").setAutoWidth(true);
        tabla.addColumn(caso -> {
            List<String> codigos = caso.getAsaltanteCodigos();
            return codigos == null || codigos.isEmpty() ? "Sin participantes" : String.join(", ", codigos);
        }).setHeader("Asaltantes").setAutoWidth(true).setFlexGrow(1);
        tabla.addColumn(CasoJudicialDTO::getJuezCodigo).setHeader("Juez").setAutoWidth(true);
        tabla.addColumn(caso -> Boolean.TRUE.equals(caso.getSentenciado()) ? "Completo" : "Pendiente").setHeader("Sentencias").setAutoWidth(true);
        tabla.addColumn(this::obtenerEstadoVisible).setHeader("Estado").setAutoWidth(true);
        tabla.addColumn(caso -> caso.getMotivoReapertura() == null || caso.getMotivoReapertura().isBlank() ? "-" : caso.getMotivoReapertura())
                .setHeader("Motivo de reapertura").setAutoWidth(true);
        tabla.addComponentColumn(caso -> {
            Button botonSentencias = new Button("Ver sentencias", evento -> abrirSentencias(caso));
            botonSentencias.setVisible(permisosInterfaz.puedeConsultarSentencias());
            if (!permisosInterfaz.puedeModificarCasos()) {
                return botonSentencias;
            }
            boolean activo = Boolean.TRUE.equals(caso.getActivo());
            String estado = obtenerEstado(caso);
            Button botonModificar = new Button("Modificar", evento -> abrirFormulario(caso));
            botonModificar.setVisible(permisosInterfaz.puedeModificarCasos());
            Button botonEstado = new Button();
            if ("CERRADO".equals(estado)) {
                botonEstado.setText("Reabrir por apelación");
                botonEstado.addClickListener(evento -> abrirReapertura(caso));
            } else if ("REABIERTO_POR_APELACION".equals(estado)) {
                botonEstado.setText("Cerrar");
                botonEstado.addClickListener(evento -> confirmarCierre(caso));
            } else {
                botonEstado.setText("Cierre automático");
                botonEstado.setEnabled(false);
                botonEstado.setTooltipText("El caso se cierra cuando todas las participaciones se resuelven.");
            }
            return new HorizontalLayout(botonSentencias, botonModificar, botonEstado);
        }).setHeader("Acciones").setAutoWidth(true);
        tabla.setSizeFull();
    }

    private void configurarVista() {
        H2 titulo = new H2("Casos Judiciales");
        Button botonNuevo = new Button("Nuevo caso", evento -> abrirFormulario(null));
        botonNuevo.setVisible(permisosInterfaz.puedeModificarCasos());


        HorizontalLayout filtros = crearBarraBusqueda();
        filtros.setAlignItems(HorizontalLayout.Alignment.END);
        setSizeFull();
        add(titulo, botonVolver, botonNuevo, filtros, tabla, paginadorCasos.getControles());
        expand(tabla);
    }

    private void abrirFormulario(CasoJudicialDTO caso) {
        boolean creacion = caso == null;
        List<AsaltoDTO> asaltos;
        List<JuezDTO> jueces;
        try {
            asaltos = asaltoClient.listar();
            jueces = juezClient.listar(true);
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar asaltos y jueces.");
            return;
        }
        if (asaltos.isEmpty() || jueces.isEmpty()) {
            NotificacionUtils.mostrarInformacion("Se necesita al menos un asalto y un juez activo.");
            return;
        }
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(creacion ? "Nuevo caso judicial" : "Modificar " + caso.getCodigo());
        Select<AsaltoDTO> campoAsalto = new Select<>();
        campoAsalto.setLabel("Asalto");
        campoAsalto.setItems(asaltos);
        campoAsalto.setItemLabelGenerator(asalto -> asalto.getCodigo() + " - " + asalto.getFecha());
        campoAsalto.setRequiredIndicatorVisible(true);
        campoAsalto.setWidthFull();
        Select<JuezDTO> campoJuez = new Select<>();
        campoJuez.setLabel("Juez");
        campoJuez.setItems(jueces);
        campoJuez.setItemLabelGenerator(juez -> juez.getCodigo() + " - " + juez.getNombre());
        campoJuez.setRequiredIndicatorVisible(true);
        campoJuez.setWidthFull();
        if (!creacion) {
            campoAsalto.setValue(buscarAsalto(asaltos, caso.getAsaltoCodigo()));
            campoAsalto.setEnabled(false);
            campoJuez.setValue(buscarJuez(jueces, caso.getJuezCodigo()));
        }
        Button botonGuardar = new Button(creacion ? "Guardar" : "Guardar cambios");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());
        botonGuardar.addClickListener(evento -> {
            boolean asaltoVacio = campoAsalto.getValue() == null;
            boolean juezVacio = campoJuez.getValue() == null;
            campoAsalto.setInvalid(asaltoVacio);
            campoAsalto.setErrorMessage("Debe seleccionar un asalto.");
            campoJuez.setInvalid(juezVacio);
            campoJuez.setErrorMessage("Debe seleccionar un juez.");
            if (asaltoVacio || juezVacio) {
                return;
            }
            CasoJudicialDTO datos = new CasoJudicialDTO();
            datos.setJuezCodigo(campoJuez.getValue().getCodigo());
            if (creacion) {
                datos.setAsaltoCodigo(campoAsalto.getValue().getCodigo());
            }
            try {
                if (creacion) {
                    casoJudicialClient.crear(datos);
                } else {
                    casoJudicialClient.actualizar(caso.getCodigo(), datos);
                }
                dialogo.close();
                cargarCasos();
                NotificacionUtils.mostrarExito(creacion ? "Caso judicial creado correctamente." : "Caso judicial modificado correctamente.");
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            }
        });
        dialogo.add(new VerticalLayout(campoAsalto, campoJuez, new HorizontalLayout(botonGuardar, botonCancelar)));
        dialogo.open();
    }

    private void abrirSentencias(CasoJudicialDTO caso) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Sentencias de " + caso.getCodigo());
        dialogo.setWidth("760px");
        Grid<SentenciaAsaltanteDTO> tablaSentencias = new Grid<>(SentenciaAsaltanteDTO.class, false);
        if (permisosInterfaz.puedeConsultarPruebas()) {
            tablaSentencias.addComponentColumn(new com.vaadin.flow.function.ValueProvider<SentenciaAsaltanteDTO, com.vaadin.flow.component.Component>() {
                @Override
                public com.vaadin.flow.component.Component apply(SentenciaAsaltanteDTO sentencia) {
                    return new Button("Pruebas de defensa", new com.vaadin.flow.component.ComponentEventListener<com.vaadin.flow.component.ClickEvent<Button>>() {
                        @Override
                        public void onComponentEvent(com.vaadin.flow.component.ClickEvent<Button> evento) {
                            new PruebasDefensaDialog(pruebaDefensaClient, permisosInterfaz,
                                    sentencia.getAsaltanteCodigo(), caso.getCodigo(), false).open();
                        }
                    });
                }
            }).setHeader("Defensa").setAutoWidth(true);
        }
        tablaSentencias.addColumn(SentenciaAsaltanteDTO::getAsaltanteCodigo).setHeader("Asaltante").setAutoWidth(true);
        if (permisosInterfaz.puedeConsultarAsaltantes()) {
            List<AsaltanteDTO> asaltantes = new java.util.ArrayList<>();
            try {
                asaltantes.addAll(asaltanteClient.listar(null));
            } catch (RestClientResponseException | ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se pudieron cargar los nombres de los asaltantes.");
            }
            tablaSentencias.addColumn(sentencia -> {
                for (AsaltanteDTO asaltante : asaltantes) {
                    if (asaltante.getCodigo() != null && asaltante.getCodigo().equalsIgnoreCase(sentencia.getAsaltanteCodigo())) {
                        return asaltante.getNombre();
                    }
                }
                return "No disponible";
            }).setHeader("Nombre").setAutoWidth(true);
        }
        tablaSentencias.addColumn(sentencia -> Boolean.TRUE.equals(sentencia.getSentenciado()) ? "Sentenciado" : "Pendiente")
                .setHeader("Estado").setAutoWidth(true);
        tablaSentencias.addColumn(sentencia -> {
            if (!Boolean.TRUE.equals(sentencia.getSentenciado())) {
                return "-";
            }
            return Boolean.TRUE.equals(sentencia.getCondenado()) ? "Sí" : "No";
        }).setHeader("Condenado").setAutoWidth(true);
        tablaSentencias.addColumn(sentencia -> sentencia.getTiempoCarcel() == null ? "-" : sentencia.getTiempoCarcel())
                .setHeader("Meses de cárcel").setAutoWidth(true);
        if ((permisosInterfaz.puedeModificarSentencias())
                && Boolean.TRUE.equals(caso.getActivo())) {
            tablaSentencias.addComponentColumn(sentencia -> new Button(Boolean.TRUE.equals(sentencia.getSentenciado())
                    ? "Modificar" : "Registrar",
                    evento -> abrirFormularioSentencia(caso, sentencia, tablaSentencias))).setHeader("Acción").setAutoWidth(true);
        }
        try {
            tablaSentencias.setItems(casoJudicialClient.listarSentencias(caso.getCodigo()));
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
            return;
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pueden consultar las sentencias.");
            return;
        }
        tablaSentencias.setWidthFull();
        tablaSentencias.setHeight("360px");
        Button cerrar = new Button("Cerrar", evento -> dialogo.close());
        dialogo.add(tablaSentencias, cerrar);
        dialogo.open();
    }

    private void abrirFormularioSentencia(CasoJudicialDTO caso, SentenciaAsaltanteDTO sentencia, Grid<SentenciaAsaltanteDTO> tablaSentencias) {
        boolean modificacion = Boolean.TRUE.equals(sentencia.getSentenciado());
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle((modificacion ? "Modificar sentencia de " : "Registrar sentencia de ") + sentencia.getAsaltanteCodigo());
        Checkbox campoCondenado = new Checkbox("Condenado");
        campoCondenado.setValue(modificacion && Boolean.TRUE.equals(sentencia.getCondenado()));
        IntegerField campoMeses = new IntegerField("Meses de cárcel");
        campoMeses.setMin(0);
        campoMeses.setStepButtonsVisible(true);
        campoMeses.setValue(modificacion && sentencia.getTiempoCarcel() != null ? sentencia.getTiempoCarcel() : 0);
        campoMeses.setEnabled(campoCondenado.getValue());
        campoCondenado.addValueChangeListener(evento -> {
            boolean condenado = Boolean.TRUE.equals(evento.getValue());
            campoMeses.setEnabled(condenado);
            if (!condenado) {
                campoMeses.setValue(0);
            }
        });
        Button guardar = new Button("Guardar", evento -> {
            int meses = campoMeses.getValue() != null ? campoMeses.getValue() : 0;
            boolean condenado = Boolean.TRUE.equals(campoCondenado.getValue());
            campoMeses.setInvalid(condenado && meses <= 0);
            campoMeses.setErrorMessage("Una condena debe indicar al menos un mes de cárcel.");
            if (condenado && meses <= 0) {
                return;
            }
            SentenciaAsaltanteDTO datos = new SentenciaAsaltanteDTO(sentencia.getAsaltanteCodigo(), condenado, meses);
            datos.setSentenciado(true);
            try {
                if (modificacion) {
                    casoJudicialClient.modificarSentencia(caso.getCodigo(), datos);
                } else {
                    casoJudicialClient.registrarSentencia(caso.getCodigo(), datos);
                }
                tablaSentencias.setItems(casoJudicialClient.listarSentencias(caso.getCodigo()));
                cargarCasos();
                dialogo.close();
                NotificacionUtils.mostrarExito("Sentencia guardada correctamente.");
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            }
        });
        Button cancelar = new Button("Cancelar", evento -> dialogo.close());
        dialogo.add(campoCondenado, campoMeses, new HorizontalLayout(guardar, cancelar));
        dialogo.open();
    }

    private AsaltoDTO buscarAsalto(List<AsaltoDTO> asaltos, String codigo) {
        for (AsaltoDTO asalto : asaltos) {
            if (asalto.getCodigo() != null && asalto.getCodigo().equalsIgnoreCase(codigo)) {
                return asalto;
            }
        }
        return null;
    }

    private JuezDTO buscarJuez(List<JuezDTO> jueces, String codigo) {
        for (JuezDTO juez : jueces) {
            if (juez.getCodigo() != null && juez.getCodigo().equalsIgnoreCase(codigo)) {
                return juez;
            }
        }
        return null;
    }

    private void confirmarCierre(CasoJudicialDTO caso) {
        ConfirmDialog confirmacion = new ConfirmDialog();
        confirmacion.setHeader("Cerrar caso judicial");
        confirmacion.setText("¿Confirma el cierre de " + caso.getCodigo() + "?");
        confirmacion.setCancelable(true);
        confirmacion.setCancelText("Cancelar");
        confirmacion.setConfirmText("Cerrar caso");
        confirmacion.addConfirmListener(evento -> cerrarCaso(caso));
        confirmacion.open();
    }

    private void cerrarCaso(CasoJudicialDTO caso) {
        try {
            casoJudicialClient.cerrar(caso.getCodigo());
            cargarCasos();
            NotificacionUtils.mostrarExito("Caso judicial cerrado correctamente.");
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        }
    }

    private void abrirReapertura(CasoJudicialDTO caso) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Reabrir " + caso.getCodigo() + " por apelación");
        TextArea campoMotivo = new TextArea("Motivo de la apelación");
        campoMotivo.setWidthFull();
        campoMotivo.setMaxLength(500);
        campoMotivo.setRequiredIndicatorVisible(true);
        Button botonConfirmar = new Button("Reabrir caso");
        Button botonCancelar = new Button("Cancelar", evento -> dialogo.close());
        botonConfirmar.addClickListener(evento -> {
            boolean motivoVacio = campoMotivo.getValue().isBlank();
            campoMotivo.setInvalid(motivoVacio);
            campoMotivo.setErrorMessage("Debe indicar el motivo de la apelación.");
            if (motivoVacio) {
                return;
            }
            if (reactivarCaso(caso, campoMotivo.getValue().trim())) {
                dialogo.close();
            }
        });
        dialogo.add(campoMotivo, new HorizontalLayout(botonConfirmar, botonCancelar));
        dialogo.open();
    }

    private boolean reactivarCaso(CasoJudicialDTO caso, String motivo) {
        try {
            casoJudicialClient.reactivar(caso.getCodigo(), motivo);
            cargarCasos();
            NotificacionUtils.mostrarExito("Caso judicial reabierto por apelación correctamente.");
            return true;
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        } catch (IllegalArgumentException excepcion) {
            NotificacionUtils.mostrarInformacion(excepcion.getMessage());
        }
        return false;
    }

    private void cargarCasos() {
        datosBusqueda = List.of();
        aplicarFiltros();
        try {
            datosBusqueda = casoJudicialClient.listar();
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

    private String obtenerEstadoExactoSeleccionado() {
        return switch (filtroEstado.getValue()) {
            case "En trámite" ->
                "EN_TRAMITE";
            case "Cerrados" ->
                "CERRADO";
            case "Reabiertos por apelación" ->
                "REABIERTO_POR_APELACION";
            default ->
                null;
        };
    }

    private String obtenerEstado(CasoJudicialDTO caso) {
        if (caso.getEstado() != null && !caso.getEstado().isBlank()) {
            return caso.getEstado();
        }
        return Boolean.TRUE.equals(caso.getActivo())
                ? "EN_TRAMITE"
                : "CERRADO";
    }

    private String obtenerEstadoVisible(CasoJudicialDTO caso) {
        return switch (obtenerEstado(caso)) {
            case "EN_TRAMITE" ->
                "En trámite";
            case "REABIERTO_POR_APELACION" ->
                "Reabierto por apelación";
            case "CERRADO" ->
                "Cerrado";
            default ->
                obtenerEstado(caso);
        };
    }

    private void mostrarErrorOperacion(RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        aplicarFiltroRecibido(evento);
        cargarCasos();
    }

    private void aplicarFiltroRecibido(BeforeEnterEvent evento) {
        Map<String, List<String>> parametros = evento.getLocation().getQueryParameters().getParameters();
        String asaltante = primerParametro(parametros, "asaltante");
        String juez = primerParametro(parametros, "juez");
        if (asaltante != null && permisosInterfaz.puedeConsultarAsaltantes()) {
            criterioBusqueda.setValue("Asaltante");
            valorBusqueda.setValue(asaltante);
            configurarRegreso("asaltantes", "Volver a asaltantes");
        } else if (juez != null && permisosInterfaz.puedeConsultarJueces()) {
            criterioBusqueda.setValue("Juez");
            valorBusqueda.setValue(juez);
            configurarRegreso("jueces", "Volver a jueces");
        }
    }

    private void configurarRegreso(String ruta, String texto) {
        rutaVolver = ruta;
        botonVolver.setText(texto);
        botonVolver.setVisible(true);
    }

    private String primerParametro(Map<String, List<String>> parametros, String nombre) {
        List<String> valores = parametros.get(nombre);
        return valores == null || valores.isEmpty() ? null : valores.get(0);
    }

    private void configurarBusqueda() {
        criterioBusqueda.setLabel("Criterio de búsqueda");
        criterioBusqueda.setItems("Todas", "Código", "Asalto", "Juez", "Asaltante", "Sentenciado", "Motivo de reapertura");
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
        String texto = normalizarBusqueda(valorBusqueda.getValue());
        List<CasoJudicialDTO> filtrados = new java.util.ArrayList<>();
        for (CasoJudicialDTO dato : datosBusqueda) {
            if ((obtenerEstadoExactoSeleccionado() == null || obtenerEstadoExactoSeleccionado().equals(obtenerEstado(dato))) && coincideCriterio(dato, criterio, texto)) {
                filtrados.add(dato);
            }
        }
        paginadorCasos.setItems(filtrados);
    }

    private boolean coincideCriterio(CasoJudicialDTO dato, String criterio, String texto) {
        if ("Código".equals(criterio)) { return normalizarBusqueda(dato.getCodigo()).contains(texto); }
        if ("Asalto".equals(criterio)) { return normalizarBusqueda(dato.getAsaltoCodigo()).contains(texto); }
        if ("Juez".equals(criterio)) { return normalizarBusqueda(dato.getJuezCodigo()).contains(texto); }
        if ("Asaltante".equals(criterio)) { return normalizarBusqueda(dato.getAsaltanteCodigos()).contains(texto); }
        if ("Sentenciado".equals(criterio)) { return normalizarBusqueda(Boolean.TRUE.equals(dato.getSentenciado()) ? "Sí" : "No").contains(texto); }
        if ("Motivo de reapertura".equals(criterio)) { return normalizarBusqueda(dato.getMotivoReapertura()).contains(texto); }
        return true;
    }

    private String normalizarBusqueda(Object valor) {
        if (valor == null) { return ""; }
        String texto = valor.toString();
        if (valor instanceof java.time.LocalDate) {
            texto += " " + ((java.time.LocalDate) valor).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT).trim();
    }
}
