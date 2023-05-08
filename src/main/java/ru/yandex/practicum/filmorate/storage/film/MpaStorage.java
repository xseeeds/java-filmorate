package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface MpaStorage {

    List<Mpa> getMpaList();

    Mpa getMpaById(long id) throws NotFoundException;

    Mpa makeMpa(ResultSet resultSet, int rowNum) throws SQLException;

}