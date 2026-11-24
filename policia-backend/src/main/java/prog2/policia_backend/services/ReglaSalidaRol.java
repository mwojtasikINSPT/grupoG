package prog2.policia_backend.services;

import prog2.policia_backend.models.Usuario;

/** Regla ejecutada antes de una baja o de abandonar el rol actual. */
public interface ReglaSalidaRol {
    int prepararSalida(Usuario usuario);
}
