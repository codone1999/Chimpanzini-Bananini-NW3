package org.example.itbmshopbe.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private ZonedDateTime timestamp;
    private int status;
    private String error;
    private List<FieldErrorDetail> errors;
    private String path;
}
