package ru.yandex.practicum.filmorate.storage.film.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InMemoryFilmStorageImpl implements FilmStorage {
    private final TreeSet<Film> films = new TreeSet<>(Comparator
            .comparing(Film::getRate)
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
    public Film getFilmById(long filmId) throws NotFoundException {
        return films
                .stream()
                .filter(f -> f.getId() == filmId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Такой фильм с id => " + filmId + " не существует"));
    }

    @Override
    public List<Film> getAllFilm() {
        return new ArrayList<>(films);
    }

    @Override
    public List<Film> getFilmByPopular(int count, String genre, Integer year) {

        return films
                .stream()
                .filter(film -> {
                    if (genre != null && year != null) {

                        final Optional<Genre> filmGenre = film.getGenres()
                                .stream()
                                .filter(fG -> fG.getName().equalsIgnoreCase(genre))
                                .findFirst();

                        return film.getReleaseDate().getYear() == year &&  filmGenre.isPresent();

                    } else if (genre != null) {

                        final Optional<Genre> filmGenre = film.getGenres()
                                .stream()
                                .filter(fG -> fG.getName().equalsIgnoreCase(genre))
                                .findFirst();

                        return filmGenre.isPresent();

                    } else if (year != null) {

                        return film.getReleaseDate().getYear() == year;
                    }
                    return false;
                })
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
                        new NotFoundException("Такой фильм с id =>" + filmId + " не существует")));
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
            throw new NotFoundException("Такой фильм с id => " + filmId + " не существует");
        }
    }

    @Override
    public void checkFilmByNameReleaseDate(Film film) throws ConflictException {

        final Film ceil = films.ceiling(film);
        final Film floor = films.floor(film);

        if (Objects.equals(ceil, floor) && ceil != null) {
            throw new ConflictException("Такой фильм с именем => " + film.getName() + " и датой релиза => " + film.getReleaseDate()
                    + " уже существует по id => " + ceil.getId());
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
    public void addUserMarkOnFilm(long filmId, long userId, int mark) {

        if (mark > 0) {
            getFilmById(filmId).getUserFilmLike().put(userId, mark);
        } else {
            getFilmById(filmId).getUserFilmLike().put(userId, null);
        }
        setRateFilmByMarks(filmId);
    }

    @Override
    public void removeUserMarkOnFilm(long filmId, long userId, int mark) {
        getFilmById(filmId).getUserFilmLike().remove(userId);
        setRateFilmByMarks(filmId);
    }

    @Override
    public List<Film> getFilmsByDirector(long directorId, String sortBy) throws ConflictException {

        final List<Film> filmList = new ArrayList<>();

        if (sortBy.equalsIgnoreCase("year")) {

            final Set<Film> filmSetByReleaseDate = new TreeSet<>(Comparator.comparing(Film::getReleaseDate));

            filmSetByReleaseDate.addAll(films
                    .stream()
                    .filter(
                            film -> {
                                final Optional<Director> filmDirector = film.getDirectors()
                                        .stream()
                                        .filter(fD -> fD.getId() == directorId)
                                        .findFirst();

                                return filmDirector.isPresent();
                            })
                    .collect(
                            toSet()));

            filmList.addAll(filmSetByReleaseDate);

        } else if (sortBy.equalsIgnoreCase("likes")) {

            filmList.addAll(films
                    .stream()
                    .filter(
                            film -> {
                                final Optional<Director> filmDirector = film.getDirectors()
                                        .stream()
                                        .filter(fD -> fD.getId() == directorId)
                                        .findFirst();

                                return filmDirector.isPresent();
                            })
                    .collect(
                            toSet()));
        } else {
            throw new NotFoundException("Тип сортирорки " + sortBy + " не существует!");
        }

        return filmList;
    }

    @Override
    public List<Film> getCommonFilms(long userId, long otherId) {

        return films
                .stream()
                .filter(film -> film.getUserFilmLike().containsKey(userId)
                        && film.getUserFilmLike().containsKey(otherId))
                .collect(toList());
    }

    //TODO можно попрактивоться

    @Override
    public List<Film> getFilmsBySearch(String query, String by) {

        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Метод /getFilmsBySearch не реализован.");

/*
        final List<Film> filmsSearch;

        if (by.equalsIgnoreCase("director")) {

            films = filmStorage.findByDirector(queryAddSymbols);

        } else if (by.equalsIgnoreCase("title")) {

            films = filmStorage.findByName(queryAddSymbols);

        } else if (by.equalsIgnoreCase("director,title") || by.equalsIgnoreCase("title,director")) {

            films = filmStorage.findByDirectorAndName(queryAddSymbols);

        } else throw new NotfoundException("Поиск по параметру " + by + " не предусмотрен");

*/

    }

    @Override
    public Film makeFilm(ResultSet resultSet, int rowNumber) throws SQLException {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Метод /makeFilm не реализован.");
    }

    private void setRateFilmByMarks(long filmId) {

        final Film film = getFilmById(filmId);

        if (!film.getUserFilmLike().isEmpty()) {

            AtomicReference<Float> filmRate = new AtomicReference<>((float) 0);
            AtomicInteger count = new AtomicInteger();

            film.getUserFilmLike()
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(
                            mark -> {
                filmRate.updateAndGet(v -> v + mark);
                count.getAndIncrement();
            });

            film.setRate(filmRate.get() / count.get());
            log.info("Рейтинг фильма с id => {} обновлен rate => {}", filmId, filmRate);

        } else {

            log.info("Рейтинг фильма с id => {} не обновлен, нет оценок пользователей к фильму", filmId);
        }
    }

    private long getNextId() {
        return ++globalId;
    }
}