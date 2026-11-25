package prog2.policia_frontend.components;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.FileRejectedEvent;
import com.vaadin.flow.component.upload.FileRemovedEvent;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import prog2.policia_frontend.clients.PruebaDefensaClient;
import prog2.policia_frontend.dtos.PresentacionPruebaDefensaDTO;
import prog2.policia_frontend.dtos.PruebaDefensaDTO;
import prog2.policia_frontend.security.PermisosInterfaz;
import prog2.policia_frontend.utils.NormalizadorBusqueda;
import prog2.policia_frontend.utils.ErrorBackendUtils;
import prog2.policia_frontend.utils.NotificacionUtils;

/** Consulta compartida; solo el defensor autorizado dispone del formulario de presentacion. */
public class PruebasDefensaDialog extends Dialog {
    private final Select<String> criterioBusqueda = new Select<>();
    private final com.vaadin.flow.component.textfield.TextField valorBusqueda = new com.vaadin.flow.component.textfield.TextField("Buscar");
    private List<PruebaDefensaDTO> datosBusqueda = List.of();

    private final PruebaDefensaClient client;
    private final String asaltante;
    private final String caso;
    private final Grid<PruebaDefensaDTO> tabla = new Grid<>(PruebaDefensaDTO.class, false);
    private final TextArea descripcion = new TextArea("Descripción de la prueba");
    private final Span estadoArchivo = new Span("Sin archivo adjunto.");
    private final Button guardar = new Button("Presentar prueba");
    private Upload subida;
    private byte[] contenido;
    private String nombreArchivo;
    private String tipoArchivo;
    private volatile boolean recibiendoArchivo;

    public PruebasDefensaDialog(PruebaDefensaClient client, PermisosInterfaz permisos,
            String asaltante, String caso, boolean casoAbierto) {
        this.client = client; this.asaltante = asaltante; this.caso = caso;
        setHeaderTitle("Pruebas de " + asaltante + " · " + caso);
        setWidth("1050px"); setMaxWidth("95vw");
        configurarBusqueda();
        configurarTabla();
        Button actualizar = new Button("Actualizar", new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> evento) { cargarPruebas(); }
        });
        VerticalLayout cuerpo = new VerticalLayout(actualizar, crearBarraBusqueda(), tabla);
        cuerpo.setPadding(false);
        if (permisos.puedePresentarPruebas() && casoAbierto) {
            cuerpo.add(crearFormulario());
        } else {
            cuerpo.add(new Paragraph(casoAbierto ? "Consulta de pruebas de defensa." : "Consulta de pruebas. La presentación no está habilitada para este caso o usuario."));
        }
        Button cerrar = new Button("Cerrar", new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> evento) { close(); }
        });
        add(cuerpo); getFooter().add(cerrar);
        addOpenedChangeListener(new ComponentEventListener<OpenedChangeEvent>() {
            @Override
            public void onComponentEvent(OpenedChangeEvent evento) { if (!evento.isOpened()) { limpiarArchivo(); } }
        });
        cargarPruebas();
    }

    private void configurarTabla() {
        tabla.addColumn(new ValueProvider<PruebaDefensaDTO, Long>() {
            @Override public Long apply(PruebaDefensaDTO p) { return p.getId(); }
        }).setHeader("ID").setAutoWidth(true);
        tabla.addColumn(new ValueProvider<PruebaDefensaDTO, String>() {
            @Override public String apply(PruebaDefensaDTO p) { return p.getDescripcion(); }
        }).setHeader("Descripción").setFlexGrow(2);
        tabla.addComponentColumn(new ValueProvider<PruebaDefensaDTO, Component>() {
            @Override public Component apply(PruebaDefensaDTO prueba) {
                return new Button("Ver descripción", new ComponentEventListener<ClickEvent<Button>>() {
                    @Override public void onComponentEvent(ClickEvent<Button> evento) { mostrarDescripcion(prueba); }
                });
            }
        }).setHeader("Detalle").setAutoWidth(true);
        tabla.addColumn(new ValueProvider<PruebaDefensaDTO, Object>() {
            @Override public Object apply(PruebaDefensaDTO p) { return p.getFechaPresentacion(); }
        }).setHeader("Presentada").setAutoWidth(true);
        tabla.addColumn(new ValueProvider<PruebaDefensaDTO, String>() {
            @Override public String apply(PruebaDefensaDTO p) { return p.getAbogadoCodigo(); }
        }).setHeader("Autor original").setAutoWidth(true);
        tabla.addColumn(new ValueProvider<PruebaDefensaDTO, String>() {
            @Override public String apply(PruebaDefensaDTO p) {
                return p.getNombreArchivo() == null ? "Sin adjunto" : p.getNombreArchivo() + " (" + p.getTamanoArchivo() + " bytes)";
            }
        }).setHeader("Archivo").setAutoWidth(true);
        tabla.addComponentColumn(new ValueProvider<PruebaDefensaDTO, Component>() {
            @Override public Component apply(PruebaDefensaDTO prueba) {
                if (prueba.getNombreArchivo() == null) { return new Span("-"); }
                return new Anchor(new DownloadHandler() {
                    @Override public void handleDownloadRequest(DownloadEvent evento) throws IOException { descargar(prueba, evento); }
                }, "Descargar");
            }
        }).setHeader("Descarga").setAutoWidth(true);
        tabla.setWidthFull(); tabla.setHeight("300px");
    }

    private void mostrarDescripcion(PruebaDefensaDTO prueba) {
        Dialog detalle = new Dialog();
        detalle.setHeaderTitle("Descripción de la prueba " + prueba.getId());
        detalle.setWidth("700px");
        detalle.setMaxWidth("95vw");
        Paragraph texto = new Paragraph(prueba.getDescripcion());
        texto.getStyle().set("white-space", "pre-wrap").set("overflow-wrap", "anywhere");
        Button cerrar = new Button("Cerrar", new ComponentEventListener<ClickEvent<Button>>() {
            @Override public void onComponentEvent(ClickEvent<Button> evento) { detalle.close(); }
        });
        detalle.add(texto);
        detalle.getFooter().add(cerrar);
        detalle.open();
    }

    private Component crearFormulario() {
        descripcion.setRequiredIndicatorVisible(true); descripcion.setMaxLength(4000); descripcion.setWidthFull();
        descripcion.setManualValidation(true);
        descripcion.setHelperText("Obligatoria. Máximo 4000 caracteres.");
        subida = new Upload(new UploadHandler() {
            @Override public long getFileSizeMax() { return PruebaDefensaClient.MAXIMO_ARCHIVO; }
            @Override public long getRequestSizeMax() { return 3145728; }
            @Override public long getFileCountMax() { return 1; }
            @Override public void handleUploadRequest(UploadEvent evento) throws IOException { recibirArchivo(evento); }
            @Override public void responseHandled(UploadResult resultado) {
                UploadHandler.super.responseHandled(resultado);
                if (resultado.exception() != null) {
                    java.util.Optional<com.vaadin.flow.component.UI> ui = PruebasDefensaDialog.this.getUI();
                    if (ui.isPresent()) {
                        ui.get().access(new Command() {
                            @Override public void execute() {
                                recibiendoArchivo = false; limpiarArchivo(); guardar.setEnabled(true);
                                estadoArchivo.setText("No se pudo recibir el archivo. Seleccione otro archivo.");
                            }
                        });
                    }
                }
            }
        });
        subida.setMaxFiles(1); subida.setMaxFileSize(PruebaDefensaClient.MAXIMO_ARCHIVO);
        subida.setAcceptedFileExtensions(".pdf", ".jpg", ".jpeg", ".png", ".txt");
        subida.setUploadButton(new Button("Seleccionar archivo"));
        subida.setDropLabel(new Span("Arrastre un archivo aquí"));
        subida.setI18n(new UploadI18N().setError(new UploadI18N.Error()
                .setFileIsTooBig("El archivo no puede superar los 2 MB.")
                .setIncorrectFileType("Los formatos permitidos son PDF, JPG, JPEG, PNG y TXT.")
                .setTooManyFiles("Solo se permite un archivo adjunto.")));
        subida.addFileRejectedListener(new ComponentEventListener<FileRejectedEvent>() {
            @Override public void onComponentEvent(FileRejectedEvent evento) {
                limpiarArchivo(); estadoArchivo.setText(evento.getErrorMessage()); guardar.setEnabled(true);
            }
        });
        subida.addFileRemovedListener(new ComponentEventListener<FileRemovedEvent>() {
            @Override public void onComponentEvent(FileRemovedEvent evento) { limpiarArchivo(); guardar.setEnabled(true); }
        });
        guardar.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override public void onComponentEvent(ClickEvent<Button> evento) { confirmarPresentacion(); }
        });
        return new VerticalLayout(new Paragraph("Adjunto opcional: un PDF, JPG, JPEG, PNG o TXT. Máximo 2 MB (2.097.152 bytes). No se admiten archivos vacíos."),
                descripcion, subida, estadoArchivo, guardar);
    }

    private void recibirArchivo(UploadEvent evento) throws IOException {
        recibiendoArchivo = true;
        evento.getUI().access(new Command() {
            @Override public void execute() { guardar.setEnabled(false); }
        });
        try {
            client.validarAdjuntoDeclarado(evento.getFileName(), evento.getFileSize(), evento.getContentType());
            byte[] bytes;
            try (InputStream entrada = evento.getInputStream()) { bytes = entrada.readNBytes(PruebaDefensaClient.MAXIMO_ARCHIVO + 1); }
            client.validarAdjuntoDeclarado(evento.getFileName(), bytes.length, evento.getContentType());
            evento.getUI().access(new Command() {
                @Override public void execute() {
                    recibiendoArchivo = false;
                    if (!isOpened()) { return; }
                    contenido = bytes; nombreArchivo = evento.getFileName(); tipoArchivo = evento.getContentType();
                    estadoArchivo.setText(nombreArchivo + " · " + bytes.length + " bytes. Listo para presentar."); guardar.setEnabled(true);
                }
            });
        } catch (IllegalArgumentException ex) {
            evento.reject(ex.getMessage());
            evento.getUI().access(new Command() {
                @Override public void execute() { recibiendoArchivo = false; limpiarArchivo(); estadoArchivo.setText(ex.getMessage()); guardar.setEnabled(true); }
            });
        }
    }

    private void confirmarPresentacion() {
        if (recibiendoArchivo) { NotificacionUtils.mostrarAdvertencia("Espere a que termine de recibir el archivo."); return; }
        String texto = descripcion.getValue();
        boolean vacia = texto.isBlank();
        boolean demasiadoLarga = texto.length() > 4000;
        descripcion.setInvalid(vacia || demasiadoLarga);
        descripcion.setErrorMessage(vacia ? "La descripción es obligatoria."
                : demasiadoLarga ? "La descripción no puede superar los 4000 caracteres." : null);
        if (descripcion.isInvalid()) { return; }
        try {
            if (contenido != null) { client.validarAdjuntoDeclarado(nombreArchivo, contenido.length, tipoArchivo); }
        } catch (IllegalArgumentException ex) {
            mostrarError(ex);
            return;
        }
        ConfirmDialog confirmacion = new ConfirmDialog();
        confirmacion.setHeader("Presentar prueba de defensa");
        confirmacion.setText("¿Está seguro de que desea presentar esta prueba? Una vez presentada, no podrá modificarla ni eliminarla");
        confirmacion.setCancelable(true);
        confirmacion.setCancelText("Cancelar");
        confirmacion.setConfirmText("Confirmar presentación");
        confirmacion.addConfirmListener(new ComponentEventListener<ConfirmDialog.ConfirmEvent>() {
            @Override public void onComponentEvent(ConfirmDialog.ConfirmEvent evento) { presentar(); }
        });
        confirmacion.open();
    }

    private void presentar() {
        PresentacionPruebaDefensaDTO datos = new PresentacionPruebaDefensaDTO();
        datos.setAsaltanteCodigo(asaltante); datos.setCasoCodigo(caso); datos.setDescripcion(descripcion.getValue().trim());
        try {
            client.presentar(datos, nombreArchivo, tipoArchivo, contenido);
            descripcion.clear();
            descripcion.setInvalid(false);
            descripcion.setErrorMessage(null);
            limpiarArchivo(); subida.clearFileList(); cargarPruebas();
            NotificacionUtils.mostrarExito("Prueba presentada correctamente.");
        } catch (RestClientResponseException | ResourceAccessException | IllegalArgumentException ex) {
            mostrarError(ex); limpiarArchivo(); subida.clearFileList();
        }
    }

    private void cargarPruebas() {
        try { datosBusqueda = client.consultar(asaltante, caso); aplicarFiltros(); }
        catch (RestClientResponseException | ResourceAccessException ex) { tabla.setItems(java.util.List.of()); mostrarError(ex); }
    }

    private void descargar(PruebaDefensaDTO prueba, DownloadEvent evento) throws IOException {
        try {
            ResponseEntity<byte[]> respuesta = client.descargar(prueba.getId());
            byte[] bytes = respuesta.getBody();
            if (bytes == null) { throw new IOException("El backend no devolvió el archivo."); }
            evento.setFileName(respuesta.getHeaders().getContentDisposition().getFilename());
            evento.setContentType(respuesta.getHeaders().getContentType() == null ? "application/octet-stream" : respuesta.getHeaders().getContentType().toString());
            evento.setContentLength(bytes.length);
            evento.getResponse().setHeader("Cache-Control", "no-store");
            evento.getResponse().setHeader("X-Content-Type-Options", "nosniff");
            evento.getOutputStream().write(bytes);
        } catch (RestClientResponseException | ResourceAccessException ex) {
            String mensaje = mensajeError(ex);
            evento.getResponse().setStatus(ex instanceof RestClientResponseException ? ((RestClientResponseException) ex).getStatusCode().value() : 503);
            evento.setContentType("text/plain;charset=UTF-8");
            evento.getOutputStream().write(mensaje.getBytes(StandardCharsets.UTF_8));
            evento.getUI().access(new Command() {
                @Override public void execute() { NotificacionUtils.mostrarAdvertencia(mensaje); }
            });
        }
    }

    private void limpiarArchivo() { contenido = null; nombreArchivo = null; tipoArchivo = null; estadoArchivo.setText("Sin archivo adjunto."); }
    private String mensajeError(RuntimeException ex) {
        if (ex instanceof RestClientResponseException) { return ErrorBackendUtils.obtenerMensaje((RestClientResponseException) ex); }
        if (ex instanceof ResourceAccessException) { return "No se puede conectar con el backend."; }
        return ex.getMessage();
    }
    private void mostrarError(RuntimeException ex) { NotificacionUtils.mostrarAdvertencia(mensajeError(ex)); }

    private void configurarBusqueda() {
        criterioBusqueda.setLabel("Criterio de búsqueda");
        criterioBusqueda.setItems("Todas", "Identificador", "Descripción", "Fecha de presentación", "Autor original", "Archivo");
        criterioBusqueda.setValue("Todas");
        valorBusqueda.setClearButtonVisible(true);
        valorBusqueda.setPlaceholder("Ingrese una coincidencia");
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

                aplicarFiltros();
            }
        });
        HorizontalLayout barra = new HorizontalLayout(criterioBusqueda, valorBusqueda, buscar, limpiar);
        barra.setAlignItems(HorizontalLayout.Alignment.END);
        return barra;
    }

    private void aplicarFiltros() {
        String criterio = criterioBusqueda.getValue();
        String texto = NormalizadorBusqueda.normalizar(valorBusqueda.getValue());
        List<PruebaDefensaDTO> filtrados = new java.util.ArrayList<>();
        for (PruebaDefensaDTO dato : datosBusqueda) {

            if (coincideCriterio(dato, criterio, texto)) { filtrados.add(dato); }
        }
        tabla.setItems(filtrados);
    }

    private boolean coincideCriterio(PruebaDefensaDTO dato, String criterio, String texto) {
        if ("Identificador".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getId()).contains(texto); }
        if ("Descripción".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getDescripcion()).contains(texto); }
        if ("Fecha de presentación".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getFechaPresentacion()).contains(texto); }
        if ("Autor original".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getAbogadoCodigo()).contains(texto); }
        if ("Archivo".equals(criterio)) { return NormalizadorBusqueda.normalizar(dato.getNombreArchivo()).contains(texto); }
        return true;
    }

}
