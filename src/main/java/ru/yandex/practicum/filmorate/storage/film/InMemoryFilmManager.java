package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Optional;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;


@Component
@Slf4j
public class InMemoryFilmManager implements FilmStorage {
    private final TreeSet<Film> films = new TreeSet<>(Comparator
            .comparing(Film::getName)
            .thenComparing(Film::getReleaseDate)
            .thenComparing(Film::getDuration));

    private Integer globalId = 0;
    private final TreeSet<Integer> idsFilms = new TreeSet<>();

    @Override
    public void addFilm(Film film) {
        film.setId(getNextId());
        films.add(film);
        idsFilms.add(film.getId());
    }

    @Override
    public void checkFilmByNameReleaseDateDuration(Film film) {

        Film ceil = films.ceiling(film);
        Film floor = films.floor(film);

        Film existentFilm = ceil == floor ? ceil : null;

        if (existentFilm != null) {

            log.error("Такой фильм: {} уже существует по id {}", film, existentFilm.getId());

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой фильм: "
                            + film
                            + " уже существует, по id: " + existentFilm.getId());
        }
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
        Film ceil = films.ceiling(film);
        Film floor = films.floor(film);

        Film oldFilm = ceil == floor ? ceil : null;

        films.remove(oldFilm);
        films.add(film);
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
        resetGlobalId();
    }

    @Override
    public void checkFilm(Film film) {

        if (!films.contains(film)) {

            log.error("Такой фильм: {} уже существует, для обновления используй PUT запрос", film);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой фильм: "
                            + film
                            + " уже существует, для обновления используй PUT запрос");
        }
    }

    @Override
    public void checkFilmById(int filmId) {

        if (idsFilms.contains(filmId)) {

            log.error("Такой фильм с id: {} не существует", filmId);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Такой фильм с id: "
                            + filmId
                            + " не существует");
        }
        //films.stream().noneMatch(film -> film.getId() == filmId);
    }

    private Integer getNextId() {
        return ++globalId;
    }

    private void resetGlobalId() {
        globalId = 0;
    }
}