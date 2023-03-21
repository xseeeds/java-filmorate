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

import java.util.Collection;

import static java.util.stream.Collectors.toList;


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


    public ResponseEntity<Film> addFilm(Film film) throws ResponseStatusException {

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
                .body(film);
    }

    public ResponseEntity<Film> getFilmById(int filmId) throws ResponseStatusException {

        final Film film = filmStorage.getFilmById(filmId);

        log.info("Фильм получен c id=>{} =>>>{}", filmId, film);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(film);
    }

    public ResponseEntity<Film> updateFilm(Film film) throws ResponseStatusException {

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
                .body(film);
    }

    public ResponseEntity<Collection<Film>> getAllFilm() {

        final Collection<Film> allFilm = filmStorage.getAllFilm();

        log.info("Текущее количество фильмов : {}", allFilm.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(allFilm);
    }

    public ResponseEntity<Film> removeFilmById(int filmId) throws ResponseStatusException {

        final Film film = filmStorage.removeFilmById(filmId);

        log.info("Фильм удален c id=>{} ===>{}", filmId, film);

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body(film);
    }

    public ResponseEntity<String> removeAllFilm() {

        filmStorage.removeAllFilm();

        log.info("Все фильмы удалены.");

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body("205 (RESET_CONTENT) Все фильмы удалены.");
    }

    public ResponseEntity<Film> addUserLikeByFilmId(int filmId, int userId) throws ResponseStatusException {

        final Film film = filmStorage.getFilmById(filmId);

        userStorage.checkUserById(userId);

        checkFilmLikeByUserId(film, userId, true);

        film.getLikes().add(userId);

        filmStorage.updateFilm(film);

        log.info("Пользователем c id=>{} добавлен лайк фильму c id=>{}", userId, filmId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(film);
    }

    public ResponseEntity<Film> removeUserLikeByFilmId(int filmId, int userId) {

        final Film film = filmStorage.getFilmById(filmId);

        userStorage.checkUserById(userId);

        checkFilmLikeByUserId(film, userId, false);

        film.getLikes().remove(userId);

        filmStorage.updateFilm(film);

        log.info("Пользователем c id=>{} удален лайк у фильма c id=>{}", userId, filmId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(film);
    }

    public ResponseEntity<Collection<Film>> getFilmByPopular(int count) {

        final Collection<Film> filmByPopular = filmStorage
                .getAllFilm()
                .stream()
                .sorted(this::filmCompareByLikes)
                .limit(count)
                .collect(toList());

        log.info("Запрошенное количество фильмов по популярности : {}", filmByPopular.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(filmByPopular);
    }

    private void checkFilmLikeByUserId (Film film, int userId, boolean param) {

        if (param) {

            if (film.getLikes().contains(userId)) {

                log.error("У фильма с id=>{} уже существует лайк пользователя с id=>{}", film.getId(), userId);

                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "У фильма с id=>" + film.getId() + " уже существует лайк пользователя с id=>" + userId);
            }

        } else {

            if (!film.getLikes().contains(userId)) {

                log.error("У фильма с id=>{} не существует лайка пользователя с id=>{}", film.getId(), userId);

                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "У фильма с id=>" + film.getId() + " не существует лайка пользователя с id=>" + userId);
            }
        }
    }

    private int filmCompareByLikes(Film f0, Film f1) {
        return Integer.compare(f1.getLikes().size(), f0.getLikes().size());
    }


    /*private int setRatingFilm(Film film) {
        return film.getLikes().values().stream().mapToInt(Integer::intValue).sum() / likes.size();
    }*/

}
