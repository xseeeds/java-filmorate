package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;


@Primary
@Component
public class FilmStorageImpl implements FilmStorage {
    protected static final TreeSet<Film> films = new TreeSet<>(Comparator
            .comparing(Film::getName)
            .thenComparing(Film::getReleaseDate)
            .thenComparing(Film::getDuration));
    protected static final TreeSet<Long> idsFilms = new TreeSet<>();

    @Override
    public Film getFilm(Film film) {
        Film ceil = films.ceiling(film);
        Film floor = films.floor(film);
        return Objects.equals(ceil, floor) ? ceil : null;
    }

    @Override
    public Film getFilmById(long filmId) {
        return films
                .stream()
                .filter(f -> f.getId() == filmId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Такой фильм с id: " + filmId + " не существует"));
    }

    @Override
    public Collection<Film> getAllFilm() {
        return films;
    }

    @Override
    public void removeFilmById(long filmId) {
        films.remove(films
                .stream()
                .filter(f -> f.getId() == filmId)
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Такой фильм с id: " + filmId + " не существует")));
        idsFilms.remove(filmId);
    }

    @Override
    public void removeAllFilm() {
        films.clear();
        idsFilms.clear();
    }

    @Override
    public Long getIdExistentFilm(long filmId) {
        Long ceil = idsFilms.ceiling(filmId);
        Long floor = idsFilms.floor(filmId);
        return Objects.equals(ceil, floor) ? ceil : null;
    }

}