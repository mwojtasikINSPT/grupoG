package prog2.policia_backend.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.security.authorization.AuthorizationResult;

import prog2.policia_backend.models.Entidad;
import prog2.policia_backend.models.Permiso;

@Component
public class AutorizadorPermisos implements AuthorizationManager<RequestAuthorizationContext> {

    private final Map<String, Entidad> rutas = new HashMap<>();

    public AutorizadorPermisos() {
        rutas.put("asaltantes", Entidad.ASALTANTE);
        rutas.put("asaltos", Entidad.ASALTO);
        rutas.put("bandas", Entidad.BANDA);
        rutas.put("casos-judiciales", Entidad.CASO_JUDICIAL);
        rutas.put("contratos-vigilancia", Entidad.CONTRATO_VIGILANCIA);
        rutas.put("entidades-bancarias", Entidad.ENTIDAD_BANCARIA);
        rutas.put("jueces", Entidad.JUEZ);
        rutas.put("sentencias-asaltantes", Entidad.SENTENCIA_ASALTANTE);
        rutas.put("sucursales", Entidad.SUCURSAL);
        rutas.put("usuarios", Entidad.USUARIO);
    }

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        Authentication usuario = authentication.get();

        if (usuario == null || !usuario.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        if (esAdministrador(usuario)) {
            return new AuthorizationDecision(true);
        }

        String ruta = context.getRequest().getServletPath();
        String metodo = context.getRequest().getMethod();

        if (ruta.equals("/api/usuarios/mi-perfil") || ruta.equals("/api/usuarios/mis-permisos")) {
            return new AuthorizationDecision(metodo.equals(HttpMethod.GET.name()));
        }

        if (ruta.equals("/api/usuarios/password")) {
            return new AuthorizationDecision(metodo.equals(HttpMethod.PATCH.name()));
        }

        if (ruta.equals("/api/roles") || ruta.startsWith("/api/roles/")) {
            return new AuthorizationDecision(false);
        }

        if (ruta.startsWith("/api/usuarios/")) {
            String resto = ruta.substring("/api/usuarios/".length());

            if (resto.endsWith("/password")) {
                return new AuthorizationDecision(false);
            }
        }

        Entidad entidad = obtenerEntidad(ruta);

        if (entidad == null) {
            return new AuthorizationDecision(false);
        }

        if (entidad == Entidad.USUARIO && !metodo.equals(HttpMethod.GET.name())) {
            return new AuthorizationDecision(false);
        }

        if (metodo.equals(HttpMethod.GET.name())) {
            boolean admiteLecturaPropia = entidad == Entidad.CONTRATO_VIGILANCIA
                    || entidad == Entidad.USUARIO;

            boolean permitido = tienePermiso(usuario, entidad, Permiso.LECTURA);

            if (admiteLecturaPropia) {
                permitido = permitido || tienePermiso(usuario, entidad, Permiso.LECTURA_DATOS_PROPIOS);
            }

            return new AuthorizationDecision(permitido);
        }

        if (metodo.equals(HttpMethod.POST.name())
                || metodo.equals(HttpMethod.PUT.name())
                || metodo.equals(HttpMethod.PATCH.name())
                || metodo.equals(HttpMethod.DELETE.name())) {

            return new AuthorizationDecision(tienePermiso(usuario, entidad, Permiso.ESCRITURA));
        }

        return new AuthorizationDecision(false);
    }

    private Entidad obtenerEntidad(String ruta) {
        if (!ruta.startsWith("/api/")) {
            return null;
        }

        String resto = ruta.substring(5);
        int separador = resto.indexOf('/');

        String recurso = separador >= 0 ? resto.substring(0, separador) : resto;

        return rutas.get(recurso);
    }

    public boolean tienePermiso(Authentication authentication, Entidad entidad, Permiso permiso) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String requerida = permiso.name() + "_" + entidad.name();

        for (GrantedAuthority autoridad : authentication.getAuthorities()) {
            if (requerida.equals(autoridad.getAuthority())) {
                return true;
            }
        }

        return false;
    }

    public boolean puedeLeer(Authentication authentication, Entidad entidad) {
        return esAdministrador(authentication)
                || tienePermiso(authentication, entidad, Permiso.LECTURA)
                || tienePermiso(authentication, entidad, Permiso.LECTURA_DATOS_PROPIOS);
    }

    public boolean puedeLeerTodo(Authentication authentication, Entidad entidad) {
        return esAdministrador(authentication)
                || tienePermiso(authentication, entidad, Permiso.LECTURA);
    }

    public boolean puedeLeerPropios(Authentication authentication, Entidad entidad) {
        return esAdministrador(authentication)
                || tienePermiso(authentication, entidad, Permiso.LECTURA_DATOS_PROPIOS);
    }

    public boolean puedeEscribir(Authentication authentication, Entidad entidad) {
        return esAdministrador(authentication)
                || tienePermiso(authentication, entidad, Permiso.ESCRITURA);
    }

    public boolean esAdministrador(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        for (GrantedAuthority autoridad : authentication.getAuthorities()) {
            if ("ROLE_ADMINISTRADOR".equals(autoridad.getAuthority())) {
                return true;
            }
        }

        return false;
    }
}
