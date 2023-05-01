package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
import ru.yandex.practicum.filmorate.model.ValidationErrorResponse;
import ru.yandex.practicum.filmorate.model.Violation;

import javax.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice("ru.yandex.practicum.filmorate.controller")
public class ErrorHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ErrorResponse errorBadRequestException(
            final BadRequestException e
    ) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(HttpStatus.BAD_REQUEST.toString(), "errorNotFoundException", e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ErrorResponse errorNotFoundException(
            final NotFoundException e
    ) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(HttpStatus.NOT_FOUND.toString(),
                "errorNotFoundException", e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ErrorResponse errorConflictException(
            final ConflictException e
    ) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(HttpStatus.CONFLICT.toString(),
                "errorConflictException", e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ErrorResponse errorConflictException(
            final SQLIntegrityConstraintViolationException e
    ) {
        log.error(e.getMessage(), e);
        final String[] message = e.getMessage().split("\\)");
        return new ErrorResponse(HttpStatus.CONFLICT.toString(),
                "errorSQLIntegrityConstraintViolationException", message[0] + ") " + message[1] + ")");
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ValidationErrorResponse onConstraintValidationException(
            final ConstraintViolationException e
    ) {
        log.error(e.getMessage(), e);
        final List<Violation> errorPathVariable = e.getConstraintViolations()
                .stream()
                .map(
                        violation -> new Violation(
                                violation.getPropertyPath().toString(),
                                violation.getMessage()
                        )
                )
                .collect(Collectors.toList());
        return new ValidationErrorResponse(errorPathVariable);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ValidationErrorResponse onMethodArgumentNotValidException(
            final MethodArgumentNotValidException e
    ) {
        log.error(e.getMessage(), e);
        final List<Violation> errorRequestBody = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(
                        error -> new Violation(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                )
                .collect(Collectors.toList());
        return new ValidationErrorResponse(errorRequestBody);
    }
}
