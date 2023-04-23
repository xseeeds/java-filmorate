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
    private final GenreStorage dbGenreStorageImpl;

    private final FilmStorage dbFilmStorageImpl;


    public List<Genre> getGenres() {

        List<Genre> allGenres = dbGenreStorageImpl.getGenreList();

        log.info("Жанры получены (кол-во) => {}", allGenres.size());

        return allGenres;
    }

    public Genre getGenreById(@Positive int id) throws NotFoundException {

        final Genre genre = dbGenreStorageImpl.getGenreById(id);

        log.info("Жанр получен по id => {}", id);

        return genre;
    }

    @Validated
    public Genre createGenre(@Valid Genre genre) throws NotFoundException {

        dbGenreStorageImpl.checkGenre(genre);

        Genre result = dbGenreStorageImpl.createGenre(genre);

        log.info("Жанр добавлен => {}", genre);

        return result;

    }

    @Validated
    public Genre updateGenre(@Valid Genre genre) throws NotFoundException {

        dbGenreStorageImpl.checkGenreById(genre.getId());

        dbGenreStorageImpl.checkGenre(genre);

        log.info("Жанр обновлен => {}", genre);

        return dbGenreStorageImpl.updateGenre(genre);

    }

    public void addGenreOnFilm(@Positive int genreId, @Positive long filmId) {

        dbGenreStorageImpl.checkGenreById(genreId);

        dbFilmStorageImpl.checkFilmById(filmId);

        dbGenreStorageImpl.checkGenreOnFilm(genreId, filmId, true);

        log.info("Фильму c id => {} добавлен жанр c id => {}", filmId, genreId);

        dbGenreStorageImpl.addGenreOnFilm(genreId, filmId);
    }

    public void removeGenreOnFilm(@Positive int genreId, @Positive long filmId) {

        dbGenreStorageImpl.checkGenreById(genreId);

        dbFilmStorageImpl.checkFilmById(filmId);

        dbGenreStorageImpl.checkGenreOnFilm(genreId, filmId, false);

        log.info("У фильму c id => {} удален жанр c id => {}", filmId, genreId);

        dbGenreStorageImpl.removeGenreOnFilm(genreId, filmId);
    }

    public String removeAllGenre() {

        dbGenreStorageImpl.removeAllGenre();

        log.info("Все жанры удалены, id сброшен");

        return "Все жанры удалены. id сброшен";
    }

    public String removeGenreById(@Positive int genreId) throws NotFoundException {

        dbGenreStorageImpl.checkGenreById(genreId);

        dbGenreStorageImpl.removeGenreById(genreId);

        log.info("Фильм c id => {} удален", genreId);

        return "Жанр c id => " + genreId + "удален";
    }
}