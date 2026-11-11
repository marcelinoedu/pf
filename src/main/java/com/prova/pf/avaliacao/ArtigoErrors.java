package com.prova.pf.avaliacao;

import java.util.LinkedHashMap;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(-1)
@RestControllerAdvice(assignableTypes = ArtigoController.class)
class ArtigoErrors {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidInput(MethodArgumentNotValidException exception) {
        var fields = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors()
            .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Confira os campos do artigo.");
        problem.setProperty("campos", fields);
        return problem;
    }
}
