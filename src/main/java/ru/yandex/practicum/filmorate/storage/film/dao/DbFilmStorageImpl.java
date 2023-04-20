package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;


@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbFilmStorageImpl implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage dbMpaStorage;
    private final GenreStorage dbGenreStorage;

    @Override
    public Film getFilmById(long filmId) {

        checkFilmById(filmId);

        final String sql =
                "SELECT * " +
                        "FROM public.films " +
                        "WHERE id = ?";

        return jdbcTemplate.queryForObject(sql,
                this::makeFilm,
                filmId);
    }

    @Override
    public Collection<Film> getAllFilm() {

        final String sql =
                "SELECT * " +
                        "FROM public.films";

        return jdbcTemplate.query(sql,
                this::makeFilm);
    }

    @Override
    public Collection<Film> getFilmByPopular(int count) {

        final String sql =
                "SELECT f.id, f.name, f.description, f.duration, f.release_date, f.rate, f.mpa " +
                        "FROM public.films AS f " +
                        "LEFT JOIN public.\"LIKE\" AS l ON f.id = l.film_id " +
                        "GROUP BY f.id " +
                        "ORDER BY COUNT(l.film_id) DESC " +
                        "LIMIT ?";

        return jdbcTemplate.query(sql,
                this::makeFilm,
                count);
    }

    @Override
    public void removeFilmById(long filmId) {

        checkFilmById(filmId);

        jdbcTemplate.update(
                "DELETE FROM public.films " +
                        "WHERE id = ?",
                filmId);
    }

    @Override
    public void removeAllFilm() {

        jdbcTemplate.update(
                "DELETE FROM public.\"LIKE\"");

        jdbcTemplate.update(
                "DELETE FROM public.genre");

        jdbcTemplate.update(
                "DELETE FROM public.films");
        jdbcTemplate.update(
                "ALTER TABLE public.films " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 1");

        jdbcTemplate.update(
                "DELETE FROM public.genres " +
                        "WHERE (id > 6)");
        jdbcTemplate.update(
                "ALTER TABLE public.genres " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 7");
    }

    @Override
    public void checkFilmById(long filmId) throws NotFoundException {

        final String sql =
                "SELECT id " +
                        "FROM public.films " +
                        "WHERE id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                filmId);

        if (!rows.next()) {
            throw new NotFoundException("Такой фильм с id => " + filmId + " не существует");
        }
    }

    @Override
    public void checkFilmByNameReleaseDateDuration(Film film) throws ConflictException {

        final boolean newFilm = (film.getId() == null);

        String sql;
        Object[] params;

        if (newFilm) {
            sql = "SELECT id " +
                    "FROM public.films " +
                    "WHERE name = ? " +
                    "AND duration = ? " +
                    "AND release_date = ?";
            params = new Object[]{film.getName(), film.getDuration(), film.getReleaseDate()};
        } else {
            sql = "SELECT id " +
                    "FROM public.films " +
                    "WHERE name = ? " +
                    "AND duration = ? " +
                    "AND release_date = ? " +
                    "AND id <> ?";
            params = new Object[]{film.getName(), film.getDuration(), film.getReleaseDate(), film.getId()};
        }

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                params);

        if (rows.next()) {
            throw new ConflictException("Такой фильм с именем => " + film.getName() + " уже существует по id => " + rows.getLong("id"));
        }
    }

    @Override
    public void checkFilmLikeByUserId(long filmId, long userId, boolean addOrRemove) throws ConflictException, NotFoundException {

        final String sql =
                "SELECT user_id " +
                        "FROM public.\"LIKE\" " +
                        "WHERE user_id = ?" +
                        "AND film_id =?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                userId, filmId);

        if (addOrRemove) {
            if (rows.next()) {
                throw new ConflictException("У фильма с id => " + filmId
                        + " уже существует лайк пользователя с id => " + userId);
            }
        } else {
            if (!rows.next()) {
                throw new NotFoundException("У фильма с id => " + filmId
                        + " не существует лайка пользователя с id => " + userId);
            }
        }
    }

    @Override
    public void addUserLikeOnFilm(long filmId, long userId) {

        final String sql =
                "INSERT INTO public.\"LIKE\" " +
                        "(user_id, film_id)" +
                        " VALUES (?, ?)";

        jdbcTemplate.update(sql,
                userId, filmId);
    }

    @Override
    public void removeUserLikeOnFilm(long filmId, long userId) {

        final String sql =
                "DELETE FROM public.\"LIKE\" " +
                        "WHERE user_id = ? " +
                        "AND film_id = ?";

        jdbcTemplate.update(sql,
                userId, filmId);
    }

    private Film makeFilm(ResultSet resultSet, int rowNumber) throws SQLException {
        final String sqlQueryGetMpaById =
                "SELECT * " +
                        "FROM public.mpa " +
                        "WHERE id = ?";

        Mpa mpa;
        try {
            mpa = jdbcTemplate.queryForObject(sqlQueryGetMpaById,
                    dbMpaStorage::makeMpa,
                    resultSet.getInt("mpa"));
        } catch (IncorrectResultSizeDataAccessException e) {
            mpa = null;
        }

        final String sqlGenresByFilmId =
                "SELECT id, name " +
                        "FROM public.genres " +
                        "JOIN public.genre on public.genres.id = public.genre.genre_id " +
                        "WHERE public.genre.film_id = ? " +
                        "ORDER BY id";

        final String sqlLikesByFilmId =
                "SELECT user_id " +
                        "FROM public.\"LIKE\" " +
                        "WHERE film_id = ?";


        final Film filmBuilder = Film
                .builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .rate(resultSet.getFloat("rate"))
                .mpa(mpa)
                .build();

        filmBuilder
                .getGenres()
                .addAll(
                        jdbcTemplate.query(sqlGenresByFilmId,
                                dbGenreStorage::makeGenre,
                                filmBuilder.getId()));

        filmBuilder
                .getLikes()
                .addAll(
                        jdbcTemplate.query(sqlLikesByFilmId,
                                (rs, rowNum) -> rs.getLong("user_id"),
                                filmBuilder.getId()));

        return filmBuilder;
    }
}