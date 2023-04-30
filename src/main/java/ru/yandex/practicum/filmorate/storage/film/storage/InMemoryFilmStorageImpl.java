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
    private final TreeSet<Film> films = new TreeSet<>(Comparator
            .comparing(Film::getLikesSize)
            .thenComparing(Film::getName)
            .thenComparing(Film::getReleaseDate)
            .thenComparing(Film::getDuration));
    private final TreeSet<Long> idsFilms = new TreeSet<>();
    private long globalId = 0;

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        films.add(film);
        idsFilms.add(film.getId());
        return film;
    }

    @Override
    public void resetGlobalId() {
        globalId = 0;
    }

    @Override
    public Film updateFilm(Film film) {
        Optional<Film> oldFilm = films
                .stream()
                .filter(f -> f.getId().equals(film.getId()))
                .findFirst();
        if (oldFilm.isPresent()) {
            films.remove(oldFilm.get());
            films.add(film);
        }
        return film;
    }

    @Override
    public Film getFilmById(long filmId) {
        return films
                .stream()
                .filter(f -> f.getId() == filmId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Такой фильм с id: " + filmId + " не существует"));
    }

    @Override
    public List<Film> getAllFilm() {
        return new ArrayList<>(films);
    }

    @Override
    public List<Film> getFilmByPopular(int count) {
        return films
                .stream()
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
        resetGlobalId();
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

            if (film.getUserFilmLike().containsKey(userId)) {
                throw new ConflictException("У фильма с id => " + film.getId()
                        + " уже существует лайк пользователя с id => " + userId);
            }

        } else {

            if (!film.getUserFilmLike().containsKey(userId)) {
                throw new NotFoundException("У фильма с id => " + film.getId()
                        + " не существует лайка пользователя с id => " + userId);
            }
        }
    }

    @Override
    public void addUserLikeOnFilm(long filmId, long userId, int mark) {
        getFilmById(filmId).getUserFilmLike().put(userId, mark);
    }

    @Override
    public void removeUserLikeOnFilm(long filmId, long userId, int mark) {
        getFilmById(filmId).getUserFilmLike().remove(userId);
    }

    private long getNextId() {
        return ++globalId;
    }
}