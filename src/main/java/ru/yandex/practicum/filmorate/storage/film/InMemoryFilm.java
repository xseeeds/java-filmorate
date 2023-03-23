package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;


@Component
@Slf4j
public class InMemoryFilm implements FilmStorage {
    private final TreeSet<Film> films = new TreeSet<>(Comparator
            .comparing(Film::getName)
            .thenComparing(Film::getReleaseDate)
            .thenComparing(Film::getDuration));
    private final TreeSet<Integer> idsFilms = new TreeSet<>();

    @Override
    public void addFilm(Film film) {
        films.add(film);
        idsFilms.add(film.getId());
    }

    @Override
    public Film getFilm(Film film) {
        Film ceil = films.ceiling(film);
        Film floor = films.floor(film);
        return Objects.equals(ceil, floor) ? ceil : null;
    }

    @Override
    public Film getFilmById(int filmId) {
        Optional<Film> film = films
                .stream()
                .filter(f -> f.getId() == filmId)
                .findFirst();

        if (film.isEmpty()) {
            log.error("Такой фильм с id: {} не существует", filmId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Такой фильм с id: "
                            + filmId
                            + " не существует");
        }
        return film.get();
    }

    @Override
    public void updateFilm(Film film) {
        Optional<Film> oldFilm = films
                .stream()
                .filter(f -> f.getId() == film.getId())
                .findFirst();
        if (oldFilm.isPresent()) {
            films.remove(oldFilm.get());
            films.add(film);
        }
    }

    @Override
    public Collection<Film> getAllFilm() {
        return films;
    }

    @Override
    public Film removeFilmById(int filmId) {
        Optional<Film> film = films
                .stream()
                .filter(f -> f.getId() == filmId)
                .findFirst();
        if (film.isEmpty()) {
            log.error("Такой фильм с id: {} не существует", filmId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Такой фильм с id: "
                            + filmId
                            + " не существует");
        }
        films.remove(film.get());
        idsFilms.remove(filmId);
        return film.get();
    }

    @Override
    public void removeAllFilm() {
        films.clear();
        idsFilms.clear();
    }

    @Override
    public Integer getIdExistentFilm(int filmId) {
        Integer ceil = idsFilms.ceiling(filmId);
        Integer floor = idsFilms.floor(filmId);
        return Objects.equals(ceil, floor) ? ceil : null;
    }

}