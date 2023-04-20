package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotation.FirstFilmBirthdayValidator;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;


import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class Film {

    @Min(value = 0, groups = FilmStorage.class, message = "должно быть больше 0")
    @Null(groups = FilmStorage.OnCreate.class, message = "POST request. Для обновления используй PUT запрос, film имеет id!!!")
    @NotNull(groups = FilmStorage.OnUpdate.class, message = "PUT request. Для обновления используй id в теле запроса film")
    @EqualsAndHashCode.Exclude
    private Long id;

    @NotNull(groups = FilmStorage.class)
    @NotBlank(groups = FilmStorage.class)
    private String name;

    @Length(max = 200, groups = FilmStorage.class)
    @EqualsAndHashCode.Exclude
    private String description;

    @Positive(groups = FilmStorage.class)
    private int duration;

    @FirstFilmBirthdayValidator(groups = FilmStorage.class)
    private LocalDate releaseDate;

    @Max(value = 10, groups = FilmStorage.class)
    @PositiveOrZero(groups = FilmStorage.class)
    @EqualsAndHashCode.Exclude
    private float rate;

    private Mpa mpa;

    private final Set<Genre> genres = new HashSet<>();

    //TODO TreeMap<Integer, Integer> likes; userId/rate
    private final Set<Long> likes = new HashSet<>();

    @JsonIgnore
    public int getLikesSize() {
        return likes.size();
    }
}
