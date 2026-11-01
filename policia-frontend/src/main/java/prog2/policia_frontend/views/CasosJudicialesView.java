package prog2.policia_frontend.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import prog2.policia_frontend.utils.NotificacionUtils;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.AsaltanteClient;
import prog2.policia_frontend.clients.AsaltoClient;
import prog2.policia_frontend.clients.CasoJudicialClient;
import prog2.policia_frontend.clients.JuezClient;
import prog2.policia_frontend.dtos.AsaltanteDTO;
import prog2.policia_frontend.dtos.AsaltoDTO;
import prog2.policia_frontend.dtos.CasoJudicialDTO;
import prog2.policia_frontend.dtos.JuezDTO;
import prog2.policia_frontend.dtos.SentenciaAsaltanteDTO;
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;
import prog2.policia_frontend.utils.ErrorBackendUtils;

/**
 * Administra los casos judiciales y las sentencias individuales de sus
 * asaltantes.
 */
@Route(value = "casos-judiciales", layout = MainLayout.class)
@PageTitle("Casos judiciales | Sistema Policial")
public class CasosJudicialesView extends VerticalLayout
        implements BeforeEnterObserver {

    private final CasoJudicialClient casoJudicialClient;
    private final AsaltoClient asaltoClient;
    private final AsaltanteClient asaltanteClient;
    private final JuezClient juezClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<CasoJudicialDTO> tabla
            = new Grid<>(CasoJudicialDTO.class, false);
    private final Select<String> filtroEstado = new Select<>();
    private final Select<String> filtroRelacion = new Select<>();
    private final Select<JuezDTO> filtroJuez = new Select<>();
    private final Select<AsaltanteDTO> filtroAsaltante = new Select<>();
    private final Button botonVolver = new Button("Volver");
    private List<JuezDTO> juecesDisponibles = List.of();
    private List<AsaltanteDTO> asaltantesDisponibles = List.of();
    private String rutaVolver;

    public CasosJudicialesView(
            CasoJudicialClient casoJudicialClient,
            AsaltoClient asaltoClient,
            AsaltanteClient asaltanteClient,
            JuezClient juezClient,
            SesionUsuario sesionUsuario) {

        this.casoJudicialClient = casoJudicialClient;
        this.asaltoClient = asaltoClient;
        this.asaltanteClient = asaltanteClient;
        this.juezClient = juezClient;
        this.sesionUsuario = sesionUsuario;

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
        filtroEstado.setLabel("Estado");
        filtroEstado.setItems(
                "Todos",
                "En trámite",
                "Cerrados",
                "Reabiertos por apelación"
        );
        filtroEstado.setValue("Todos");

        filtroRelacion.setLabel("Buscar casos");
        filtroRelacion.setItems("Todos", "Por juez", "Por asaltante");
        filtroRelacion.setValue("Todos");

        filtroJuez.setLabel("Juez");
        filtroJuez.setItemLabelGenerator(juez
                -> juez.getCodigo() + " - " + juez.getNombre());
        filtroJuez.setVisible(false);

        filtroAsaltante.setLabel("Asaltante");
        filtroAsaltante.setItemLabelGenerator(asaltante
                -> asaltante.getCodigo() + " - " + asaltante.getNombre());
        filtroAsaltante.setVisible(false);

        filtroRelacion.addValueChangeListener(evento -> {
            filtroJuez.setVisible("Por juez".equals(evento.getValue()));
            filtroAsaltante.setVisible(
                    "Por asaltante".equals(evento.getValue()));
            filtroJuez.clear();
            filtroAsaltante.clear();
        });
    }

    private void configurarTabla() {
        tabla.addColumn(CasoJudicialDTO::getCodigo)
                .setHeader("Código").setAutoWidth(true);
        tabla.addColumn(CasoJudicialDTO::getAsaltoCodigo)
                .setHeader("Asalto").setAutoWidth(true);
        tabla.addColumn(caso -> {
            List<String> codigos = caso.getAsaltanteCodigos();
            return codigos == null || codigos.isEmpty()
                    ? "Sin participantes" : String.join(", ", codigos);
        }).setHeader("Asaltantes").setAutoWidth(true).setFlexGrow(1);
        tabla.addColumn(CasoJudicialDTO::getJuezCodigo)
                .setHeader("Juez").setAutoWidth(true);
        tabla.addColumn(caso
                -> Boolean.TRUE.equals(caso.getSentenciado())
                ? "Completo" : "Pendiente")
                .setHeader("Sentencias").setAutoWidth(true);
        tabla.addColumn(this::obtenerEstadoVisible)
                .setHeader("Estado").setAutoWidth(true);
        tabla.addColumn(caso
                -> caso.getMotivoReapertura() == null
                || caso.getMotivoReapertura().isBlank()
                ? "-"
                : caso.getMotivoReapertura())
                .setHeader("Motivo de reapertura")
                .setAutoWidth(true);

        tabla.addComponentColumn(caso -> {
            Button botonSentencias = new Button(
                    "Ver sentencias", evento -> abrirSentencias(caso));
            if (!puedeEscribir()) {
                return botonSentencias;
            }

            boolean activo = Boolean.TRUE.equals(caso.getActivo());
            String estado = obtenerEstado(caso);
            Button botonModificar = new Button(
                    "Modificar", evento -> abrirFormulario(caso));
            botonModificar.setEnabled(activo);

            Button botonEstado = new Button();

            if ("CERRADO".equals(estado)) {
                botonEstado.setText("Reabrir por apelación");
                botonEstado.addClickListener(
                        evento -> abrirReapertura(caso)
                );
            } else if ("REABIERTO_POR_APELACION".equals(estado)) {
                botonEstado.setText("Cerrar");
                botonEstado.addClickListener(
                        evento -> confirmarCierre(caso)
                );
            } else {
                botonEstado.setText("Cierre automático");
                botonEstado.setEnabled(false);
                botonEstado.setTooltipText(
                        "El caso se cierra cuando todas las participaciones se resuelven."
                );
            }

            return new HorizontalLayout(
                    botonSentencias, botonModificar, botonEstado);
        }).setHeader("Acciones").setAutoWidth(true);
        tabla.setSizeFull();
    }

    private void configurarVista() {
        H2 titulo = new H2("Administración de casos judiciales");
        Button botonNuevo = new Button(
                "Nuevo caso", evento -> abrirFormulario(null));
        botonNuevo.setVisible(puedeEscribir());

        Button botonBuscar = new Button("Buscar", evento -> cargarCasos());
        Button botonLimpiar = new Button("Limpiar", evento -> {
            filtroEstado.setValue("Todos");
            filtroRelacion.setValue("Todos");
            filtroJuez.clear();
            filtroAsaltante.clear();
            cargarCasos();
        });

        HorizontalLayout filtros = new HorizontalLayout(
                filtroRelacion, filtroJuez, filtroAsaltante,
                filtroEstado, botonBuscar, botonLimpiar);
        filtros.setAlignItems(HorizontalLayout.Alignment.END);

        setSizeFull();
        add(titulo, botonVolver, botonNuevo, filtros, tabla);
        expand(tabla);
    }

    private void abrirFormulario(CasoJudicialDTO caso) {
        boolean creacion = caso == null;
        List<AsaltoDTO> asaltos;
        List<JuezDTO> jueces;

        try {
            asaltos = asaltoClient.listar();
            jueces = juezClient.listar(true, null);
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
        dialogo.setHeaderTitle(creacion
                ? "Nuevo caso judicial" : "Modificar " + caso.getCodigo());

        Select<AsaltoDTO> campoAsalto = new Select<>();
        campoAsalto.setLabel("Asalto");
        campoAsalto.setItems(asaltos);
        campoAsalto.setItemLabelGenerator(asalto
                -> asalto.getCodigo() + " - " + asalto.getFecha());
        campoAsalto.setRequiredIndicatorVisible(true);
        campoAsalto.setWidthFull();

        Select<JuezDTO> campoJuez = new Select<>();
        campoJuez.setLabel("Juez");
        campoJuez.setItems(jueces);
        campoJuez.setItemLabelGenerator(juez
                -> juez.getCodigo() + " - " + juez.getNombre());
        campoJuez.setRequiredIndicatorVisible(true);
        campoJuez.setWidthFull();

        if (!creacion) {
            campoAsalto.setValue(buscarAsalto(asaltos, caso.getAsaltoCodigo()));
            campoAsalto.setEnabled(false);
            campoJuez.setValue(buscarJuez(jueces, caso.getJuezCodigo()));
        }

        Button botonGuardar = new Button(
                creacion ? "Guardar" : "Guardar cambios");
        Button botonCancelar = new Button(
                "Cancelar", evento -> dialogo.close());

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
                NotificacionUtils.mostrarExito(creacion
                        ? "Caso judicial creado correctamente."
                        : "Caso judicial modificado correctamente.");
            } catch (RestClientResponseException excepcion) {
                mostrarErrorOperacion(excepcion);
            } catch (ResourceAccessException excepcion) {
                NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
            }
        });

        dialogo.add(new VerticalLayout(
                campoAsalto, campoJuez,
                new HorizontalLayout(botonGuardar, botonCancelar)));
        dialogo.open();
    }

    private void abrirSentencias(CasoJudicialDTO caso) {
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Sentencias de " + caso.getCodigo());
        dialogo.setWidth("760px");

        Grid<SentenciaAsaltanteDTO> tablaSentencias
                = new Grid<>(SentenciaAsaltanteDTO.class, false);
        tablaSentencias.addColumn(SentenciaAsaltanteDTO::getAsaltanteCodigo)
                .setHeader("Asaltante").setAutoWidth(true);
        tablaSentencias.addColumn(sentencia
                -> Boolean.TRUE.equals(sentencia.getSentenciado())
                ? "Sentenciado" : "Pendiente")
                .setHeader("Estado").setAutoWidth(true);
        tablaSentencias.addColumn(sentencia -> {
            if (!Boolean.TRUE.equals(sentencia.getSentenciado())) {
                return "-";
            }
            return Boolean.TRUE.equals(sentencia.getCondenado()) ? "Sí" : "No";
        }).setHeader("Condenado").setAutoWidth(true);
        tablaSentencias.addColumn(sentencia
                -> sentencia.getTiempoCarcel() == null
                ? "-" : sentencia.getTiempoCarcel())
                .setHeader("Meses de cárcel").setAutoWidth(true);

        if (puedeEscribir() && Boolean.TRUE.equals(caso.getActivo())) {
            tablaSentencias.addComponentColumn(sentencia -> new Button(
                    Boolean.TRUE.equals(sentencia.getSentenciado())
                    ? "Modificar" : "Registrar",
                    evento -> abrirFormularioSentencia(
                            caso, sentencia, tablaSentencias)))
                    .setHeader("Acción").setAutoWidth(true);
        }

        try {
            tablaSentencias.setItems(
                    casoJudicialClient.listarSentencias(caso.getCodigo()));
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

    private void abrirFormularioSentencia(
            CasoJudicialDTO caso,
            SentenciaAsaltanteDTO sentencia,
            Grid<SentenciaAsaltanteDTO> tablaSentencias) {

        boolean modificacion = Boolean.TRUE.equals(sentencia.getSentenciado());
        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle(
                (modificacion ? "Modificar sentencia de " : "Registrar sentencia de ")
                + sentencia.getAsaltanteCodigo());

        Checkbox campoCondenado = new Checkbox("Condenado");
        campoCondenado.setValue(
                modificacion && Boolean.TRUE.equals(sentencia.getCondenado()));

        IntegerField campoMeses = new IntegerField("Meses de cárcel");
        campoMeses.setMin(0);
        campoMeses.setStepButtonsVisible(true);
        campoMeses.setValue(modificacion && sentencia.getTiempoCarcel() != null
                ? sentencia.getTiempoCarcel() : 0);
        campoMeses.setEnabled(campoCondenado.getValue());

        campoCondenado.addValueChangeListener(evento -> {
            boolean condenado = Boolean.TRUE.equals(evento.getValue());
            campoMeses.setEnabled(condenado);
            if (!condenado) {
                campoMeses.setValue(0);
            }
        });

        Button guardar = new Button("Guardar", evento -> {
            int meses = campoMeses.getValue() != null
                    ? campoMeses.getValue() : 0;
            boolean condenado = Boolean.TRUE.equals(campoCondenado.getValue());
            campoMeses.setInvalid(condenado && meses <= 0);
            campoMeses.setErrorMessage(
                    "Una condena debe indicar al menos un mes de cárcel.");
            if (condenado && meses <= 0) {
                return;
            }

            SentenciaAsaltanteDTO datos = new SentenciaAsaltanteDTO(
                    sentencia.getAsaltanteCodigo(), condenado, meses);
            datos.setSentenciado(true);
            try {
                if (modificacion) {
                    casoJudicialClient.modificarSentencia(caso.getCodigo(), datos);
                } else {
                    casoJudicialClient.registrarSentencia(caso.getCodigo(), datos);
                }
                tablaSentencias.setItems(
                        casoJudicialClient.listarSentencias(caso.getCodigo()));
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
        dialogo.add(campoCondenado, campoMeses,
                new HorizontalLayout(guardar, cancelar));
        dialogo.open();
    }

    private AsaltoDTO buscarAsalto(List<AsaltoDTO> asaltos, String codigo) {
        return asaltos.stream()
                .filter(asalto -> asalto.getCodigo() != null
                && asalto.getCodigo().equalsIgnoreCase(codigo))
                .findFirst().orElse(null);
    }

    private JuezDTO buscarJuez(List<JuezDTO> jueces, String codigo) {
        return jueces.stream()
                .filter(juez -> juez.getCodigo() != null
                && juez.getCodigo().equalsIgnoreCase(codigo))
                .findFirst().orElse(null);
    }

    private void confirmarCierre(CasoJudicialDTO caso) {
        ConfirmDialog confirmacion = new ConfirmDialog();
        confirmacion.setHeader("Cerrar caso judicial");
        confirmacion.setText(
                "¿Confirma el cierre de " + caso.getCodigo() + "?");
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
        dialogo.setHeaderTitle(
                "Reabrir " + caso.getCodigo() + " por apelación"
        );

        TextArea campoMotivo = new TextArea("Motivo de la apelación");
        campoMotivo.setWidthFull();
        campoMotivo.setMaxLength(500);
        campoMotivo.setRequiredIndicatorVisible(true);

        Button botonConfirmar = new Button("Reabrir caso");
        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        botonConfirmar.addClickListener(evento -> {
            boolean motivoVacio = campoMotivo.getValue().isBlank();
            campoMotivo.setInvalid(motivoVacio);
            campoMotivo.setErrorMessage(
                    "Debe indicar el motivo de la apelación."
            );

            if (motivoVacio) {
                return;
            }

            if (reactivarCaso(caso, campoMotivo.getValue().trim())) {
                dialogo.close();
            }
        });

        dialogo.add(
                campoMotivo,
                new HorizontalLayout(botonConfirmar, botonCancelar)
        );
        dialogo.open();
    }

    private boolean reactivarCaso(
            CasoJudicialDTO caso,
            String motivo) {
        try {
            casoJudicialClient.reactivar(caso.getCodigo(), motivo);
            cargarCasos();
            NotificacionUtils.mostrarExito(
                    "Caso judicial reabierto por apelación correctamente."
            );
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
        try {
            Boolean estado = obtenerEstadoSeleccionado();
            String estadoExacto = obtenerEstadoExactoSeleccionado();
            String tipo = filtroRelacion.getValue();
            List<CasoJudicialDTO> casos;

            if ("Por juez".equals(tipo)) {
                if (filtroJuez.getValue() == null) {
                    NotificacionUtils.mostrarInformacion("Seleccione un juez.");
                    return;
                }
                casos = casoJudicialClient.listarPorJuez(
                        filtroJuez.getValue().getCodigo());
            } else if ("Por asaltante".equals(tipo)) {
                if (filtroAsaltante.getValue() == null) {
                    NotificacionUtils.mostrarInformacion("Seleccione un asaltante.");
                    return;
                }
                casos = casoJudicialClient.listarPorAsaltante(
                        filtroAsaltante.getValue().getCodigo());
            } else {
                casos = casoJudicialClient.listar(estado);
            }

            if (estadoExacto != null) {
                casos = casos.stream()
                        .filter(caso -> estadoExacto.equals(
                        obtenerEstado(caso)))
                        .toList();
            }

            tabla.setItems(casos);
            if (!"Todos".equals(tipo) && casos.isEmpty()) {
                NotificacionUtils.mostrarInformacion("No se encontraron casos judiciales.");
            }
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se puede conectar con el backend.");
        }
    }

    private void cargarOpcionesFiltros() {
        try {
            juecesDisponibles = juezClient.listar(null, null);
            asaltantesDisponibles = asaltanteClient.listar(null, null);
            filtroJuez.setItems(juecesDisponibles);
            filtroAsaltante.setItems(asaltantesDisponibles);
        } catch (RestClientResponseException excepcion) {
            mostrarErrorOperacion(excepcion);
        } catch (ResourceAccessException excepcion) {
            NotificacionUtils.mostrarInformacion("No se pudieron cargar jueces y asaltantes.");
        }
    }

    private Boolean obtenerEstadoSeleccionado() {
        return switch (filtroEstado.getValue()) {
            case "En trámite", "Reabiertos por apelación" -> true;
            case "Cerrados" -> false;
            default -> null;
        };
    }

    private String obtenerEstadoExactoSeleccionado() {
        return switch (filtroEstado.getValue()) {
            case "En trámite" -> "EN_TRAMITE";
            case "Cerrados" -> "CERRADO";
            case "Reabiertos por apelación" -> "REABIERTO_POR_APELACION";
            default -> null;
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
            case "EN_TRAMITE" -> "En trámite";
            case "REABIERTO_POR_APELACION" -> "Reabierto por apelación";
            case "CERRADO" -> "Cerrado";
            default -> obtenerEstado(caso);
        };
    }

    private void mostrarErrorOperacion(
            RestClientResponseException excepcion) {
        NotificacionUtils.mostrarAdvertencia(ErrorBackendUtils.obtenerMensaje(excepcion));
    }

    private boolean puedeEscribir() {
        return sesionUsuario.tienePermiso(PermisosApp.ESCRITURA);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarOpcionesFiltros();
        aplicarFiltroRecibido(evento);
        cargarCasos();
    }

    private void aplicarFiltroRecibido(BeforeEnterEvent evento) {
        Map<String, List<String>> parametros = evento.getLocation()
                .getQueryParameters()
                .getParameters();

        String codigoAsaltante = primerParametro(
                parametros,
                "asaltante"
        );
        String codigoJuez = primerParametro(parametros, "juez");

        if (codigoAsaltante != null) {
            AsaltanteDTO asaltante = asaltantesDisponibles.stream()
                    .filter(item -> codigoAsaltante.equalsIgnoreCase(
                    item.getCodigo()))
                    .findFirst()
                    .orElse(null);

            if (asaltante != null) {
                filtroRelacion.setValue("Por asaltante");
                filtroAsaltante.setValue(asaltante);
                configurarRegreso(
                        "asaltantes",
                        "Volver a asaltantes"
                );
            }
        } else if (codigoJuez != null) {
            JuezDTO juez = juecesDisponibles.stream()
                    .filter(item -> codigoJuez.equalsIgnoreCase(
                    item.getCodigo()))
                    .findFirst()
                    .orElse(null);

            if (juez != null) {
                filtroRelacion.setValue("Por juez");
                filtroJuez.setValue(juez);
                configurarRegreso("jueces", "Volver a jueces");
            }
        }
    }

    private void configurarRegreso(String ruta, String texto) {
        rutaVolver = ruta;
        botonVolver.setText(texto);
        botonVolver.setVisible(true);
    }

    private String primerParametro(
            Map<String, List<String>> parametros,
            String nombre) {
        List<String> valores = parametros.get(nombre);
        return valores == null || valores.isEmpty()
                ? null
                : valores.get(0);
    }
}
