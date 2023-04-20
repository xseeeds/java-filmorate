package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface GenreStorage {

    interface OnCreate {
        Genre createGenre(Genre genre);
    }

    interface OnUpdate {
        Genre updateGenre(Genre genre);
    }

    void addGenreOnFilm(int genreId, long filmId);

    void removeGenreOnFilm(int genreId, long filmId);

    List<Genre> getGenreList();

    Genre getGenreById(int id);

    void checkGenreById(int id);

    void checkGenre(Genre genre);

    void removeGenreById(int id);

    void removeAllGenre();

    Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException;

    void checkGenreOnFilm(int genreId, long filmId, boolean addOrRemove);
}