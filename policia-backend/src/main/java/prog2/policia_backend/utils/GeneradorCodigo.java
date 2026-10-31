package prog2.policia_backend.utils;

public class GeneradorCodigo {

    public static String generar(String prefijo, Long numero) {
        return String.format("%s%05d", prefijo, numero);
    }
}