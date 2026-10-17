package com.tomatoes.insumos.Excepciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones en respuestas JSON uniformes: {timestamp, status, mensaje, errores}. */
@RestControllerAdvice
public class ManejadorExcepciones {

    private static final Logger log = LoggerFactory.getLogger(ManejadorExcepciones.class);

    @ExceptionHandler(NoEncontradoException.class)
    ResponseEntity<Map<String, Object>> noEncontrado(NoEncontradoException ex) { return responder(404, ex.getMessage(), null); }

    @ExceptionHandler(NegocioException.class)
    ResponseEntity<Map<String, Object>> negocio(NegocioException ex) { return responder(400, ex.getMessage(), null); }

    @ExceptionHandler(NoAutorizadoException.class)
    ResponseEntity<Map<String, Object>> noAutorizado(NoAutorizadoException ex) { return responder(401, ex.getMessage(), null); }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    ResponseEntity<Map<String, Object>> noDisponible(ServicioNoDisponibleException ex) { return responder(503, ex.getMessage(), null); }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> prohibido(AccessDeniedException ex) {
        return responder(403, "No tiene permisos para realizar esta operación.", null);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    ResponseEntity<Map<String, Object>> duplicado(DuplicateKeyException ex) {
        return responder(409, "Ya existe un registro con ese valor único (documento, correo o nombre).", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return responder(400, "Hay datos inválidos en el formulario.", errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException ex) {
        return responder(400, "El cuerpo de la solicitud no es un JSON válido o tiene un valor con formato incorrecto.", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<Map<String, Object>> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return responder(400, "El parámetro '" + ex.getName() + "' tiene un valor no válido.", null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> generico(Exception ex) {
        if (ex instanceof ErrorResponse er) {
            return responder(er.getStatusCode().value(), er.getBody().getDetail() != null ? er.getBody().getDetail() : "Solicitud no válida.", null);
        }
        log.error("Error inesperado", ex);
        return responder(500, "Ocurrió un error interno. Intente de nuevo o contacte al administrador.", null);
    }

    private ResponseEntity<Map<String, Object>> responder(int estado, String mensaje, Map<String, String> errores) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", estado);
        cuerpo.put("mensaje", mensaje);
        if (errores != null) cuerpo.put("errores", errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
