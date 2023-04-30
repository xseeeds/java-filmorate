package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;


@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;


    @Validated
    public Film createFilm(@Valid Film film) throws ConflictException {

        filmStorage.checkFilmByNameReleaseDateDuration(film);

        final Film createdFilm = filmStorage.createFilm(film);

        log.info("Фильм добавлен => {}", createdFilm);

        return createdFilm;
    }

    @Validated
    public Film updateFilm(@Valid Film film) throws NotFoundException, ConflictException {

        filmStorage.checkFilmById(film.getId());

        filmStorage.checkFilmByNameReleaseDateDuration(film);

        final Film updatedFilm = filmStorage.updateFilm(film);

        log.info("Фильм обновлен => {}", updatedFilm);

        return updatedFilm;
    }

    public List<Film> getAllFilm() {

        final List<Film> allFilm = filmStorage.getAllFilm();

        log.info("Фильм получены (кол-во) => {}", allFilm.size());

        return allFilm;
    }

    public Film getFilmById(@Positive long filmId) throws NotFoundException {

        final Film film = filmStorage.getFilmById(filmId);

        log.info("Фильм получен c id => {} =>>> {}", filmId, film);

        return film;
    }

    public String removeAllFilm() {

        filmStorage.removeAllFilm();

        log.info("Все фильмы удалены, id сброшен");

        return "Все фильмы удалены. id сброшен";
    }

    public String removeFilmById(@Positive long filmId) throws NotFoundException {

        filmStorage.removeFilmById(filmId);

        log.info("Фильм c id => {} удален", filmId);

        return "Фильм c id => " + filmId + "удален";
    }

    public void addUserLikeByFilmId(@Positive long filmId, @Positive long userId, @Min(0) @Max(10) int mark) throws ConflictException, NotFoundException {

        filmStorage.checkFilmById(filmId);

        userStorage.checkUserById(userId);

        filmStorage.checkFilmLikeByUserId(filmId, userId, true);

        filmStorage.addUserLikeOnFilm(filmId, userId, mark);

        log.info("Пользователем c id => {} добавлен лайк фильму c id => {}", userId, filmId);
    }

    public void removeUserLikeByFilmId(@Positive long filmId, @Min(-2) long userId, @Min(0) @Max(10) int mark) throws ConflictException, NotFoundException {

        filmStorage.checkFilmById(filmId);

        userStorage.checkUserById(userId);

        filmStorage.checkFilmLikeByUserId(filmId, userId, false);

        filmStorage.removeUserLikeOnFilm(filmId, userId, mark);

        log.info("Пользователем c id => {} удален лайк у фильма c id => {}", userId, filmId);
    }

    public List<Film> getFilmByPopular(@Positive int count) {

        final List<Film> filmByPopular = filmStorage.getFilmByPopular(count);

        log.info("Запрошенное количество фильмов по популярности : {}", filmByPopular.size());

        return filmByPopular;
    }

}
