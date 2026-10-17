package prog2.policia_frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import prog2.policia_frontend.layouts.MainLayout;
import prog2.policia_frontend.security.PermisosApp;
import prog2.policia_frontend.security.SesionUsuario;

/*
 * Pantalla utilizada para administrar casos judiciales.
 * Por ahora se accede escribiendo /casos-judiciales.
 */
@Route(
        value = "casos-judiciales",
        layout = MainLayout.class
)
@PageTitle("Casos judiciales | Sistema Policial")
public class CasosJudicialesView
        extends VerticalLayout
        implements BeforeEnterObserver {

    private final CasoJudicialClient casoJudicialClient;
    private final AsaltoClient asaltoClient;
    private final AsaltanteClient asaltanteClient;
    private final JuezClient juezClient;
    private final SesionUsuario sesionUsuario;

    private final Grid<CasoJudicialDTO> tabla
            = new Grid<>(CasoJudicialDTO.class, false);

    private final Select<String> filtroEstado
            = new Select<>();

    private final Select<String> filtroRelacion
            = new Select<>();

    private final Select<JuezDTO> filtroJuez
            = new Select<>();

    private final Select<AsaltanteDTO> filtroAsaltante
            = new Select<>();

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

        configurarFiltro();
        configurarTabla();
        configurarVista();
    }

    private void configurarFiltro() {

        filtroEstado.setLabel("Estado");
        filtroEstado.setItems("Todos", "Activos", "Cerrados");
        filtroEstado.setValue("Todos");

        filtroRelacion.setLabel("Buscar casos");
        filtroRelacion.setItems(
                "Todos",
                "Por juez",
                "Por asaltante"
        );
        filtroRelacion.setValue("Todos");

        filtroJuez.setLabel("Juez");
        filtroJuez.setItemLabelGenerator(juez ->
                juez.getCodigo() + " - " + juez.getNombre()
        );
        filtroJuez.setVisible(false);

        filtroAsaltante.setLabel("Asaltante");
        filtroAsaltante.setItemLabelGenerator(asaltante ->
                asaltante.getCodigo() + " - " + asaltante.getNombre()
        );
        filtroAsaltante.setVisible(false);

        filtroRelacion.addValueChangeListener(evento -> {

            boolean porJuez = "Por juez".equals(evento.getValue());
            boolean porAsaltante
                    = "Por asaltante".equals(evento.getValue());

            filtroJuez.setVisible(porJuez);
            filtroAsaltante.setVisible(porAsaltante);

            filtroJuez.clear();
            filtroAsaltante.clear();
        });
    }

    private void configurarTabla() {

        tabla.addColumn(CasoJudicialDTO::getCodigo)
                .setHeader("Código")
                .setAutoWidth(true);

        tabla.addColumn(CasoJudicialDTO::getAsaltoCodigo)
                .setHeader("Asalto")
                .setAutoWidth(true);

        tabla.addColumn(caso -> {
            List<String> codigos = caso.getAsaltanteCodigos();
            return codigos == null || codigos.isEmpty()
                    ? "Sin asaltantes"
                    : String.join(", ", codigos);
        }).setHeader("Asaltantes")
                .setAutoWidth(true)
                .setFlexGrow(1);

        tabla.addColumn(CasoJudicialDTO::getJuezCodigo)
                .setHeader("Juez")
                .setAutoWidth(true);

        tabla.addColumn(caso ->
                Boolean.TRUE.equals(caso.getSentenciado())
                        ? "Sí"
                        : "No"
        ).setHeader("Sentenciado")
                .setAutoWidth(true);

        tabla.addColumn(caso ->
                Boolean.TRUE.equals(caso.getCondenado())
                        ? "Sí"
                        : "No"
        ).setHeader("Condenado")
                .setAutoWidth(true);

        tabla.addColumn(caso ->
                caso.getTiempoCarcel() != null
                        ? caso.getTiempoCarcel()
                        : 0
        ).setHeader("Meses de cárcel")
                .setAutoWidth(true);

        tabla.addColumn(caso ->
                Boolean.TRUE.equals(caso.getActivo())
                        ? "Activo"
                        : "Cerrado"
        ).setHeader("Estado")
                .setAutoWidth(true);

        tabla.addComponentColumn(caso -> {

            /*
             * Consultar un caso requiere lectura. Cambiar sus relaciones,
             * sentencia, condena, cárcel o estado requiere escritura.
             */
            if (!puedeEscribir()) {
                return new Span("Solo lectura");
            }

            boolean estaActivo = Boolean.TRUE.equals(caso.getActivo());

            Button botonModificar = new Button("Modificar");
            botonModificar.setEnabled(estaActivo);

            if (estaActivo) {
                botonModificar.addClickListener(
                        evento -> abrirFormulario(caso)
                );
            } else {
                botonModificar.setTooltipText(
                        "Debe reactivar el caso antes de modificarlo."
                );
            }

            Button botonEstado = new Button(
                    estaActivo ? "Cerrar" : "Reactivar"
            );

            if (estaActivo) {
                botonEstado.addClickListener(
                        evento -> confirmarCierre(caso)
                );
            } else {
                botonEstado.addClickListener(
                        evento -> reactivarCaso(caso)
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

    private void configurarVista() {

        H2 titulo = new H2("Administración de casos judiciales");

        Button botonNuevo = new Button(
                "Nuevo caso",
                evento -> abrirFormulario(null)
        );

        botonNuevo.setVisible(
                puedeEscribir()
        );

        Button botonBuscar = new Button(
                "Buscar",
                evento -> cargarCasos()
        );

        Button botonLimpiar = new Button(
                "Limpiar",
                evento -> {
                    filtroEstado.setValue("Todos");
                    filtroRelacion.setValue("Todos");
                    filtroJuez.clear();
                    filtroAsaltante.clear();
                    cargarCasos();
                }
        );

        HorizontalLayout filtros = new HorizontalLayout(
                filtroRelacion,
                filtroJuez,
                filtroAsaltante,
                filtroEstado,
                botonBuscar,
                botonLimpiar
        );
        filtros.setAlignItems(HorizontalLayout.Alignment.END);

        setSizeFull();
        add(titulo, botonNuevo, filtros, tabla);
        expand(tabla);
    }

    private void abrirFormulario(CasoJudicialDTO caso) {

        boolean esCreacion = caso == null;

        List<AsaltoDTO> asaltos;
        List<AsaltanteDTO> asaltantesActivos;
        List<JuezDTO> juecesActivos;

        try {

            asaltos = asaltoClient.listar();
            asaltantesActivos = asaltanteClient.listar(true, null);
            juecesActivos = juezClient.listar(true, null);

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);
            return;

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se pueden consultar los datos necesarios."
            );
            return;
        }

        if (asaltos.isEmpty() || juecesActivos.isEmpty()) {

            Notification.show(
                    "Se necesita al menos un asalto y un juez activo."
            );
            return;
        }

        Dialog dialogo = new Dialog();
        dialogo.setWidth("650px");
        dialogo.setHeaderTitle(
                esCreacion
                        ? "Nuevo caso judicial"
                        : "Modificar " + caso.getCodigo()
        );

        Select<AsaltoDTO> campoAsalto = new Select<>();
        campoAsalto.setLabel("Asalto");
        campoAsalto.setItems(asaltos);
        campoAsalto.setItemLabelGenerator(asalto ->
                asalto.getCodigo()
                        + " - "
                        + asalto.getFecha()
        );
        campoAsalto.setRequiredIndicatorVisible(true);
        campoAsalto.setWidthFull();

        Select<JuezDTO> campoJuez = new Select<>();
        campoJuez.setLabel("Juez");
        campoJuez.setItems(juecesActivos);
        campoJuez.setItemLabelGenerator(juez ->
                juez.getCodigo()
                        + " - "
                        + juez.getNombre()
        );
        campoJuez.setRequiredIndicatorVisible(true);
        campoJuez.setWidthFull();

        MultiSelectComboBox<AsaltanteDTO> campoAsaltantes
                = new MultiSelectComboBox<>(
                        esCreacion
                                ? "Asaltantes acusados"
                                : "Agregar asaltantes"
                );

        campoAsaltantes.setItemLabelGenerator(asaltante ->
                asaltante.getCodigo()
                        + " - "
                        + asaltante.getNombre()
        );
        campoAsaltantes.setRequiredIndicatorVisible(esCreacion);
        campoAsaltantes.setWidthFull();

        Span participantesActuales = new Span();

        Checkbox campoCondenado = new Checkbox("Condenado");
        Checkbox campoSentenciado = new Checkbox("Dictar sentencia y cerrar");

        IntegerField campoTiempoCarcel
                = new IntegerField("Meses de cárcel");
        campoTiempoCarcel.setMin(0);
        campoTiempoCarcel.setStepButtonsVisible(true);
        campoTiempoCarcel.setValue(0);
        campoTiempoCarcel.setEnabled(false);
        campoTiempoCarcel.setWidthFull();

        campoCondenado.addValueChangeListener(evento -> {

            boolean condenado = Boolean.TRUE.equals(evento.getValue());
            campoTiempoCarcel.setEnabled(condenado);

            if (!condenado) {
                campoTiempoCarcel.setValue(0);
            }
        });

        if (esCreacion) {

            campoAsalto.addValueChangeListener(evento -> {
                actualizarParticipantesDisponibles(
                        campoAsaltantes,
                        evento.getValue(),
                        asaltantesActivos,
                        Set.of()
                );
            });

            campoCondenado.setVisible(false);
            campoSentenciado.setVisible(false);
            campoTiempoCarcel.setVisible(false);
            participantesActuales.setVisible(false);

        } else {

            AsaltoDTO asaltoActual = asaltos.stream()
                    .filter(asalto
                            -> asalto.getCodigo() != null
                            && asalto.getCodigo().equalsIgnoreCase(
                                    caso.getAsaltoCodigo()
                            ))
                    .findFirst()
                    .orElse(null);

            campoAsalto.setValue(asaltoActual);
            campoAsalto.setEnabled(false);

            JuezDTO juezActual = juecesActivos.stream()
                    .filter(juez
                            -> juez.getCodigo() != null
                            && juez.getCodigo().equalsIgnoreCase(
                                    caso.getJuezCodigo()
                            ))
                    .findFirst()
                    .orElse(null);

            if (juezActual != null) {
                campoJuez.setValue(juezActual);
            }

            Set<String> codigosActuales = new HashSet<>(
                    caso.getAsaltanteCodigos() != null
                            ? caso.getAsaltanteCodigos()
                            : List.of()
            );

            participantesActuales.setText(
                    "Asaltantes actuales: "
                            + (codigosActuales.isEmpty()
                                    ? "ninguno"
                                    : String.join(", ", codigosActuales))
            );

            actualizarParticipantesDisponibles(
                    campoAsaltantes,
                    asaltoActual,
                    asaltantesActivos,
                    codigosActuales
            );

            campoCondenado.setValue(
                    Boolean.TRUE.equals(caso.getCondenado())
            );
            campoTiempoCarcel.setValue(
                    caso.getTiempoCarcel() != null
                            ? caso.getTiempoCarcel()
                            : 0
            );
            campoTiempoCarcel.setEnabled(
                    Boolean.TRUE.equals(caso.getCondenado())
            );
        }

        Button botonGuardar = new Button(
                esCreacion ? "Guardar" : "Guardar cambios"
        );

        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        botonGuardar.addClickListener(evento -> {

            boolean asaltoVacio = campoAsalto.getValue() == null;
            boolean juezVacio = campoJuez.getValue() == null;
            boolean asaltantesVacios
                    = esCreacion
                    && campoAsaltantes.getSelectedItems().isEmpty();

            int meses = campoTiempoCarcel.getValue() != null
                    ? campoTiempoCarcel.getValue()
                    : 0;

            boolean condenaInvalida
                    = !esCreacion
                    && Boolean.TRUE.equals(campoCondenado.getValue())
                    && meses <= 0;

            campoAsalto.setInvalid(asaltoVacio);
            campoAsalto.setErrorMessage("Debe seleccionar un asalto.");

            campoJuez.setInvalid(juezVacio);
            campoJuez.setErrorMessage("Debe seleccionar un juez.");

            campoAsaltantes.setInvalid(asaltantesVacios);
            campoAsaltantes.setErrorMessage(
                    "Debe seleccionar al menos un asaltante."
            );

            campoTiempoCarcel.setInvalid(condenaInvalida);
            campoTiempoCarcel.setErrorMessage(
                    "Una condena debe tener al menos un mes de cárcel."
            );

            if (asaltoVacio
                    || juezVacio
                    || asaltantesVacios
                    || condenaInvalida) {
                return;
            }

            List<String> seleccionados
                    = campoAsaltantes.getSelectedItems()
                            .stream()
                            .map(AsaltanteDTO::getCodigo)
                            .toList();

            CasoJudicialDTO datos = new CasoJudicialDTO();

            if (esCreacion) {

                datos.setAsaltoCodigo(
                        campoAsalto.getValue().getCodigo()
                );
                datos.setAsaltanteCodigos(seleccionados);
                datos.setJuezCodigo(
                        campoJuez.getValue().getCodigo()
                );

            } else {

                datos.setJuezCodigo(
                        campoJuez.getValue().getCodigo()
                );

                if (!seleccionados.isEmpty()) {
                    datos.setAsaltanteCodigos(seleccionados);
                } else {
                    datos.setAsaltanteCodigos(null);
                }

                datos.setCondenado(campoCondenado.getValue());
                datos.setTiempoCarcel(meses);
                datos.setSentenciado(campoSentenciado.getValue());
            }

            try {

                if (esCreacion) {
                    casoJudicialClient.crear(datos);
                } else {
                    casoJudicialClient.actualizar(
                            caso.getCodigo(),
                            datos
                    );
                }

                dialogo.close();
                cargarCasos();

                Notification.show(
                        esCreacion
                                ? "Caso judicial creado correctamente."
                                : "Caso judicial modificado correctamente."
                );

            } catch (RestClientResponseException excepcion) {

                mostrarErrorOperacion(excepcion);

            } catch (ResourceAccessException excepcion) {

                Notification.show("No se puede conectar con el backend.");

            } catch (IllegalArgumentException excepcion) {

                Notification.show(excepcion.getMessage());
            }
        });

        dialogo.add(
                new VerticalLayout(
                        campoAsalto,
                        campoJuez,
                        participantesActuales,
                        campoAsaltantes,
                        campoCondenado,
                        campoTiempoCarcel,
                        campoSentenciado,
                        new HorizontalLayout(
                                botonGuardar,
                                botonCancelar
                        )
                )
        );

        dialogo.open();
    }

    private void actualizarParticipantesDisponibles(
            MultiSelectComboBox<AsaltanteDTO> campo,
            AsaltoDTO asalto,
            List<AsaltanteDTO> asaltantesActivos,
            Set<String> excluirCodigos) {

        campo.clear();

        if (asalto == null || asalto.getAsaltantesCodigos() == null) {
            campo.setItems(List.of());
            return;
        }

        List<AsaltanteDTO> participantes = asaltantesActivos.stream()
                .filter(asaltante
                        -> asaltante.getCodigo() != null
                        && asalto.getAsaltantesCodigos().stream()
                                .anyMatch(codigo
                                        -> codigo.equalsIgnoreCase(
                                                asaltante.getCodigo()
                                        ))
                        && excluirCodigos.stream()
                                .noneMatch(codigo
                                        -> codigo.equalsIgnoreCase(
                                                asaltante.getCodigo()
                                        )))
                .toList();

        campo.setItems(participantes);
    }

    private void confirmarCierre(CasoJudicialDTO caso) {

        Dialog dialogo = new Dialog();
        dialogo.setHeaderTitle("Cerrar " + caso.getCodigo());

        Span explicacion = new Span(
                "El caso quedará inactivo. Esta acción no carga una sentencia."
        );

        Button botonConfirmar = new Button(
                "Confirmar cierre",
                evento -> {
                    cerrarCaso(caso);
                    dialogo.close();
                }
        );

        Button botonCancelar = new Button(
                "Cancelar",
                evento -> dialogo.close()
        );

        dialogo.add(
                new VerticalLayout(
                        explicacion,
                        new HorizontalLayout(
                                botonConfirmar,
                                botonCancelar
                        )
                )
        );
        dialogo.open();
    }

    private void cerrarCaso(CasoJudicialDTO caso) {

        try {

            casoJudicialClient.cerrar(caso.getCodigo());
            cargarCasos();
            Notification.show("Caso judicial cerrado correctamente.");

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show("No se puede conectar con el backend.");
        }
    }

    private void reactivarCaso(CasoJudicialDTO caso) {

        try {

            casoJudicialClient.reactivar(caso.getCodigo());
            cargarCasos();
            Notification.show("Caso judicial reactivado correctamente.");

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show("No se puede conectar con el backend.");
        }
    }

    private Boolean obtenerEstado() {

        return switch (filtroEstado.getValue()) {
            case "Activos" -> true;
            case "Cerrados" -> false;
            default -> null;
        };
    }

    /*
     * Centraliza el permiso necesario para crear, modificar,
     * cerrar o reactivar un caso judicial.
     */
    private boolean puedeEscribir() {

        return sesionUsuario.tienePermiso(
                PermisosApp.ESCRITURA
        );
    }

    private void cargarCasos() {

        try {

            String tipoBusqueda = filtroRelacion.getValue();
            Boolean estado = obtenerEstado();

            List<CasoJudicialDTO> casos;

            if ("Por juez".equals(tipoBusqueda)) {

                JuezDTO juez = filtroJuez.getValue();

                if (juez == null) {
                    Notification.show(
                            "Seleccione un juez."
                    );
                    return;
                }

                casos = casoJudicialClient.listarPorJuez(
                        juez.getCodigo()
                );

            } else if ("Por asaltante".equals(tipoBusqueda)) {

                AsaltanteDTO asaltante = filtroAsaltante.getValue();

                if (asaltante == null) {
                    Notification.show(
                            "Seleccione un asaltante."
                    );
                    return;
                }

                casos = casoJudicialClient.listarPorAsaltante(
                        asaltante.getCodigo()
                );

            } else {

                casos = casoJudicialClient.listar(estado);
            }

            /*
             * Los endpoints por juez y por asaltante devuelven
             * todos sus casos. Si se eligió un estado, se filtra
             * el resultado en la propia pantalla.
             */
            if (!"Todos".equals(tipoBusqueda) && estado != null) {

                casos = casos.stream()
                        .filter(caso
                                -> estado.equals(caso.getActivo()))
                        .toList();
            }

            tabla.setItems(casos);

            if (!"Todos".equals(tipoBusqueda) && casos.isEmpty()) {

                String persona = "Por juez".equals(tipoBusqueda)
                        ? "El juez seleccionado"
                        : "El asaltante seleccionado";

                String aclaracionEstado = estado != null
                        ? " con el estado seleccionado"
                        : "";

                Notification.show(
                        persona
                                + " no tiene casos judiciales"
                                + aclaracionEstado
                                + "."
                );
            }

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show("No se puede conectar con el backend.");

        } catch (IllegalStateException excepcion) {

            Notification.show("Debe iniciar sesión.");

        } catch (IllegalArgumentException excepcion) {

            Notification.show(excepcion.getMessage());
        }
    }

    /*
     * Carga las opciones reales para que el usuario no tenga
     * que recordar ni escribir códigos manualmente.
     */
    private void cargarOpcionesFiltros() {

        try {

            filtroJuez.setItems(juezClient.listar(null, null));
            filtroAsaltante.setItems(
                    asaltanteClient.listar(null, null)
            );

        } catch (RestClientResponseException excepcion) {

            mostrarErrorOperacion(excepcion);

        } catch (ResourceAccessException excepcion) {

            Notification.show(
                    "No se pudieron cargar jueces y asaltantes."
            );
        }
    }

    private void mostrarErrorOperacion(
            RestClientResponseException excepcion) {

        int estado = excepcion.getStatusCode().value();
        String mensajeBackend = obtenerMensajeBackend(excepcion);

        if (estado == 400) {
            Notification.show(
                    mensajeBackend != null
                            ? mensajeBackend
                            : "Los datos ingresados no son válidos."
            );
        } else if (estado == 401) {
            Notification.show("La sesión no es válida.");
        } else if (estado == 403) {
            Notification.show(
                    "No tiene permiso para realizar esta operación."
            );
        } else if (estado == 404) {
            Notification.show(
                    mensajeBackend != null
                            ? mensajeBackend
                            : "No se encontró el recurso solicitado."
            );
        } else if (estado == 409) {
            Notification.show(
                    mensajeBackend != null
                            ? mensajeBackend
                            : "La relación o el estado elegido no está permitido."
            );
        } else {
            Notification.show("No se pudo completar la operación.");
        }
    }

    /*
     * Obtiene el texto comprensible que el backend envía en el JSON.
     * No muestra detalles internos de los errores 500.
     */
    private String obtenerMensajeBackend(
            RestClientResponseException excepcion) {

        String cuerpo = excepcion.getResponseBodyAsString();

        if (cuerpo == null || cuerpo.isBlank()) {
            return null;
        }

        Pattern patron = Pattern.compile(
                "\\\"(?:mensaje|error)\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\""
        );

        Matcher coincidencia = patron.matcher(cuerpo);

        if (!coincidencia.find()) {
            return null;
        }

        return coincidencia.group(1)
                .replace("\\\\\"", "\"")
                .replace("\\\\n", " ")
                .replace("\\\\\\\\", "\\");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent evento) {
        cargarOpcionesFiltros();
        cargarCasos();
    }
}
