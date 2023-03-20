package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import static java.util.stream.Collectors.toCollection;

@Service
@Slf4j
public class FilmService {
    FilmStorage filmStorage;
    UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }


    public ResponseEntity<Map<String, Film>> addFilm(Film film) throws ResponseStatusException {

        if (film.getId() != 0) {

            log.error("POST request. Для обновления используй PUT запрос, film имеет id => {}", film);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "POST request. Для обновления используй PUT запрос");
        }

        filmStorage.checkFilm(film);

        filmStorage.addFilm(film);

        log.info("newFilm {}", film);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("Фильм добавлен c id=>" + film.getId(), film));
    }

    public ResponseEntity<Map<String, Film>> getFilmById(int filmId) throws ResponseStatusException {

        final Film film = filmStorage.getFilmById(filmId);

        log.info("Фильм получен c id=>{} =>>>{}", filmId, film);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Запрос пользователя по id ", film));
    }

    public ResponseEntity<Map<String, Film>> updateFilm(Film film) throws ResponseStatusException {

        if (film.getId() == 0) {

            log.error("PUT request. Для обновления используй id в теле запроса film => {}", film);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PUT request. Для обновления используй id в теле запроса");
        }

        filmStorage.checkFilmById(film.getId());

        filmStorage.checkFilmByNameReleaseDateDuration(film);

        log.info("updateFilm {}", film);

        filmStorage.updateFilm(film);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Фильм обновлен c id=>" + film.getId(), film));
    }

    public ResponseEntity<Map<String, Collection<Film>>> getAllFilm() {

        final Collection<Film> allFilm = filmStorage.getAllFilm();

        log.info("Текущее количество фильмов : {}", allFilm.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Все фильмы=>", allFilm));
    }

    public ResponseEntity<Map<String, Film>> removeFilmById(int filmId) throws ResponseStatusException {

        final Film film = filmStorage.removeFilmById(filmId);

        log.info("Фильм удален c id=>{} ===>{}", filmId, film);

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body(Map.of("Фильм удален c id=>" + filmId, film));
    }

    public ResponseEntity<String> removeAllFilm() {

        filmStorage.removeAllFilm();

        log.info("Все фильмы удалены.");

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body("\"Все фильмы удалены.\"");
    }

    public ResponseEntity<Map<String, Film>> addUserLikeByFilmId(int filmId, int userId) throws ResponseStatusException {

        final Film film = filmStorage.getFilmById(filmId);

        userStorage.checkUserById(userId);

        if (film.getLikes().contains(userId)) {

            log.error("У фильма с id=>{} уже существует лайк пользователя с id=>{}", film.getId(), userId);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "У фильма с id=>" + film.getId() + " уже существует лайк пользователя с id=>" + userId);
        }

        film.getLikes().add(userId);

        filmStorage.updateFilm(film);

        log.info("Пользователем c id=>{} добавлен лайк фильму c id=>{}", userId, filmId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("Пользователем c id=>" + userId + " добавлен лайк фильму  id=>" + filmId, film));
    }

    public ResponseEntity<Map<String, Film>> removeUserLikeByFilmId(int filmId, int userId) {

        final Film film = filmStorage.getFilmById(filmId);

        userStorage.checkUserById(userId);

        if (!film.getLikes().contains(userId)) {

            log.error("У фильма с id=>{} не существует лайка пользователя с id=>{}", film.getId(), userId);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "У фильма с id=>" + film.getId() + " не существует лайка пользователя с id=>" + userId);
        }

        film.getLikes().remove(userId);

        filmStorage.updateFilm(film);

        log.info("Пользователем c id=>{} удален лайк у фильма c id=>{}", userId, filmId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("Пользователем c id=>" + userId + " удален лайк у фильма c id=>" + filmId, film));
    }

    public ResponseEntity<Map<String, Collection<Film>>> getFilmByPopular(int count) {

        final Collection<Film> filmByPopular = filmStorage
                .getAllFilm()
                .stream()
                .sorted(this::filmCompareByLikes)
                .limit(count)
                .collect(toCollection(ArrayList::new));

        log.info("Запрошенное количество фильмов по популярности : {}", filmByPopular.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Запрос фильмов по полярности=>", filmByPopular));
    }

    private int filmCompareByLikes(Film f0, Film f1) {
        return Integer.compare(f0.getLikes().size(), f1.getLikes().size());
    }


    /*private int setRatingFilm(Film film) {
        return film.getLikes().values().stream().mapToInt(Integer::intValue).sum() / likes.size();
    }*/

}
