package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    ResponseEntity<Film> addFilm(Film film);
    ResponseEntity<Film> updateFilm(Film film);
    Collection<Film> getAllFilm();
    ResponseEntity<String> removeAllFilm();
}
