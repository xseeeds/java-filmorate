package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
public class InMemoryFilmManager implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public ResponseEntity<Film> addFilm(Film film) {

        if (film.getId() != 0) {

            log.error("POST request. Для обновления используй PUT запрос, film имеет id => {}", film);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "POST request. Для обновления используй PUT запрос");
        }

        if (films
                .values()
                .stream()
                .anyMatch(existentFilm -> existentFilm.equals(film))) {

            log.error("Такой фильм: {} уже существует, для обновления используй PUT запрос", film);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой фильм: "
                            + film
                            + " уже существует, для обновления используй PUT запрос");
        }

        log.info("film {}", film);

        Film newFilm = film.toBuilder().id(films.size() + 1)
                .build();
        films.put(newFilm.getId(), newFilm);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(newFilm);
    }


    @Override
    public ResponseEntity<Film> updateFilm(Film newFilm) {

        if (newFilm.getId() == 0) {

            log.error("PUT request. Для обновления используй id в теле запроса newFilm => {}", newFilm);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PUT request. Для обновления используй id в теле запроса");
        }

        if (!films.containsKey(newFilm.getId())) {

            log.error("Такой фильм: {} не существует", newFilm);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Такой фильм:"
                            + newFilm
                            + " не существует");
        }

        final Film existentFilm = films
                .values()
                .stream()
                .filter(searchFilm -> searchFilm.equals(newFilm))
                .findFirst()
                .orElse(null);

        if (existentFilm != null) {

            log.error("Такой фильм: {} уже существует по id {}", newFilm, existentFilm.getId());

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой фильм: "
                            + newFilm
                            + " уже существует, по id: " + existentFilm.getId());
        }

        log.info("newFilm {}", newFilm);

        films.put(newFilm.getId(), newFilm);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(newFilm);
    }

    @Override
    public Collection<Film> getAllFilm() {

        log.info("Текущее количество фильмов : {}", films.size());

        return films.values();
    }

    @Override
    public ResponseEntity<String> removeAllFilm() {

        films.clear();

        log.info("Все фильмы удалены.");

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body("Все фильмы удалены.");
    }
}
