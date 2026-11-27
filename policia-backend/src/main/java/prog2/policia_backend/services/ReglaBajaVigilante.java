package prog2.policia_backend.services;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import prog2.policia_backend.exceptions.VigilanteConContratoFuturoException;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.ContratoVigilanciaRepository;

@Component
@RequiredArgsConstructor
public class ReglaBajaVigilante implements ReglaBajaUsuario {
    private final ContratoVigilanciaRepository contratoVigilanciaRepository;

    @Override
    public int prepararBaja(Usuario usuario) {
        if (contratoVigilanciaRepository.existsByVigilante_IdAndFechaGreaterThanEqualAndActivoTrue(usuario.getId(), LocalDate.now())) {
            throw new VigilanteConContratoFuturoException();
        }
        return 0;
    }
}
