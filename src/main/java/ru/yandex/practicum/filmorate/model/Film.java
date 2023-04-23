package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotation.FirstFilmBirthdayValidator;


import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    private final Set<Genre> genres = new HashSet<>();

    //TODO TreeMap<Integer, Integer> likes; userId/rate
    private final Set<Long> userFilmLike = new HashSet<>();

    @JsonIgnore
    public int getLikesSize() {
        return userFilmLike.size();
    }
}
