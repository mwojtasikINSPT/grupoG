package prog2.policia_backend.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Centraliza el manejo de excepciones de la API y las convierte en respuestas HTTP
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Devuelve 404 para Recurso inexistente
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // Devuelve 400 cuando fallan las validaciones de los datos 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (mensaje1, mensaje2) -> mensaje1
                ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errores);
    }

    // Devuelve 403 cuando el usuario autenticado no tiene permiso
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Acceso denegado");
    }

    // Devuelve 400 cuando se viola una regla de negocio o se recibe un argumento inválido
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Petición incorrecta");
        error.put("mensaje", ex.getMessage()); // texto del Service

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Devuelve 409 Conflict cuando se intenta eliminar una banda con miembros activos
    @ExceptionHandler(BandaConMiembrosException.class)
    public ResponseEntity<Map<String, String>> manejarBandaConMiembros(
            BandaConMiembrosException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // Devuelve 409 cuando se intenta eliminar un contrato del dia o cumplido
    @ExceptionHandler(ContratoVigilanciaCumplidoException.class)
    public ResponseEntity<Map<String, String>> manejarContratoVigilanciaCumplido(
            ContratoVigilanciaCumplidoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // Devuelve 409 cuando un vigilante ya tiene un contrato activo en esa fecha
    @ExceptionHandler(ContratoVigilanciaDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarContratoDuplicado(
            ContratoVigilanciaDuplicadoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // Devuelve 409 cuando se intenta dar de baja un vigilante con contratos activos
    @ExceptionHandler(VigilanteConContratoFuturoException.class)
    public ResponseEntity<Map<String, String>> manejarVigilanteConContratoFuturo(
            VigilanteConContratoFuturoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // Devuelve 409 cuando se intenta dar de baja una entidad bancaria con sucursales activas.
    @ExceptionHandler(EntidadBancariaConSucursalesException.class)
    public ResponseEntity<Map<String, String>> manejarEntidadBancariaConSucursales(
            EntidadBancariaConSucursalesException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // Devuelve 500 ante errores inesperados no controlados 
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorGeneral(
            Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servidor"));
    }
}
