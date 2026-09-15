package grupog.sistemapolicia.service;

import grupog.sistemapolicia.dto.UsuarioRequest;
import grupog.sistemapolicia.dto.UsuarioResponse;
import grupog.sistemapolicia.model.Usuario;
import grupog.sistemapolicia.model.UsuarioAdministrador;
import grupog.sistemapolicia.model.UsuarioInvestigador;
import grupog.sistemapolicia.model.UsuarioVigilante;
import grupog.sistemapolicia.model.Vigilante;
import grupog.sistemapolicia.repository.UsuarioRepository;
import grupog.sistemapolicia.repository.VigilanteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final VigilanteRepository vigilanteRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
            VigilanteRepository vigilanteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.vigilanteRepository = vigilanteRepository;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::crearRespuesta)
                .toList();
    }

    public UsuarioResponse guardar(UsuarioRequest datos) {
        validar(datos);

        if (usuarioRepository.existsById(datos.username())) {
            throw new IllegalArgumentException(
                    "El usuario '" + datos.username() + "' ya existe");
        }

        Usuario usuario;
        switch (datos.rol()) {
            case ADMINISTRADOR -> usuario = new UsuarioAdministrador(
                    datos.username(), datos.password());
            case INVESTIGADOR -> usuario = new UsuarioInvestigador(
                    datos.username(), datos.password());
            case VIGILANTE -> {
                if (datos.codigoVigilante() == null
                        || datos.codigoVigilante().isBlank()) {
                    throw new IllegalArgumentException(
                            "El código de vigilante es obligatorio para ese rol");
                }
                Vigilante vigilante = vigilanteRepository
                        .findById(datos.codigoVigilante())
                        .orElseThrow(() -> new IllegalArgumentException(
                        "No existe el vigilante '"
                        + datos.codigoVigilante() + "'"));
                usuario = new UsuarioVigilante(
                        datos.username(), datos.password(), vigilante);
            }
            default -> throw new IllegalArgumentException("Rol inválido");
        }

        return crearRespuesta(usuarioRepository.save(usuario));
    }

    private void validar(UsuarioRequest datos) {
        if (datos == null) {
            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios");
        }
        if (datos.username() == null || datos.username().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio");
        }
        if (datos.password() == null || datos.password().isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria");
        }
        if (datos.rol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }

    private UsuarioResponse crearRespuesta(Usuario usuario) {
        String codigoVigilante = null;
        if (usuario instanceof UsuarioVigilante usuarioVigilante
                && usuarioVigilante.getVigilante() != null) {
            codigoVigilante = usuarioVigilante.getVigilante().getCodigo();
        }
        return new UsuarioResponse(
                usuario.getUsername(),
                usuario.obtenerRol(),
                codigoVigilante);
    }
}
