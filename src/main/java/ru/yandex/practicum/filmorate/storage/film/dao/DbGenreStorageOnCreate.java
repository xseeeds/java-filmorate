package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbGenreStorageOnCreate implements GenreStorage.OnCreate {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre createGenre(Genre genre) {

        final Map<String, Object> genreFields = new HashMap<>();
        genreFields.put("name", genre.getName());

        final SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("genres")
                .usingGeneratedKeyColumns("id");

        final int genreId =  simpleJdbcInsert.executeAndReturnKey(
                genreFields).intValue();

        genre.setId(genreId);

        return genre;

    }
}
