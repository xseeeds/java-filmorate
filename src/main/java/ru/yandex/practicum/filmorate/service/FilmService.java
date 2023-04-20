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
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.Collection;


@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmService {
    private final FilmStorage dbFilmStorageImpl;
    private final FilmStorage.OnCreate dbFilmStorageImplOnCreate;
    private final FilmStorage.OnUpdate dbFilmStorageImplOnUpdate;
    private final UserStorage dbUserStorageImpl;

    @Validated({FilmStorage.OnCreate.class, FilmStorage.class})
    public Film createFilm(@Valid Film film) throws ConflictException {

        dbFilmStorageImpl.checkFilmByNameReleaseDateDuration(film);

        final Film createdFilm = dbFilmStorageImplOnCreate.createFilm(film);

        log.info("Фильм добавлен => {}", createdFilm);

        return createdFilm;
    }

    @Validated({FilmStorage.OnUpdate.class, FilmStorage.class})
    public Film updateFilm(@Valid Film film) throws NotFoundException, ConflictException {

        dbFilmStorageImpl.checkFilmById(film.getId());

        dbFilmStorageImpl.checkFilmByNameReleaseDateDuration(film);

        final Film updatedFilm = dbFilmStorageImplOnUpdate.updateFilm(film);

        log.info("Фильм обновлен => {}", updatedFilm);

        return updatedFilm;
    }

    public Collection<Film> getAllFilm() {

        final Collection<Film> allFilm = dbFilmStorageImpl.getAllFilm();

        log.info("Фильм получены (кол-во) => {}", allFilm.size());

        return allFilm;
    }

    public Film getFilmById(@Positive long filmId) throws NotFoundException {

        final Film film = dbFilmStorageImpl.getFilmById(filmId);

        log.info("Фильм получен c id => {} =>>> {}", filmId, film);

        return film;
    }

    public String removeAllFilm() {

        dbFilmStorageImpl.removeAllFilm();

        log.info("Все фильмы удалены, id сброшен");

        return "Все фильмы удалены. id сброшен";
    }

    public String removeFilmById(@Positive long filmId) throws NotFoundException {

        dbFilmStorageImpl.removeFilmById(filmId);

        log.info("Фильм c id => {} удален", filmId);

        return "Фильм c id => " + filmId + "удален";
    }

    public void addUserLikeByFilmId(@Positive long filmId, @Positive long userId) throws ConflictException, NotFoundException {

        dbFilmStorageImpl.checkFilmById(filmId);

        dbUserStorageImpl.checkUserById(userId);

        dbFilmStorageImpl.checkFilmLikeByUserId(filmId, userId, true);

        dbFilmStorageImpl.addUserLikeOnFilm(filmId, userId);

        log.info("Пользователем c id => {} добавлен лайк фильму c id => {}", userId, filmId);
    }

    public void removeUserLikeByFilmId(@Positive long filmId, @Min(-2) long userId) throws ConflictException, NotFoundException {

        dbFilmStorageImpl.checkFilmById(filmId);

        dbUserStorageImpl.checkUserById(userId);

        dbFilmStorageImpl.checkFilmLikeByUserId(filmId, userId, false);

        dbFilmStorageImpl.removeUserLikeOnFilm(filmId, userId);

        log.info("Пользователем c id => {} удален лайк у фильма c id => {}", userId, filmId);
    }

    public Collection<Film> getFilmByPopular(@Positive int count) {

        final Collection<Film> filmByPopular = dbFilmStorageImpl.getFilmByPopular(count);

        log.info("Запрошенное количество фильмов по популярности : {}", filmByPopular.size());

        return filmByPopular;
    }



/*
    private int setRatingFilm(Film film) {
        return film.getLikes().values().stream().mapToInt(Integer::intValue).sum() / likes.size();
    }
*/

}
