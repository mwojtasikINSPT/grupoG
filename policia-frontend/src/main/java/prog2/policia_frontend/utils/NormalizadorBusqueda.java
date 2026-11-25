package prog2.policia_frontend.utils;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Normaliza valores de los DTO para las búsquedas locales de las vistas. */
public final class NormalizadorBusqueda {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private NormalizadorBusqueda() {
    }

    public static String normalizar(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor.toString();
        if (valor instanceof LocalDate) {
            texto += " " + ((LocalDate) valor).format(FECHA);
        } else if (valor instanceof LocalDateTime) {
            texto += " " + ((LocalDateTime) valor).format(FECHA_HORA);
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
