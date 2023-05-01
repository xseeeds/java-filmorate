package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.ConflictException;
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


    public List<Genre> getAllGenre() {

        final List<Genre> allGenres = genreStorage.getAllGenre();

        log.info("Жанры получены (кол-во) => {}", allGenres.size());

        return allGenres;
    }

    public Genre getGenreById(@Positive long id) throws NotFoundException {

        final Genre genre = genreStorage.getGenreById(id);

        log.info("Жанр получен c id => {} =>>> {}", id, genre);

        return genre;
    }

    @Validated
    public Genre createGenre(@Valid Genre genre) throws ConflictException {

        genreStorage.checkGenreByName(genre);

        final Genre createdGenre = genreStorage.createGenre(genre);

        log.info("Жанр добавлен => {}", createdGenre);

        return createdGenre;

    }

    @Validated
    public Genre updateGenre(@Valid Genre genre) throws NotFoundException, ConflictException {

        genreStorage.checkGenreById(genre.getId());

        genreStorage.checkGenreByName(genre);

        final Genre updatedGenre = genreStorage.updateGenre(genre);

        log.info("Жанр обновлен => {}", updatedGenre);

        return updatedGenre;

    }

    public void addGenreOnFilm(@Positive long genreId, @Positive long filmId) {

        genreStorage.checkGenreById(genreId);

        filmStorage.checkFilmById(filmId);

        genreStorage.checkGenreOnFilm(genreId, filmId, true);

        log.info("Фильму c id => {} добавлен жанр c id => {}", filmId, genreId);

        genreStorage.addGenreOnFilm(genreId, filmId);
    }

    public void removeGenreOnFilm(@Positive long genreId, @Positive long filmId) {

        genreStorage.checkGenreById(genreId);

        filmStorage.checkFilmById(filmId);

        genreStorage.checkGenreOnFilm(genreId, filmId, false);

        log.info("У фильма c id => {} удален жанр c id => {}", filmId, genreId);

        genreStorage.removeGenreOnFilm(genreId, filmId);
    }

    public String removeAllGenre() {

        genreStorage.removeAllGenre();

        log.info("Все жанры удалены, id сброшен");

        return "Все жанры удалены. id сброшен";
    }

    public String removeGenreById(@Positive long genreId) throws NotFoundException {

        genreStorage.removeGenreById(genreId);

        log.info("Фильм c id => {} удален", genreId);

        return "Жанр c id => " + genreId + "удален";
    }
}