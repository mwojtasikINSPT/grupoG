package prog2.policia_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prog2.policia_backend.DTOs.AbogadoDisponibleDTO;
import prog2.policia_backend.DTOs.ResultadoAsignacionAbogadoDTO;
import prog2.policia_backend.exceptions.AbogadoInactivoException;
import prog2.policia_backend.exceptions.AbogadoNoEncontradoException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.exceptions.RolAbogadoInvalidoException;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.UsuarioRepository;

@Service
@RequiredArgsConstructor
@PreAuthorize("@seguridadAdministrativa.esAdministrador(authentication)")
public class AsignacionAbogadoService {
    private final AsaltanteRepository asaltanteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<AbogadoDisponibleDTO> listarDisponibles() {
        List<AbogadoDisponibleDTO> resultado = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.findByRol_NombreAndActivoTrue("ABOGADO_DEFENSOR")) {
            if (Boolean.TRUE.equals(usuario.getRol().getActivo())) {
                resultado.add(new AbogadoDisponibleDTO(usuario.getCodigo(), usuario.getNombre()));
            }
        }
        return resultado;
    }

    @Transactional
    public ResultadoAsignacionAbogadoDTO asignar(String asaltanteCodigo, String abogadoCodigo) {
        Usuario abogado = obtenerAbogado(abogadoCodigo);
        Asaltante asaltante = obtenerAsaltante(asaltanteCodigo);
        String anterior = codigoAbogado(asaltante);
        asaltante.setAbogadoDefensor(abogado);
        asaltanteRepository.save(asaltante);
        String mensaje = "Abogado asignado correctamente";
        if (abogado.getCodigo().equals(anterior)) {
            mensaje = "El abogado ya estaba asignado al asaltante";
        } else if (anterior != null) {
            mensaje = "Abogado reasignado correctamente";
        }
        return new ResultadoAsignacionAbogadoDTO(asaltante.getCodigo(), anterior, abogado.getCodigo(), mensaje);
    }

    @Transactional
    public ResultadoAsignacionAbogadoDTO desvincular(String codigo) {
        Asaltante asaltante = obtenerAsaltante(codigo);
        String anterior = codigoAbogado(asaltante);
        asaltante.setAbogadoDefensor(null);
        asaltanteRepository.save(asaltante);
        String mensaje = anterior == null ? "El asaltante ya se encontraba sin abogado" : "Abogado desvinculado correctamente";
        return new ResultadoAsignacionAbogadoDTO(asaltante.getCodigo(), anterior, null, mensaje);
    }

    private String codigoAbogado(Asaltante asaltante) {
        return asaltante.getAbogadoDefensor() == null ? null : asaltante.getAbogadoDefensor().getCodigo();
    }

    private Asaltante obtenerAsaltante(String codigo) {
        Optional<Asaltante> resultado = asaltanteRepository.findConBloqueoByCodigo(codigo.trim().toUpperCase(java.util.Locale.ROOT));
        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Asaltante", codigo);
        }
        return resultado.get();
    }

    private Usuario obtenerAbogado(String codigo) {
        Optional<Usuario> resultado = usuarioRepository.findConBloqueoByCodigo(codigo.trim().toUpperCase(java.util.Locale.ROOT));
        if (resultado.isEmpty()) {
            throw new AbogadoNoEncontradoException(codigo);
        }
        Usuario abogado = resultado.get();
        if (!"ABOGADO_DEFENSOR".equals(abogado.getRol().getNombre()) || !Boolean.TRUE.equals(abogado.getRol().getActivo())) {
            throw new RolAbogadoInvalidoException();
        }
        if (!abogado.isActivo()) {
            throw new AbogadoInactivoException();
        }
        return abogado;
    }
}
