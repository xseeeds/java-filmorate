package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbGenreStorageImpl implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Genre createGenre(Genre genre) {

        final Map<String, Object> genreFields = new HashMap<>();
        genreFields.put("name", genre.getName());

        final SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("genres")
                .usingGeneratedKeyColumns("id");

        final int genreId =  simpleJdbcInsert.executeAndReturnKey(
                genreFields).intValue();

        genre.setId(genreId);

        return genre;
    }

    @Override
    public Genre updateGenre(Genre genre) {

        final String sqlQuery =
                "UPDATE genres " +
                        "SET name = ? " +
                        "WHERE id = ?";

        jdbcTemplate.update(sqlQuery,
                genre.getName(), genre.getId());

        return genre;
    }

    @Override
    public void addGenreOnFilm(int genreId, long filmId) {

        final String sql =
                "INSERT INTO film_genre " +
                        "(film_id, genre_id) " +
                        "VALUES (?, ?)";

        jdbcTemplate.update(sql,
                genreId, filmId);
    }

    @Override
    public void removeGenreOnFilm(int genreId, long filmId) {

        final String sql =
                "DELETE FROM film_genre " +
                        "WHERE genre_id = ? " +
                        "AND film_id = ?";

        jdbcTemplate.update(sql,
                genreId, filmId);
    }

    @Override
    public void checkGenreOnFilm(int genreId, long filmId, boolean addOrRemove) throws NotFoundException, ConflictException {

        final String sql =
                "SELECT genre_id " +
                        "FROM film_genre " +
                        "WHERE genre_id = ?" +
                        "AND film_id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                genreId, filmId);

        if (addOrRemove) {
            if (rows.next()) {
                throw new ConflictException("У фильма с id => " + filmId
                        + " уже существует жанр с id => " + genreId);
            }
        } else {
            if (!rows.next()) {
                throw new NotFoundException("У фильма с id => " + filmId
                        + " не существует жанр с id => " + genreId);
            }
        }
    }

    @Override
    public List<Genre> getGenreList() {

        final String sql =
                "SELECT * " +
                        "FROM genres";

        return jdbcTemplate.query(sql,
                this::makeGenre);

    }

    @Override
    public Genre getGenreById(int id) throws NotFoundException {

        final String sql =
                "SELECT name " +
                        "FROM genres " +
                        "WHERE id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                id);

        if (rows.next()) {
            return Genre
                    .builder()
                    .id(id)
                    .name(rows.getString("name"))
                    .build();
        } else {
            throw new NotFoundException("Жанр по id => " + id + " не существует");
        }
    }

    @Override
    public void checkGenreById(int id) throws NotFoundException {

        final String sql =
                "SELECT id " +
                        "FROM genres " +
                        "WHERE id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                id);

        if (!rows.next()) {
            throw new NotFoundException("Жанр по id => " + id + " не существует");
        }
    }

    @Override
    public void checkGenre(Genre genre) throws NotFoundException {

        final boolean newGenre = (genre.getId() == null);

        String sql;
        Object[] params;

        if (newGenre) {
            sql =
                    "SELECT id " +
                            "FROM genres " +
                            "WHERE name = ?";
            params = new Object[]{genre.getName()};
        } else {
            sql =
                    "SELECT id " +
                            "FROM genres " +
                            "WHERE name = ? " +
                            "AND id <> ?";
            params = new Object[]{genre.getName(), genre.getId()};
        }

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, params);

        if (rows.next()) {
            throw new NotFoundException("Жанр => " + genre.getName() + " уже существует по id => " + rows.getString("id"));
        }

    }

    @Override
    public void removeGenreById(int id) {
        jdbcTemplate.update(
                "DELETE FROM genres " +
                "WHERE id = ?",
                id);
    }

    @Override
    public void removeAllGenre() {
        jdbcTemplate.update(
                "DELETE FROM genres");
    }

    @Override
    public Genre makeGenre(ResultSet resultSet, int rowNum) throws SQLException {

        return Genre
                .builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();

    }

}