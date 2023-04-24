package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Film createFilm(Film film);

    void resetGlobalId();

    Film updateFilm(Film film);

    Film getFilmById(long filmId);

    Collection<Film> getAllFilm();

    Collection<Film> getFilmByPopular(int count);

    void removeFilmById(long filmId);

    void removeAllFilm();

    void checkFilmById(long filmId) throws NotFoundException;

    void checkFilmByNameReleaseDateDuration(Film film) throws ConflictException;

    void checkFilmLikeByUserId(long filmId, long userId, boolean addOrRemove) throws ConflictException, NotFoundException;

    void addUserLikeOnFilm(long filmId, long userId);

    void removeUserLikeOnFilm(long filmId, long userId);
}
