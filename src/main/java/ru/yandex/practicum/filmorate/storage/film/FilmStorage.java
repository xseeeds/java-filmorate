package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public interface FilmStorage {

    Film createFilm(Film film);

    void resetGlobalId();

    Film updateFilm(Film film);

    Film getFilmById(long filmId) throws NotFoundException;

    List<Film> getAllFilm();

    List<Film> getFilmByPopular(int count);

    void removeFilmById(long filmId);

    void removeAllFilm();

    void checkFilmById(long filmId) throws NotFoundException;

    void checkFilmByNameReleaseDate(Film film) throws ConflictException;

    void checkFilmLikeByUserId(long filmId, long userId, boolean addOrRemove) throws ConflictException, NotFoundException;

    void addUserLikeOnFilm(long filmId, long userId, int mark);

    void removeUserLikeOnFilm(long filmId, long userId, int mark);

    List<Film> getFilmsByDirector(long directorId, String sortBy) throws NotFoundException;

    Film makeFilm(ResultSet resultSet, int rowNumber) throws SQLException;
}
