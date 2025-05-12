package org.example.itbmshopbe.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
@ControllerAdvice
public class GlobalExceptionHandler  {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatusCode statusCode = ex.getStatusCode();
        GeneralErrorResponse ger = new GeneralErrorResponse(
                statusCode.value(),
                statusCode.toString(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(statusCode).body(ger);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnhandledException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        GeneralErrorResponse ger = new GeneralErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(ger);
    }

}
