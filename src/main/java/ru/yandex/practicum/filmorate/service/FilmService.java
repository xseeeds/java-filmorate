package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.Collection;

import static java.util.stream.Collectors.toList;


@Service
@Slf4j
@Validated
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmStorage.OnCreate filmStorageOnCreate;
    private final FilmStorage.OnUpdate filmStorageOnUpdate;
    private final UserService userService;

    @Autowired
    public FilmService(
            FilmStorage filmStorage,
            UserService userService,
            FilmStorage.OnCreate filmStorageOnCreate,
            FilmStorage.OnUpdate filmStorageOnUpdate
    ) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.filmStorageOnCreate = filmStorageOnCreate;
        this.filmStorageOnUpdate = filmStorageOnUpdate;
    }


    @Validated({FilmStorage.OnCreate.class, FilmStorage.class})
    public Film createFilm(@Valid Film film) throws ConflictException {

        checkFilmByNameReleaseDateDuration(film);

        final Film createdFilm = filmStorageOnCreate.createFilm(film);

        log.info("Фильм добавлен =>{}", createdFilm);

        return createdFilm;
    }

    @Validated({FilmStorage.OnUpdate.class, FilmStorage.class})
    public Film updateFilm(@Valid Film film) throws NotFoundException, ConflictException {

        checkFilmById(film.getId());

        checkFilmByNameReleaseDateDuration(film);

        final Film updatedFilm = filmStorageOnUpdate.updateFilm(film);

        log.info("Фильм обновлен =>{}", updatedFilm);

        return updatedFilm;
    }

    public Collection<Film> getAllFilm() {

        final Collection<Film> allFilm = filmStorage.getAllFilm();

        log.info("Фильм получены (кол-во) =>{}", allFilm.size());

        return allFilm;
    }

    public Film getFilmById(@Positive long filmId) throws NotFoundException {

        final Film film = filmStorage.getFilmById(filmId);

        log.info("Фильм получен c id=>{} =>>>{}", filmId, film);

        return film;
    }

    public String removeAllFilm() {

        filmStorage.removeAllFilm();

        filmStorageOnCreate.resetGlobalId();

        log.info("Все фильмы удалены, id сброшен");

        return "Все фильмы удалены. id сброшен";
    }

    public String removeFilmById(@Positive long filmId) throws NotFoundException {

        filmStorage.removeFilmById(filmId);

        log.info("Фильм c id=>{} удален", filmId);

        return "Фильм c id=>" + filmId + "удален";
    }

    public Film addUserLikeByFilmId(@Positive long filmId, @Positive long userId) throws ConflictException, NotFoundException {

        final Film film = filmStorage.getFilmById(filmId);

        userService.checkUserById(userId);

        checkFilmLikeByUserId(film, userId, true);

        film.getLikes().add(userId);

        final Film updatedFilm = filmStorageOnUpdate.updateFilm(film);

        log.info("Пользователем c id=>{} добавлен лайк фильму c id=>{}", userId, filmId);

        return updatedFilm;
    }

    public Film removeUserLikeByFilmId(@Positive long filmId, @Min(-2) long userId) throws ConflictException, NotFoundException {

        final Film film = filmStorage.getFilmById(filmId);

        userService.checkUserById(userId);

        checkFilmLikeByUserId(film, userId, false);

        film.getLikes().remove(userId);

        final Film updatedFilm = filmStorageOnUpdate.updateFilm(film);

        log.info("Пользователем c id=>{} удален лайк у фильма c id=>{}", userId, filmId);

        return updatedFilm;
    }

    public Collection<Film> getFilmByPopular(@Positive int count) {

        final Collection<Film> filmByPopular = filmStorage
                .getAllFilm()
                .stream()
                .sorted(this::filmCompareByLikes)
                .limit(count)
                .collect(toList());

        log.info("Запрошенное количество фильмов по популярности : {}", filmByPopular.size());

        return filmByPopular;
    }

    private void checkFilmLikeByUserId(Film film, long userId, boolean param) {

        if (param) {

            if (film.getLikes().contains(userId)) {
                throw new ConflictException("У фильма с id=>" + film.getId()
                        + " уже существует лайк пользователя с id=>" + userId);
            }

        } else {

            if (!film.getLikes().contains(userId)) {
                throw new NotFoundException("У фильма с id=>" + film.getId()
                        + " не существует лайка пользователя с id=>" + userId);
            }
        }
    }

    private void checkFilmByNameReleaseDateDuration(Film film) {

        final Film existentFilm = filmStorage.getFilm(film);

        if (existentFilm != null) {
            throw new ConflictException("Такой фильм: " + film
                    + " уже существует, по id=>" + existentFilm.getId());
        }
    }

    private void checkFilmById(long filmId) {

        final Long existentId = filmStorage.getIdExistentFilm(filmId);

        if (existentId == null) {
            throw new NotFoundException("Такой фильм с id: " + filmId + " не существует");
        }
    }

    private int filmCompareByLikes(Film f0, Film f1) {
        return Integer.compare(f1.getLikes().size(), f0.getLikes().size());
    }


    /*private int setRatingFilm(Film film) {
        return film.getLikes().values().stream().mapToInt(Integer::intValue).sum() / likes.size();
    }*/

}
