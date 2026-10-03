package prog2.policia_backend.exceptions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

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

    // Devuelve 400 cuando se viola una regla de negocio
    @ExceptionHandler({
        CasoSinCondenaConCarcelException.class,
        CasoCondenadoSinCarcelException.class
    })
    public ResponseEntity<Map<String, String>> manejarError400(
            RuntimeException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Petición incorrecta",
                        "mensaje", ex.getMessage()
                ));
    }

    // Devuelve 403 cuando el usuario autenticado no tiene permiso
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Acceso denegado - Revise sus credenciales");
    }

    // Devuelve 409 Conflict ante conflictos de reglas de negocio
    @ExceptionHandler({
        BandaConMiembrosException.class,
        ContratoVigilanciaCumplidoException.class,
        ContratoVigilanciaDuplicadoException.class,
        VigilanteConContratoFuturoException.class,
        EntidadBancariaConSucursalesException.class,
        JuezConCasosJudicialesException.class,
        PersonaNoReactivableException.class,
        CasoJudicialYaExistenteException.class,
        AsaltanteNoParticipaEnAsaltoException.class,
        SucursalYaCerradaException.class,
        MotivoCierreSucursalObligatorioException.class,
        SucursalNoReactivableException.class,
        SucursalYaActivaException.class,})
    public ResponseEntity<Map<String, String>> manejarConflicto(
            RuntimeException ex) {

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
    

    // Maneja errores de formato en el JSON como valores inválidos en enums,
    // indicando el campo afectado, el valor recibido y los valores permitidos.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(
            HttpMessageNotReadableException ex) {

        Throwable causa = ex.getCause();

        if (causa instanceof InvalidFormatException invalidFormat
                && invalidFormat.getTargetType() != null
                && invalidFormat.getTargetType().isEnum()) {

            Class<?> tipoEnum = invalidFormat.getTargetType();

            String valoresPermitidos = Arrays.stream(tipoEnum.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            String campo = "campo";

            if (!invalidFormat.getPath().isEmpty()) {
                String descripcion = invalidFormat.getPath()
                        .get(0)
                        .getDescription();

                int inicio = descripcion.indexOf("[\"");
                int fin = descripcion.indexOf("\"]");

                if (inicio >= 0 && fin > inicio) {
                    campo = descripcion.substring(inicio + 2, fin);
                }
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "Valor no válido",
                            "campo", campo,
                            "valorRecibido",
                            String.valueOf(invalidFormat.getValue()),
                            "valoresPermitidos", valoresPermitidos
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error",
                        "El JSON enviado no es válido",
                         "detalle", ex.getMessage()
                ));
    }
}
