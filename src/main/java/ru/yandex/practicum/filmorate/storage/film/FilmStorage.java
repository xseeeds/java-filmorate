package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    void addFilm(Film film);

    Film getFilm(Film film);

    Film getFilmById(int filmId);

    void updateFilm(Film film);

    Collection<Film> getAllFilm();

    Film removeFilmById(int filmId);

    void removeAllFilm();

    Integer getIdExistentFilm(int filmId);

}
