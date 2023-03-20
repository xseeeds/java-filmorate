package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import nonapi.io.github.classgraph.json.Id;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotation.FirstFilmBirthday;


import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Data
public class Film {

    @Id
    @EqualsAndHashCode.Exclude
    private int id;

    @NotNull
    @NotBlank
    private String name;

    @Length(max = 200)
    @EqualsAndHashCode.Exclude
    private String description;

    @FirstFilmBirthday
    private LocalDate releaseDate;

    @Positive
    private int duration;

    @Max(10)
    @PositiveOrZero
    @EqualsAndHashCode.Exclude
    private float rate;

    //TODO сделай TreeMap<Integer, Integer> likes; userId/rate
    Set<Integer> likes;
}
