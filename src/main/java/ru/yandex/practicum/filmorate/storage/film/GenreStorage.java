package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface GenreStorage {

    Genre createGenre(Genre genre);

    Genre updateGenre(Genre genre);

    void addGenreOnFilm(int genreId, long filmId);

    void removeGenreOnFilm(int genreId, long filmId);

    void checkGenreOnFilm(int genreId, long filmId, boolean addOrRemove) throws NotFoundException, ConflictException;

    List<Genre> getGenreList();

    Genre getGenreById(int id) throws NotFoundException;

    void checkGenreById(int id) throws NotFoundException;

    void checkGenre(Genre genre) throws NotFoundException;

    void removeGenreById(int id);

    void removeAllGenre();

    Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException;
}