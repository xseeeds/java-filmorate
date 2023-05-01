package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotation.FirstFilmBirthdayValidator;


import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;


@Data
@Builder
public class Film {

    @Min(value = 0, message = "должно быть больше 0")
    @EqualsAndHashCode.Exclude
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @Length(max = 200)
    @EqualsAndHashCode.Exclude
    private String description;

    @Positive
    @EqualsAndHashCode.Exclude
    private int duration;

    @FirstFilmBirthdayValidator
    private LocalDate releaseDate;

    @Max(value = 10)
    @PositiveOrZero
    @EqualsAndHashCode.Exclude
    private float rate;

    @EqualsAndHashCode.Exclude
    private Mpa mpa;

    @EqualsAndHashCode.Exclude
    private final List<Genre> genres = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    private final List<Director> directors = new ArrayList<>();

    //TODO HashMap<Long, Integer> likes; userId/rate
    @EqualsAndHashCode.Exclude
    private final Map<Long, Integer> userFilmLike = new HashMap<>();

    @JsonIgnore
    public int getLikesSize() {
        return userFilmLike.size();
    }
}
