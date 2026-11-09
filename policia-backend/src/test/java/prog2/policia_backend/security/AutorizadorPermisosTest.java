package prog2.policia_backend.security;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import static org.junit.jupiter.api.Assertions.*;

class AutorizadorPermisosTest {

    private final AutorizadorPermisos autorizador = new AutorizadorPermisos();

    private boolean permitido(String metodo, String ruta, String... permisos) {
        List<SimpleGrantedAuthority> autoridades = new ArrayList<>();
        for (String permiso : permisos) {
            autoridades.add(new SimpleGrantedAuthority(permiso));
        }
        Authentication usuario = UsernamePasswordAuthenticationToken.authenticated("USR001", "clave", autoridades);
        MockHttpServletRequest solicitud = new MockHttpServletRequest(metodo, ruta);
        solicitud.setServletPath(ruta);
        return autorizador.authorize(() -> usuario, new RequestAuthorizationContext(solicitud)).isGranted();
    }

    @ParameterizedTest
    @CsvSource({
        "/api/sucursales/SUC1/contratos, LECTURA_SUCURSAL, LECTURA_CONTRATO_VIGILANCIA",
        "/api/sucursales/SUC1/asaltos, LECTURA_SUCURSAL, LECTURA_ASALTO",
        "/api/entidades-bancarias/EB1/sucursales, LECTURA_ENTIDAD_BANCARIA, LECTURA_SUCURSAL",
        "/api/bandas/BAN1/integrantes, LECTURA_BANDA, LECTURA_ASALTANTE",
        "/api/asaltantes/ASS1/asaltos, LECTURA_ASALTANTE, LECTURA_ASALTO",
        "/api/asaltantes/ASS1/casos-judiciales, LECTURA_ASALTANTE, LECTURA_CASO_JUDICIAL",
        "/api/jueces/JUE1/casos-judiciales, LECTURA_JUEZ, LECTURA_CASO_JUDICIAL",
        "/api/casos-judiciales/CJU1/sentencias, LECTURA_CASO_JUDICIAL, LECTURA_SENTENCIA_ASALTANTE",
        "/api/casos-judiciales/sentencias/asaltante/ASS1, LECTURA_CASO_JUDICIAL, LECTURA_SENTENCIA_ASALTANTE"
    })
    void protegeLaEntidadRelacionada(String ruta, String permisoOrigen, String permisoDestino) {
        assertFalse(permitido("GET", ruta, permisoOrigen));
        assertTrue(permitido("GET", ruta, permisoDestino));
        assertFalse(permitido("GET", ruta, "ROLE_ADMINISTRADOR"));
    }

    @Test
    void unaRutaDeAsaltosPorSucursalRequiereLecturaDeAsaltos() {
        assertTrue(permitido("GET", "/api/asaltos/sucursal/SUC1", "LECTURA_ASALTO"));
        assertFalse(permitido("GET", "/api/asaltos/sucursal/SUC1", "LECTURA_SUCURSAL"));
    }

    @Test
    void aceptaSoloLecturaPropiaDeContratosEnRutaRelacionada() {
        assertTrue(permitido("GET", "/api/sucursales/SUC1/contratos", "LECTURA_DATOS_PROPIOS_CONTRATO_VIGILANCIA"));
        assertFalse(permitido("GET", "/api/sucursales/SUC1/asaltos", "LECTURA_DATOS_PROPIOS_ASALTO"));
    }

    @Test
    void escribirSentenciasRequiereSuPermisoDeEscritura() {
        String ruta = "/api/casos-judiciales/CJU1/sentencias/ASS1";
        assertFalse(permitido("PUT", ruta, "ESCRITURA_CASO_JUDICIAL"));
        assertTrue(permitido("PUT", ruta, "ESCRITURA_SENTENCIA_ASALTANTE"));
    }

    @Test
    void usuarioAnonimoNoAccedeAInformacionDeSesion() {
        Authentication anonimo = new AnonymousAuthenticationToken("clave", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        MockHttpServletRequest solicitud = new MockHttpServletRequest("GET", "/api/usuarios/mis-permisos");
        solicitud.setServletPath("/api/usuarios/mis-permisos");
        assertFalse(autorizador.authorize(() -> anonimo, new RequestAuthorizationContext(solicitud)).isGranted());
    }

    @Test
    void accesoARolesDependeDeSusPermisos() {
        assertTrue(permitido("GET", "/api/roles", "LECTURA_ROL"));
        assertFalse(permitido("GET", "/api/roles", "ESCRITURA_ROL"));
        assertFalse(permitido("GET", "/api/roles", "ROLE_ADMINISTRADOR"));

        assertTrue(permitido("POST", "/api/roles", "ESCRITURA_ROL"));
        assertTrue(permitido("PUT", "/api/roles/ROL00001", "ESCRITURA_ROL"));
        assertTrue(permitido("PATCH", "/api/roles/ROL00001/baja", "ESCRITURA_ROL"));
        assertFalse(permitido("POST", "/api/roles", "LECTURA_ROL"));
        assertFalse(permitido("POST", "/api/roles", "ROLE_ADMINISTRADOR"));
    }
}
