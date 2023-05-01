package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("directorStorage")
@RequiredArgsConstructor
public class DbDirectorStorageImpl implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAllDirector() {

        final String sql =
                "SELECT id, name " +
                        "FROM directors";

        return jdbcTemplate.query(sql,
                this::makeDirector);
    }

    @Override
    public Director getDirectorById(long directorId) throws NotFoundException {

        final String sql =
                "SELECT id, name " +
                        "FROM directors " +
                        "WHERE id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                directorId);

        if (rows.next()) {
            return Director
                    .builder()
                    .id(rows.getLong("id"))
                    .name(rows.getString("name"))
                    .build();
        } else {
            throw new NotFoundException("Режиссер по id => " + directorId + " не существует");
        }
    }

    @Override
    public void addDirectorOnFilm(long directorId, long filmId) {

        final String sql =
                "INSERT INTO film_director " +
                        "(film_id, director_id) " +
                        "VALUES (?, ?)";

        jdbcTemplate.update(sql,
                directorId, filmId);
    }

    @Override
    public void removeDirectorOnFilm(long directorId, long filmId) {

        final String sql =
                "DELETE FROM film_director " +
                        "WHERE director_id = ? " +
                        "AND film_id = ?";

        jdbcTemplate.update(sql,
                directorId, filmId);
    }

    @Override
    public void checkDirectorById(long directorId) throws NotFoundException {

        final String sql =
                "SELECT id " +
                        "FROM directors " +
                        "WHERE id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                directorId);

        if (!rows.next()) {
            throw new NotFoundException("Режиссер по id => " + directorId + " не существует");
        }
    }

    @Override
    public void checkDirectorByName(Director director) throws ConflictException {

        final boolean newDirector = (director.getId() == null);

        String sql;
        Object[] params;

        if (newDirector) {
            sql =
                    "SELECT id " +
                            "FROM directors " +
                            "WHERE name = ?";
            params = new Object[]{director.getName()};
        } else {
            sql =
                    "SELECT id " +
                            "FROM directors " +
                            "WHERE name = ? " +
                            "AND id <> ?";
            params = new Object[]{director.getName(), director.getId()};
        }

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, params);

        if (rows.next()) {
            throw new ConflictException("Режиссёр => " + director.getName() + " уже существует по id => " + rows.getString("id"));
        }
    }

    @Override
    public void checkDirectorOnFilm(long directorId, long filmId, boolean addOrRemove) throws NotFoundException, ConflictException {

        final String sql =
                "SELECT director_id " +
                        "FROM film_director " +
                        "WHERE director_id = ?" +
                        "AND film_id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                directorId, filmId);

        if (addOrRemove) {
            if (rows.next()) {
                throw new ConflictException("У фильма с id => " + filmId
                        + " уже существует режиссер с id => " + directorId);
            }
        } else {
            if (!rows.next()) {
                throw new NotFoundException("У фильма с id => " + filmId
                        + " не существует режиссера с id => " + directorId);
            }
        }
    }

    @Override
    public Director createDirector(Director director) {

        final SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", director.getName());

        long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        director.setId(id);

        return director;
    }

    @Override
    public Director updateDirector(Director director) {

        final String sqlQuery =
                "UPDATE directors SET " +
                        "name = ? " +
                        "WHERE id = ?";

        jdbcTemplate.update(sqlQuery,
                director.getName(), director.getId()
        );

        return director;
    }

    @Override
    public void removeAllDirector() {
        jdbcTemplate.update(
                "DELETE FROM directors");
    }

    @Override
    public void removeById(long directorId) throws NotFoundException {

        final String sql =
                "DELETE FROM directors " +
                        "WHERE id = ?";

        jdbcTemplate.update(sql, directorId);
    }

    public Director makeDirector(ResultSet rs, int rowNumber) throws SQLException {

        return Director.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}

