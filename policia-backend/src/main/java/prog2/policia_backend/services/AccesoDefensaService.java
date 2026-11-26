package prog2.policia_backend.services;

import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prog2.policia_backend.exceptions.AccesoDefensaDenegadoException;
import prog2.policia_backend.exceptions.DefendidoNoInvolucradoException;
import prog2.policia_backend.exceptions.RecursoNoEncontradoException;
import prog2.policia_backend.models.Asaltante;
import prog2.policia_backend.models.CasoJudicial;
import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.SentenciaAsaltante;
import prog2.policia_backend.models.Usuario;
import prog2.policia_backend.repositories.AsaltanteRepository;
import prog2.policia_backend.repositories.CasoJudicialRepository;
import prog2.policia_backend.repositories.SentenciaAsaltanteRepository;
import prog2.policia_backend.repositories.UsuarioRepository;
import prog2.policia_backend.security.AutorizadorPermisos;
import prog2.policia_backend.security.UsuarioDetails;
import prog2.policia_backend.security.SeguridadAdministrativa;

/** Acceso vigente a defendidos y participaciones, reutilizable por futuros servicios. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccesoDefensaService {
    private final UsuarioRepository usuarioRepository;
    private final AsaltanteRepository asaltanteRepository;
    private final CasoJudicialRepository casoJudicialRepository;
    private final SentenciaAsaltanteRepository sentenciaAsaltanteRepository;
    private final AutorizadorPermisos autorizadorPermisos;
    private final SeguridadAdministrativa seguridadAdministrativa;

    public Usuario obtenerAbogadoAutenticado() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        exigirPermisoPropio(autenticacion, Entidad.ASALTANTE);
        Optional<Usuario> resultado = usuarioRepository.findByCodigo(autenticacion.getName());
        if (resultado.isEmpty()) {
            throw new AccesoDefensaDenegadoException("El usuario autenticado no esta disponible");
        }
        Usuario abogado = resultado.get();
        if (!abogado.isActivo() || !"ABOGADO_DEFENSOR".equalsIgnoreCase(abogado.getRol().getNombre())
                || !Boolean.TRUE.equals(abogado.getRol().getActivo())) {
            throw new AccesoDefensaDenegadoException("Se requiere un abogado defensor con usuario y rol activos");
        }
        return abogado;
    }

    public Asaltante obtenerDefendidoAutorizado(String codigo) {
        return obtenerDefendidoAutorizado(codigo, false);
    }

    private Asaltante obtenerDefendidoAutorizado(String codigo, boolean bloquear) {
        Usuario abogado = obtenerAbogadoAutenticado();
        Asaltante defendido = obtenerAsaltante(codigo, bloquear);
        if (defendido.getAbogadoDefensor() == null
                || !abogado.getId().equals(defendido.getAbogadoDefensor().getId())) {
            throw new AccesoDefensaDenegadoException("El defendido no esta asignado actualmente al abogado autenticado");
        }
        return defendido;
    }

    public Asaltante obtenerDefendidoParaConsultaJudicial(String codigo) {
        Asaltante defendido = obtenerDefendidoAutorizado(codigo);
        exigirPermisosJudiciales();
        return defendido;
    }

    private void exigirPermisosJudiciales() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        exigirPermisoPropio(autenticacion, Entidad.CASO_JUDICIAL);
        exigirPermisoPropio(autenticacion, Entidad.SENTENCIA_ASALTANTE);
    }

    public SentenciaAsaltante obtenerParticipacionAutorizada(String codigo, String casoCodigo) {
        Asaltante defendido = obtenerDefendidoParaConsultaJudicial(codigo);
        return obtenerParticipacion(defendido, casoCodigo, false);
    }

    /** Serializa la presentacion con la desvinculacion y con el cierre del caso. */
    @Transactional
    public SentenciaAsaltante obtenerParticipacionParaPresentacion(String codigo, String casoCodigo) {
        Asaltante defendido = obtenerDefendidoAutorizado(codigo, true);
        exigirPermisosJudiciales();
        return obtenerParticipacion(defendido, casoCodigo, true);
    }

    public SentenciaAsaltante obtenerParticipacionParaConsultaPruebas(String codigo, String casoCodigo) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || !(autenticacion.getPrincipal() instanceof UsuarioDetails)) {
            throw new AccesoDefensaDenegadoException("Se requiere un usuario autenticado para consultar pruebas");
        }
        UsuarioDetails usuario = (UsuarioDetails) autenticacion.getPrincipal();
        // La lectura general nunca permite a un abogado eludir la asignacion actual.
        if ("ABOGADO_DEFENSOR".equalsIgnoreCase(usuario.getNombreRol())) {
            exigirPermisoPropio(autenticacion, Entidad.PRUEBA_DEFENSA);
            return obtenerParticipacionAutorizada(codigo, casoCodigo);
        }
        boolean lectorGeneral = seguridadAdministrativa.esAdministrador(autenticacion)
                || "INVESTIGADOR".equalsIgnoreCase(usuario.getNombreRol());
        if (!lectorGeneral || !autorizadorPermisos.puedeLeerTodo(autenticacion, Entidad.PRUEBA_DEFENSA)) {
            throw new AccesoDefensaDenegadoException("No tiene permiso para consultar estas pruebas de defensa");
        }
        return obtenerParticipacion(obtenerAsaltante(codigo, false), casoCodigo, false);
    }

    private Asaltante obtenerAsaltante(String codigo, boolean bloquear) {
        Optional<Asaltante> resultado;
        if (bloquear) {
            resultado = asaltanteRepository.findConBloqueoByCodigo(normalizar(codigo));
        } else {
            resultado = asaltanteRepository.findByCodigo(normalizar(codigo));
        }
        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Defendido", codigo);
        }
        return resultado.get();
    }

    private SentenciaAsaltante obtenerParticipacion(Asaltante defendido, String casoCodigo, boolean bloquear) {
        Optional<CasoJudicial> resultado;
        if (bloquear) {
            resultado = casoJudicialRepository.findConBloqueoByCodigo(normalizar(casoCodigo));
        } else {
            resultado = casoJudicialRepository.findByCodigo(normalizar(casoCodigo));
        }
        if (resultado.isEmpty()) {
            throw new RecursoNoEncontradoException("Caso judicial", casoCodigo);
        }
        // La participacion existe desde que se crea el caso, incluso sin sentencia.
        Optional<SentenciaAsaltante> participacion = sentenciaAsaltanteRepository
                .findByCasoJudicial_IdAndAsaltante_Id(resultado.get().getId(), defendido.getId());
        if (participacion.isEmpty()) {
            throw new DefendidoNoInvolucradoException();
        }
        return participacion.get();
    }

    private void exigirPermisoPropio(Authentication autenticacion, Entidad entidad) {
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken
                || !autorizadorPermisos.puedeLeerPropios(autenticacion, entidad)) {
            throw new AccesoDefensaDenegadoException("No tiene permiso de consulta de defensa sobre " + entidad.name());
        }
    }

    private String normalizar(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }
}
