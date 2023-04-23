package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreService {
    private final GenreStorage genreStorage;
    private final FilmStorage filmStorage;


    public List<Genre> getGenres() {

        List<Genre> allGenres = genreStorage.getGenreList();

        log.info("Жанры получены (кол-во) => {}", allGenres.size());

        return allGenres;
    }

    public Genre getGenreById(@Positive int id) throws NotFoundException {

        final Genre genre = genreStorage.getGenreById(id);

        log.info("Жанр получен по id => {}", id);

        return genre;
    }

    @Validated
    public Genre createGenre(@Valid Genre genre) throws NotFoundException {

        genreStorage.checkGenre(genre);

        Genre result = genreStorage.createGenre(genre);

        log.info("Жанр добавлен => {}", genre);

        return result;

    }

    @Validated
    public Genre updateGenre(@Valid Genre genre) throws NotFoundException {

        genreStorage.checkGenreById(genre.getId());

        genreStorage.checkGenre(genre);

        log.info("Жанр обновлен => {}", genre);

        return genreStorage.updateGenre(genre);

    }

    public void addGenreOnFilm(@Positive int genreId, @Positive long filmId) {

        genreStorage.checkGenreById(genreId);

        filmStorage.checkFilmById(filmId);

        genreStorage.checkGenreOnFilm(genreId, filmId, true);

        log.info("Фильму c id => {} добавлен жанр c id => {}", filmId, genreId);

        genreStorage.addGenreOnFilm(genreId, filmId);
    }

    public void removeGenreOnFilm(@Positive int genreId, @Positive long filmId) {

        genreStorage.checkGenreById(genreId);

        filmStorage.checkFilmById(filmId);

        genreStorage.checkGenreOnFilm(genreId, filmId, false);

        log.info("У фильму c id => {} удален жанр c id => {}", filmId, genreId);

        genreStorage.removeGenreOnFilm(genreId, filmId);
    }

    public String removeAllGenre() {

        genreStorage.removeAllGenre();

        log.info("Все жанры удалены, id сброшен");

        return "Все жанры удалены. id сброшен";
    }

    public String removeGenreById(@Positive int genreId) throws NotFoundException {

        genreStorage.checkGenreById(genreId);

        genreStorage.removeGenreById(genreId);

        log.info("Фильм c id => {} удален", genreId);

        return "Жанр c id => " + genreId + "удален";
    }
}