package ru.yandex.practicum.filmorate.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class FirstFilmBirthdayValidatorImpl implements ConstraintValidator<FirstFilmBirthdayValidator, LocalDate> {
    private final LocalDate firstFilmBirthday = LocalDate.of(1895, 12, 27);

    @Override
    public void initialize(FirstFilmBirthdayValidator contactNumber) {
    }

    @Override
    public boolean isValid(LocalDate releaseDate, ConstraintValidatorContext constraintValidatorContext) {
        return releaseDate == null || releaseDate.isAfter(firstFilmBirthday);
    }
}