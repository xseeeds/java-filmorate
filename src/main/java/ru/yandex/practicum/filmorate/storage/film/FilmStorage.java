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

    List<Film> getFilmByPopular(int count, String genre, Integer year);

    void removeFilmById(long filmId);

    void removeAllFilm();

    void checkFilmById(long filmId) throws NotFoundException;

    void checkFilmByNameReleaseDate(Film film) throws ConflictException;

    void checkFilmLikeByUserId(long filmId, long userId, boolean addOrRemove) throws ConflictException, NotFoundException;

    void addUserMarkOnFilm(long filmId, long userId, int mark);

    void removeUserMarkOnFilm(long filmId, long userId, int mark);

    List<Film> getFilmsByDirector(long directorId, String sortBy) throws NotFoundException;

    List<Film> getCommonFilms(long userId, long otherId);

    List<Film> getFilmsBySearch(String query, String by);

    Film makeFilm(ResultSet resultSet, int rowNumber) throws SQLException;
}
