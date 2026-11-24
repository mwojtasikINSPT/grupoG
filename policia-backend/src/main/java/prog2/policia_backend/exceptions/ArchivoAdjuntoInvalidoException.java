package prog2.policia_backend.exceptions;

public class ArchivoAdjuntoInvalidoException extends RuntimeException {
    public static final String TAMANO = "El archivo no puede superar los 2 MB.";
    public static final String FORMATOS = "Los formatos permitidos son PDF, JPG, JPEG, PNG y TXT.";
    public static final String CONTENIDO = "El contenido del archivo no corresponde a un formato permitido.";
    public static final String VACIO = "El archivo está vacío.";

    public ArchivoAdjuntoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
