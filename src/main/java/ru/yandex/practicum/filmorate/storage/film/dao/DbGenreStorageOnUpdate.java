package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbGenreStorageOnUpdate implements GenreStorage.OnUpdate {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre updateGenre(Genre genre) {

        final String sqlQuery =
                "UPDATE genres " +
                        "SET name = ? " +
                        "WHERE id = ?";

        jdbcTemplate.update(sqlQuery,
                genre.getName(), genre.getId());

        return genre;

    }
}
