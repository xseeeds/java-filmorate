package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;
    private final FilmStorage filmStorage;

    public List<Director> getAllDirector() {
        return directorStorage.getAllDirector();
    }

    public Director getDirectorById(@Positive long directorId) throws NotFoundException {

        final Director director = directorStorage.getDirectorById(directorId);

        log.info("Режиссер получен  c id => {} =>>> {}", directorId, director);

        return director;
    }

    @Validated
    public Director create(@Valid Director director) throws ConflictException {

        directorStorage.checkDirectorByName(director);

        Director createdDirector = directorStorage.createDirector(director);

        log.info("Режиссер добавлен {}", createdDirector);

        return createdDirector;
    }

    @Validated
    public Director update(@Valid Director director) throws NotFoundException, ConflictException  {

        directorStorage.checkDirectorById(director.getId());

        directorStorage.checkDirectorByName(director);

        final Director updatedDirector = directorStorage.updateDirector(director);

        log.info("Режиссер обновлен {}", updatedDirector);

        return updatedDirector;
    }

    public void addDirectorOnFilm(@Positive long directorId, @Positive long filmId) {

        directorStorage.checkDirectorById(directorId);

        filmStorage.checkFilmById(filmId);

        directorStorage.checkDirectorOnFilm(directorId, filmId, true);

        log.info("Фильму c id => {} добавлен режиссер c id => {}", filmId, directorId);

        directorStorage.addDirectorOnFilm(directorId, filmId);
    }

    public void removeDirectorOnFilm(@Positive long directorId, @Positive long filmId) {

        directorStorage.checkDirectorById(directorId);

        filmStorage.checkFilmById(filmId);

        directorStorage.checkDirectorOnFilm(directorId, filmId, false);

        log.info("У фильма c id => {} удален режиссер c id => {}", filmId, directorId);

        directorStorage.removeDirectorOnFilm(directorId, filmId);
    }

    public String removeAllDirector() {

        directorStorage.removeAllDirector();

        log.info("Все режиссер удалены, id сброшен");

        return "Все режиссер удалены. id сброшен";
    }


    public String removeById(@Positive long directorId) throws NotFoundException {

        directorStorage.checkDirectorById(directorId);

        directorStorage.removeById(directorId);

        log.info("Режиссер удален по id => {}", directorId);

        return "Режиссер c id => " + directorId + "удален";
    }
}