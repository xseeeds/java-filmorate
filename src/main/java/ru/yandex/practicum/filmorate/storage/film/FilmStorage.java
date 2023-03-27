package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    interface OnCreate {
        Film createFilm(Film film);
        void resetGlobalId();
    }

    interface OnUpdate {
        Film updateFilm(Film film);
    }

    Film getFilm(Film film);

    Film getFilmById(long filmId);


    Collection<Film> getAllFilm();

    void removeFilmById(long filmId);

    void removeAllFilm();

    Long getIdExistentFilm(long filmId);

}
