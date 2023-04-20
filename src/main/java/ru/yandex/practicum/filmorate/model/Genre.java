package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Data
@Builder
public class Genre {

    @Null(groups = GenreStorage.OnCreate.class)
    @NotNull(groups = GenreStorage.OnUpdate.class)
    @Positive(groups = GenreStorage.class)
    private Integer id;

    @NotNull(groups = GenreStorage.class)
    @Pattern(regexp = "^\\S+$", message = "не должен быть пустым и содержать пробелы", groups = GenreStorage.class)
    private String name;

}
