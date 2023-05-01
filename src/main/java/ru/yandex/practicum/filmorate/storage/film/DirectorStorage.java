package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DirectorStorage {
    List<Director> getAllDirector();

    Director getDirectorById(long directorId) throws NotFoundException;

    void addDirectorOnFilm(long directorId, long filmId);

    void removeDirectorOnFilm(long directorId, long filmId);

    void checkDirectorById(long directorId) throws NotFoundException;

    void checkDirectorByName(Director director) throws ConflictException;

    void checkDirectorOnFilm(long directorId, long filmId, boolean addOrRemove) throws NotFoundException, ConflictException;

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void removeById(long directorId) throws NotFoundException;

    void removeAllDirector();

    Director makeDirector(ResultSet resultSet, int rowNumber) throws SQLException;
}