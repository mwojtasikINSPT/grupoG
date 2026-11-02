package prog2.policia_backend.utils;

import java.time.LocalDate;
import java.time.Period;
import prog2.policia_backend.exceptions.EdadInvalidaException;

public final class ValidadorEdad {

    private ValidadorEdad() {
    }

    public static void validar(LocalDate fechaNacimiento, int edadMinima, int edadMaxima) {
        if (fechaNacimiento == null || fechaNacimiento.isAfter(LocalDate.now())) {
            throw new EdadInvalidaException();
        }

        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();

        if (edad < edadMinima || edad > edadMaxima) {
            throw new EdadInvalidaException();
        }
    }
}
