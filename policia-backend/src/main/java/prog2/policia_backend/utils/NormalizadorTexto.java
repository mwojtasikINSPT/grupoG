package prog2.policia_backend.utils;

import java.text.Normalizer;

public final class NormalizadorTexto {

    private NormalizadorTexto() {
    }

    public static String normalizarParaGuardar(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }

        texto = texto.trim().toLowerCase();

        String[] palabras = texto.split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            resultado.append(Character.toUpperCase(palabra.charAt(0)))
                    .append(palabra.substring(1))
                    .append(" ");
        }

        return resultado.toString().trim();
    }

    public static String normalizarParaBuscar(String texto) {
        if (texto == null) {
            return null;
        }

        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}