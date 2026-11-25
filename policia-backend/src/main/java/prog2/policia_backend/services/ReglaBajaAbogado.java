package prog2.policia_backend.services;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.AsaltanteRepository;

@Component
@RequiredArgsConstructor
public class ReglaBajaAbogado implements ReglaBajaUsuario {
    private final AsaltanteRepository asaltanteRepository;

    @Override
    public int prepararBaja(Usuario usuario) {
        if (!"ABOGADO_DEFENSOR".equalsIgnoreCase(usuario.getRol().getNombre())) {
            return 0;
        }
        List<Asaltante> defendidos = asaltanteRepository.findByAbogadoDefensor_Id(usuario.getId());
        for (Asaltante defendido : defendidos) {
            defendido.setAbogadoDefensor(null);
        }
        asaltanteRepository.saveAll(defendidos);
        return defendidos.size();
    }
}
