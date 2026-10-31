package prog2.policia_backend.exceptions;

import java.lang.System.Logger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.LoggerFactory;
import tools.jackson.databind.exc.InvalidFormatException;

// Centraliza el manejo de excepciones de la API y las convierte en respuestas HTTP
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Devuelve 404 para recursos inexistentes.
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return crearRespuesta(HttpStatus.NOT_FOUND, ex.getClass().getSimpleName(), ex.getMessage());
    }

    // Devuelve 400 cuando fallan las validaciones de los DTO.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        StringBuilder mensaje = new StringBuilder();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (mensaje.length() > 0) {
                mensaje.append("; ");
            }

            mensaje.append(error.getField());
            mensaje.append(": ");
            mensaje.append(error.getDefaultMessage());
        }

        return crearRespuesta(HttpStatus.BAD_REQUEST, "VALIDACION_INVALIDA", mensaje.toString());
    }

    // Devuelve 400 para las reglas configuradas como peticiones incorrectas.
    @ExceptionHandler({
        CasoSinCondenaConCarcelException.class,
        CasoCondenadoSinCarcelException.class,
        OperacionNoPermitidaException.class
    })
    public ResponseEntity<Map<String, String>> manejarError400(RuntimeException ex) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, ex.getClass().getSimpleName(), ex.getMessage());
    }

    // Devuelve 403 cuando el usuario no tiene permiso.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> manejarAccesoDenegado(AccessDeniedException ex) {
        return crearRespuesta(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "Acceso denegado - Revise sus credenciales");
    }

    // Devuelve 409 para los conflictos de reglas de negocio.
    @ExceptionHandler({
        AdministradorNoPuedeDarseDeBajaException.class,
        AsaltanteConCasoJudicialException.class,
        AsaltanteDuplicadoException.class,
        AsaltanteNoParticipaEnAsaltoException.class,
        BandaConMiembrosException.class,
        BandaInactivaException.class,
        BandaYaActivaException.class,
        BandaYaInactivaException.class,
        CasoJudicialActivoException.class,
        CasoJudicialYaExistenteException.class,
        CasoSentenciadoException.class,
        ContratoVigilanciaCumplidoException.class,
        ContratoVigilanciaDuplicadoException.class,
        DatosCondenaInvalidosException.class,
        EdadInvalidaException.class,
        EntidadBancariaConSucursalesException.class,
        EntidadInactivaException.class,
        EntidadYaActivaException.class,
        EntidadYaInactivaException.class,
        FechaFuturaException.class,
        FechaPasadaException.class,
        JuezConCasosJudicialesException.class,
        MotivoBajaObligatorioException.class,
        MotivoBajaAsaltanteInvalidoException.class,
        MotivoCierreSucursalObligatorioException.class,
        PersonaInactivaException.class,
        PersonaYaActivaException.class,
        PersonaYaInactivaException.class,
        PersonaNoReactivableException.class,
        RolExistenteException.class,
        RolYaActivoException.class,
        RolYaInactivoException.class,
        SucursalYaCerradaException.class,
        SucursalNoReactivableException.class,
        SucursalYaActivaException.class,
        VigilanteConContratoFuturoException.class
    })
    public ResponseEntity<Map<String, String>> manejarConflicto(RuntimeException ex) {
        return crearRespuesta(HttpStatus.CONFLICT, ex.getClass().getSimpleName(), ex.getMessage());
    }

    // Devuelve 400 cuando el JSON recibido es inválido.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getCause();

        if (causa instanceof InvalidFormatException) {
            InvalidFormatException formato = (InvalidFormatException) causa;

            if (formato.getTargetType() != null && formato.getTargetType().isEnum()) {
                Object[] valores = formato.getTargetType().getEnumConstants();
                StringBuilder permitidos = new StringBuilder();

                for (Object valor : valores) {
                    if (permitidos.length() > 0) {
                        permitidos.append(", ");
                    }

                    permitidos.append(valor.toString());
                }

                String campo = "campo";

                if (!formato.getPath().isEmpty()) {
                    String descripcion = formato.getPath().get(0).getDescription();
                    int inicio = descripcion.indexOf("[\"");
                    int fin = descripcion.indexOf("\"]");

                    if (inicio >= 0 && fin > inicio) {
                        campo = descripcion.substring(inicio + 2, fin);
                    }
                }

                String mensaje = "Valor no válido para " + campo
                        + ": " + formato.getValue()
                        + ". Valores permitidos: " + permitidos;

                return crearRespuesta(HttpStatus.BAD_REQUEST, "VALOR_ENUM_INVALIDO", mensaje);
            }
        }

        return crearRespuesta(HttpStatus.BAD_REQUEST, "JSON_INVALIDO", "El JSON enviado no es válido");
    }

    // Devuelve 500 ante errores inesperados.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErrorGeneral(Exception ex) {
        logger.error("Error inesperado: tipo={}, mensaje={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Error interno del servidor");
    }

    // Construye una respuesta uniforme para todos los errores.
    private ResponseEntity<Map<String, String>> crearRespuesta(HttpStatus estado, String codigo, String mensaje) {
        Map<String, String> respuesta = new LinkedHashMap<>();

        respuesta.put("codigo", codigo);
        respuesta.put("mensaje", mensaje);

        return ResponseEntity.status(estado).body(respuesta);
    }
}
