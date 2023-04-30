package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotation.FirstFilmBirthdayValidator;


import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private int duration;

    @FirstFilmBirthdayValidator
    private LocalDate releaseDate;

    @Max(value = 10)
    @PositiveOrZero
    @EqualsAndHashCode.Exclude
    private float rate;

    private Mpa mpa;

    private final List<Genre> genres = new ArrayList<>();

    //TODO HashMap<Long, Integer> likes; userId/rate
    private final Map<Long, Integer> userFilmLike = new HashMap<>();

    @JsonIgnore
    public int getLikesSize() {
        return userFilmLike.size();
    }
}
