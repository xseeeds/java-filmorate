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
    private final GenreStorage dbGenreStorage;
    private final GenreStorage.OnCreate dbGenreStorageOnCreate;
    private final GenreStorage.OnUpdate dbGenreStorageOnUpdate;
    private final FilmStorage dbFilmStorageImpl;

    public List<Genre> getGenres() {

        List<Genre> allGenres = dbGenreStorage.getGenreList();

        log.info("Жанры получены (кол-во) => {}", allGenres.size());

        return allGenres;
    }

    public Genre getGenreById(@Positive int id) throws NotFoundException {

        final Genre genre = dbGenreStorage.getGenreById(id);

        log.info("Жанр получен по id => {}", id);

        return genre;
    }

    @Validated({GenreStorage.OnCreate.class, GenreStorage.class})
    public Genre addGenre(@Valid Genre genre) throws NotFoundException {

        dbGenreStorage.checkGenre(genre);

        Genre result = dbGenreStorageOnCreate.createGenre(genre);

        log.info("Жанр добавлен => {}", genre);

        return result;

    }

    @Validated({GenreStorage.OnUpdate.class, GenreStorage.class})
    public Genre updateGenre(@Valid Genre genre) throws NotFoundException {

        dbGenreStorage.checkGenreById(genre.getId());

        dbGenreStorage.checkGenre(genre);

        log.info("Жанр обновлен => {}", genre);

        return dbGenreStorageOnUpdate.updateGenre(genre);

    }

    public void addGenreOnFilm(@Positive int genreId, @Positive long filmId) {

        dbGenreStorage.checkGenreById(genreId);

        dbFilmStorageImpl.checkFilmById(filmId);

        dbGenreStorage.checkGenreOnFilm(genreId, filmId, true);

        log.info("Фильму c id => {} добавлен жанр c id => {}", filmId, genreId);

        dbGenreStorage.addGenreOnFilm(genreId, filmId);
    }

    public void removeGenreOnFilm(@Positive int genreId, @Positive long filmId) {

        dbGenreStorage.checkGenreById(genreId);

        dbFilmStorageImpl.checkFilmById(filmId);

        dbGenreStorage.checkGenreOnFilm(genreId, filmId, false);

        log.info("У фильму c id => {} удален жанр c id => {}", filmId, genreId);

        dbGenreStorage.removeGenreOnFilm(genreId, filmId);
    }

    public String removeAllGenre() {

        dbGenreStorage.removeAllGenre();

        log.info("Все жанры удалены, id сброшен");

        return "Все жанры удалены. id сброшен";
    }

    public String removeGenreById(@Positive int genreId) throws NotFoundException {

        dbGenreStorage.checkGenreById(genreId);

        dbGenreStorage.removeGenreById(genreId);

        log.info("Фильм c id => {} удален", genreId);

        return "Жанр c id => " + genreId + "удален";
    }
}