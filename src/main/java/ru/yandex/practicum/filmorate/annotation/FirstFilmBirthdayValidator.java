package ru.yandex.practicum.filmorate.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FirstFilmBirthdayValidatorImpl.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface FirstFilmBirthdayValidator {
    String message() default "не должен быть раньше первого коммерческого показа фильмов [1895-12-28]";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}