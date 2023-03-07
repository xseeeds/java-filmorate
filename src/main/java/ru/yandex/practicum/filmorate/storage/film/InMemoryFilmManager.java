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
    private final HashSet<Film> setFilms = new HashSet<>();

    @Override
    public ResponseEntity<Film> addFilm(Film film) {

        if (film.getId() != 0) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "POST request. Для обновления используй PUT запрос");
        }

        if (setFilms.contains(film)) {

            log.error("Такой фильм: {} уже существует, для обновления используй PUT запрос", film);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой фильм: "
                            + film
                            + " уже существует, для обновления используй PUT запрос");
        }

        Film newFilm = film.toBuilder().id(films.size() + 1)
                .build();
        films.put(newFilm.getId(), newFilm);

        setFilms.add(newFilm);

        return ResponseEntity.status(HttpStatus.CREATED).body(newFilm);
    }


    @Override
    public ResponseEntity<Film> updateFilm(Film newFilm) {

        if (newFilm.getId() == 0) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PUT request. Для обновления используй id в теле запроса");
        }

        if (setFilms.contains(newFilm)) {

            int existentId = setFilms
                    .stream()
                    .filter(film -> film.equals(newFilm))
                    .findFirst()
                    .get()
                    .getId();

            log.error("Такой фильм: {} уже существует по id {}", newFilm, existentId);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой фильм: "
                            + newFilm
                            + " уже существует, по id: " + existentId);
        }

        if (films.containsKey(newFilm.getId())) {

            films.put(newFilm.getId(), newFilm);

            setFilms.add(newFilm);

            return ResponseEntity.status(HttpStatus.OK).body(newFilm);
        }
        log.error("Такой фильм: {} не существует", newFilm);

        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Такой фильм:"
                        + newFilm
                        + " не существует");
    }

    @Override
    public Collection<Film> getAllFilm() {

        log.info("Текущее количество фильмов : {}", films.size());

        return films.values();
    }

    @Override
    public ResponseEntity<String> removeAllFilm() {
        films.clear();
        setFilms.clear();

        log.info("Все фильмы удалены. Текущее количество фильмов: {}", films.size());

        return ResponseEntity.status(HttpStatus.RESET_CONTENT).body("Все фильмы удалены. " +
                "Текущее количество фильмов: " + films.size());
    }
}
