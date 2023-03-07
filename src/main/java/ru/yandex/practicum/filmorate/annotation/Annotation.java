package ru.yandex.practicum.filmorate.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

public class Annotation {
    @Documented
    @Constraint(validatedBy = IsAfterCinemaBirthdayValidator.class)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IsAfterCinemaBirthday {
        String message() default "не должен быть раньше первого коммерческого показа фильмов [1895-12-28]";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    static class IsAfterCinemaBirthdayValidator implements ConstraintValidator<IsAfterCinemaBirthday, LocalDate> {
        private final LocalDate firstFilmBirthday = LocalDate.of(1895, 12, 27);

        @Override
        public void initialize(IsAfterCinemaBirthday contactNumber) {
        }

        @Override
        public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext constraintValidatorContext) {
            return releaseDate == null || releaseDate.isAfter(firstFilmBirthday);
        }
    }
}
