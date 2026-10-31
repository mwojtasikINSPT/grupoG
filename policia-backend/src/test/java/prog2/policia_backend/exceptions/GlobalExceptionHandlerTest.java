package prog2.policia_backend.exceptions;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void manejarRecursoNoEncontrado() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("Rol", "ROL00001");

        ResponseEntity<Map<String, String>> respuesta = handler.manejarRecursoNoEncontrado(ex);

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
        assertEquals("RecursoNoEncontradoException", respuesta.getBody().get("codigo"));
        assertEquals("Rol no encontrado con código: ROL00001", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarError400() {
        OperacionNoPermitidaException ex = new OperacionNoPermitidaException("Operación no permitida");

        ResponseEntity<Map<String, String>> respuesta = handler.manejarError400(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("OperacionNoPermitidaException", respuesta.getBody().get("codigo"));
        assertEquals("Operación no permitida", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarAccesoDenegado() {
        AccessDeniedException ex = new AccessDeniedException("Sin permisos");

        ResponseEntity<Map<String, String>> respuesta = handler.manejarAccesoDenegado(ex);

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
        assertEquals("ACCESO_DENEGADO", respuesta.getBody().get("codigo"));
        assertEquals("Acceso denegado - Revise sus credenciales", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarConflicto() {
        RolExistenteException ex = new RolExistenteException();

        ResponseEntity<Map<String, String>> respuesta = handler.manejarConflicto(ex);

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("RolExistenteException", respuesta.getBody().get("codigo"));
        assertEquals("Ya existe ese Rol", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarJsonInvalido() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<Map<String, String>> respuesta = handler.manejarJsonInvalido(ex);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("JSON_INVALIDO", respuesta.getBody().get("codigo"));
        assertEquals("El JSON enviado no es válido", respuesta.getBody().get("mensaje"));
    }

    @Test
    void manejarErrorGeneral() {
        Exception ex = new Exception("Información interna sensible");

        ResponseEntity<Map<String, String>> respuesta = handler.manejarErrorGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
        assertEquals("ERROR_INTERNO", respuesta.getBody().get("codigo"));
        assertEquals("Error interno del servidor", respuesta.getBody().get("mensaje"));
        assertFalse(respuesta.getBody().toString().contains("Información interna sensible"));
    }

    @Test
    void respuestaContieneSoloCodigoYMensaje() {
        ResponseEntity<Map<String, String>> respuesta = handler.manejarConflicto(new RolExistenteException());

        assertNotNull(respuesta.getBody());
        assertEquals(2, respuesta.getBody().size());
        assertTrue(respuesta.getBody().containsKey("codigo"));
        assertTrue(respuesta.getBody().containsKey("mensaje"));
    }
}
