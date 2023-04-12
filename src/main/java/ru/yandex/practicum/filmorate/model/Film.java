package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import nonapi.io.github.classgraph.json.Id;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotation.FirstFilmBirthday;


import javax.validation.constraints.*;
import java.time.LocalDate;

@Value
@Builder(toBuilder = true)
public class Film {

    @Id
    @EqualsAndHashCode.Exclude
    int id;

    @NotNull
    @NotBlank
    String name;

    @Length(max = 200)
    String description;

    @FirstFilmBirthday
    LocalDate releaseDate;

    @Positive
    int duration;

    @Max(10)
    @PositiveOrZero
    @EqualsAndHashCode.Exclude
    byte rate;
}
