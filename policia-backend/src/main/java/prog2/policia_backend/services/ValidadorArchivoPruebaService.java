package prog2.policia_backend.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import prog2.policia_backend.DTOs.ArchivoPruebaDTO;
import prog2.policia_backend.exceptions.ArchivoAdjuntoInvalidoException;

@Service
public class ValidadorArchivoPruebaService {
    private static final int LONGITUD_MAXIMA_NOMBRE = 255;
    private static final int TAMANO_MUESTRA_IMAGEN = 1024;
    private final long maximoBytes;

    public ValidadorArchivoPruebaService(@Value("${app.pruebas.archivo.max-size}") String maximo) {
        this.maximoBytes = DataSize.parse(maximo).toBytes();
        if (maximoBytes <= 0 || maximoBytes > Integer.MAX_VALUE - 1) {
            throw new IllegalArgumentException("El limite de adjuntos debe ser un numero de bytes positivo valido");
        }
    }

    public ArchivoPruebaDTO validar(List<MultipartFile> archivos) {
        if (archivos == null || archivos.isEmpty()) {
            return null;
        }
        if (archivos.size() != 1) {
            throw new ArchivoAdjuntoInvalidoException("Solo se permite un archivo adjunto.");
        }
        MultipartFile archivo = archivos.get(0);
        validarTamano(archivo.getSize());
        if (archivo.isEmpty()) {
            throw new ArchivoAdjuntoInvalidoException(ArchivoAdjuntoInvalidoException.VACIO);
        }
        String nombre = nombreSeguro(archivo.getOriginalFilename());
        int punto = nombre.lastIndexOf('.');
        String extension = punto < 0 ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        String tipoContenido = determinarTipoContenido(extension);
        byte[] contenido = leerContenido(archivo);
        if (!validarContenido(contenido, extension)) {
            throw new ArchivoAdjuntoInvalidoException(ArchivoAdjuntoInvalidoException.CONTENIDO);
        }
        return new ArchivoPruebaDTO(nombre, tipoContenido, (long) contenido.length, contenido);
    }

    private void validarTamano(long tamano) {
        if (tamano > maximoBytes) {
            throw new ArchivoAdjuntoInvalidoException(ArchivoAdjuntoInvalidoException.TAMANO);
        }
    }

    private byte[] leerContenido(MultipartFile archivo) {
        byte[] contenido;
        try (InputStream entrada = archivo.getInputStream()) {
            contenido = entrada.readNBytes((int) maximoBytes + 1);
        } catch (IOException ex) {
            throw new ArchivoAdjuntoInvalidoException("No se pudo leer el archivo adjunto.");
        }
        validarTamano(contenido.length);
        if (contenido.length == 0) {
            throw new ArchivoAdjuntoInvalidoException(ArchivoAdjuntoInvalidoException.VACIO);
        }
        return contenido;
    }

    private String determinarTipoContenido(String extension) {
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "txt":
                return "text/plain";
            default:
                throw new ArchivoAdjuntoInvalidoException(ArchivoAdjuntoInvalidoException.FORMATOS);
        }
    }

    private boolean validarContenido(byte[] contenido, String extension) {
        switch (extension) {
            case "pdf":
                return validarPdf(contenido);
            case "jpg":
            case "jpeg":
            case "png":
                return validarImagen(contenido, extension);
            case "txt":
                return validarTexto(contenido);
            default:
                return false;
        }
    }

    private String nombreSeguro(String original) {
        String nombre = original == null ? "" : original.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1).trim();
        if (nombre.isEmpty() || nombre.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new ArchivoAdjuntoInvalidoException(ArchivoAdjuntoInvalidoException.FORMATOS);
        }
        for (int i = 0; i < nombre.length(); i++) {
            if (Character.isISOControl(nombre.charAt(i))) {
                throw new ArchivoAdjuntoInvalidoException(ArchivoAdjuntoInvalidoException.FORMATOS);
            }
        }
        return nombre;
    }

    private boolean validarPdf(byte[] contenido) {
        // Loader(byte[]) utiliza buffers en memoria, sin crear archivos temporales.
        try (PDDocument documento = Loader.loadPDF(contenido)) {
            documento.getDocumentCatalog().getPages();
            return true;
        } catch (InvalidPasswordException ex) {
            // El parser reconocio un PDF cifrado; el adjunto no requiere abrirlo.
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean validarImagen(byte[] contenido, String extension) {
        try (MemoryCacheImageInputStream entrada = new MemoryCacheImageInputStream(new ByteArrayInputStream(contenido))) {
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                return false;
            }
            ImageReader lector = lectores.next();
            try {
                String esperado = "png".equals(extension) ? "PNG" : "JPEG";
                if (!esperado.equalsIgnoreCase(lector.getFormatName()) || !tieneFinalImagenValido(contenido, extension)) {
                    return false;
                }
                lector.setInput(entrada, true, true);
                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if (ancho <= 0 || alto <= 0) {
                    return false;
                }
                // Decodifica con muestreo para acotar la memoria de imagenes comprimidas grandes.
                ImageReadParam parametros = lector.getDefaultReadParam();
                parametros.setSourceSubsampling((ancho - 1) / TAMANO_MUESTRA_IMAGEN + 1,
                        (alto - 1) / TAMANO_MUESTRA_IMAGEN + 1, 0, 0);
                return lector.read(0, parametros) != null;
            } finally {
                lector.dispose();
            }
        } catch (IOException | IllegalArgumentException ex) {
            return false;
        }
    }

    private boolean tieneFinalImagenValido(byte[] contenido, String extension) {
        if (!"png".equals(extension)) {
            return contenido.length >= 4 && contenido[contenido.length - 2] == (byte) 0xff
                    && contenido[contenido.length - 1] == (byte) 0xd9;
        }
        byte[] finalPng = {0, 0, 0, 0, 73, 69, 78, 68, (byte) 174, 66, 96, (byte) 130};
        if (contenido.length < finalPng.length) {
            return false;
        }
        for (int i = 0; i < finalPng.length; i++) {
            if (contenido[contenido.length - finalPng.length + i] != finalPng[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean validarTexto(byte[] contenido) {
        // Un PDF renombrado a TXT no se considera un documento de texto.
        if (contenido.length >= 5 && new String(contenido, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
            return false;
        }
        Charset codificacion = StandardCharsets.UTF_8;
        if (contenido.length >= 2 && ((contenido[0] == (byte) 0xff && contenido[1] == (byte) 0xfe)
                || (contenido[0] == (byte) 0xfe && contenido[1] == (byte) 0xff))) {
            codificacion = StandardCharsets.UTF_16;
        }
        try {
            return esTexto(decodificarTexto(contenido, codificacion));
        } catch (CharacterCodingException ex) {
            if (codificacion != StandardCharsets.UTF_8) {
                return false;
            }
            try {
                return esTexto(decodificarTexto(contenido, Charset.forName("windows-1252")));
            } catch (CharacterCodingException error) {
                return false;
            }
        }
    }

    private String decodificarTexto(byte[] contenido, Charset codificacion) throws CharacterCodingException {
        return codificacion.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(contenido)).toString();
    }

    private boolean esTexto(String texto) {
        if (texto.isEmpty()) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);
            if (Character.isISOControl(caracter) && caracter != '\n' && caracter != '\r'
                    && caracter != '\t' && caracter != '\f') {
                return false;
            }
        }
        return true;
    }
}
