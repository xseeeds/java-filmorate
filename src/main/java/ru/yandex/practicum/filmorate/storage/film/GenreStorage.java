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

    void addGenreOnFilm(long genreId, long filmId);

    void removeGenreOnFilm(long genreId, long filmId);

    void checkGenreOnFilm(long genreId, long filmId, boolean addOrRemove) throws NotFoundException, ConflictException;

    List<Genre> getAllGenre();

    Genre getGenreById(long id) throws NotFoundException;

    void checkGenreById(long id) throws NotFoundException;

    void checkGenreByName(String genre, boolean ifExist) throws NotFoundException, ConflictException;

    void removeGenreById(long id) throws NotFoundException;

    void removeAllGenre();

    Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException;
}