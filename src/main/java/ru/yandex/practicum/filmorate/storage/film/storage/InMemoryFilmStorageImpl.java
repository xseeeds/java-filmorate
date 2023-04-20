package ru.yandex.practicum.filmorate.storage.film.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

import static java.util.stream.Collectors.toList;


@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InMemoryFilmStorageImpl implements FilmStorage {
    private final FilmStorage.OnCreate inMemoryFilmStorageImplOnCreate;

    protected static final TreeSet<Film> films = new TreeSet<>(Comparator
            .comparing(Film::getLikesSize)
            .thenComparing(Film::getName)
            .thenComparing(Film::getReleaseDate)
            .thenComparing(Film::getDuration));
    protected static final TreeSet<Long> idsFilms = new TreeSet<>();


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
    public Collection<Film> getFilmByPopular(int count) {
        return films
                .stream()
                //.sorted(this::filmCompareByLikes)
                .limit(count)
                .collect(toList());
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
        inMemoryFilmStorageImplOnCreate.resetGlobalId();
    }

    @Override
    public void checkFilmById(long filmId) throws NotFoundException {

        final Long ceil = idsFilms.ceiling(filmId);
        final Long floor = idsFilms.floor(filmId);

        if (!Objects.equals(ceil, floor) || ceil == null) {
            throw new NotFoundException("Такой фильм с id: " + filmId + " не существует");
        }
    }

    @Override
    public void checkFilmByNameReleaseDateDuration(Film film) throws ConflictException {

        final Film ceil = films.ceiling(film);
        final Film floor = films.floor(film);

        if (Objects.equals(ceil, floor) && ceil != null) {
            throw new ConflictException("Такой фильм: " + film
                    + " уже существует, по id => " + ceil.getId());
        }
    }

    @Override
    public void checkFilmLikeByUserId(long filmId, long userId, boolean addOrRemove) throws ConflictException, NotFoundException {

        final Film film = getFilmById(filmId);

        if (addOrRemove) {

            if (film.getLikes().contains(userId)) {
                throw new ConflictException("У фильма с id => " + film.getId()
                        + " уже существует лайк пользователя с id => " + userId);
            }

        } else {

            if (!film.getLikes().contains(userId)) {
                throw new NotFoundException("У фильма с id => " + film.getId()
                        + " не существует лайка пользователя с id => " + userId);
            }
        }
    }

    @Override
    public void addUserLikeOnFilm(long filmId, long userId) {
        getFilmById(filmId).getLikes().add(userId);
    }

    @Override
    public void removeUserLikeOnFilm(long filmId, long userId) {
        getFilmById(filmId).getLikes().remove(userId);
    }

/*
    private Film getFilm(Film film) {
        Film ceil = films.ceiling(film);
        Film floor = films.floor(film);
        return Objects.equals(ceil, floor) ? ceil : null;
    }
*/

/*
    private int filmCompareByLikes(Film f0, Film f1) {
        return Integer.compare(f1.getLikes().size(), f0.getLikes().size());
    }
*/
}