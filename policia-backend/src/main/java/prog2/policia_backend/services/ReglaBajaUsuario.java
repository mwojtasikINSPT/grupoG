package prog2.policia_backend.services;

import prog2.policia_backend.models.Usuario;

/** Regla ejecutada exclusivamente antes de la baja del usuario. */
public interface ReglaBajaUsuario {
    int prepararBaja(Usuario usuario);
}
