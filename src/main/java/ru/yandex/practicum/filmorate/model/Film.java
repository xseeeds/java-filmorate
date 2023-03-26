package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotation.FirstFilmBirthday;


import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    @PositiveOrZero
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

    //TODO TreeMap<Integer, Integer> likes; userId/rate
    final Set<Integer> likes = new HashSet<>();
}
