package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Repository("filmStorage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbFilmStorageImpl implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage dbMpaStorageImpl;
    private final GenreStorage dbGenreStorageImpl;

    @Override
    public Film createFilm(Film film) {

        final SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        final Map<String, Object> filmToMap = new HashMap<>();
        filmToMap.put("name", film.getName());
        filmToMap.put("description", film.getDescription());
        filmToMap.put("release_date", film.getReleaseDate());
        filmToMap.put("duration", film.getDuration());

        final long id = simpleJdbcInsert.executeAndReturnKey(filmToMap).longValue();
        film.setId(id);

        if (film.getMpa() != null) {

            jdbcTemplate.update(
                    "INSERT INTO film_mpa " +
                            "(film_id, mpa_id) " +
                            "VALUES (?, ?)",
                    film.getId(), film.getMpa().getId());

            final String sqlQueryMpaByFilmId =
                    "SELECT id, name " +
                            "FROM mpas " +
                            "JOIN film_mpa on mpas.id = film_mpa.mpa_id " +
                            "AND film_mpa.film_id = ? " +
                            "ORDER BY id";

            film.setMpa(
                    jdbcTemplate.queryForObject(sqlQueryMpaByFilmId,
                            dbMpaStorageImpl::makeMpa,
                            film.getId()));
        }

        if (!film.getGenres().isEmpty()) {

            updateAllGenresByFilmId(film.getId(), film.getGenres());

            film.getGenres().clear();

            final String sqlGenresByFilmId =
                    "SELECT id, name " +
                            "FROM genres " +
                            "JOIN film_genre on genres.id = film_genre.genre_id " +
                            "AND film_genre.film_id = ?" +
                            "ORDER BY id";

            film.getGenres()
                    .addAll(
                            jdbcTemplate.query(sqlGenresByFilmId,
                                    dbGenreStorageImpl::makeGenre,
                                    film.getId()));
        }

        return film;
    }

    @Override
    public Film updateFilm(Film film) {

        final String sqlUpdateFilm =
                "UPDATE films " +
                        "SET name = ?, description = ?, duration = ?, release_date = ? " +
                        "WHERE id = ?";

        jdbcTemplate.update(sqlUpdateFilm,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getId());

        if (film.getMpa() != null) {

            jdbcTemplate.update(
                    "DELETE FROM film_mpa " +
                            "WHERE film_id = ?",
                    film.getId());

            jdbcTemplate.update(
                    "INSERT INTO film_mpa " +
                            "(film_id, mpa_id) " +
                            "VALUES (?, ?)",
                    film.getId(), film.getMpa().getId());

            final String sqlQueryMpaByFilmId =
                    "SELECT id, name " +
                            "FROM mpas " +
                            "JOIN film_mpa on mpas.id = film_mpa.mpa_id " +
                            "AND film_mpa.film_id = ? " +
                            "ORDER BY id";

            film
                    .setMpa(
                            jdbcTemplate.queryForObject(sqlQueryMpaByFilmId,
                                    dbMpaStorageImpl::makeMpa,
                                    film.getId()));
        }

        //TODO убрать под if обновление фильма без жанров не должно удалить все имеющиеся жанры
        jdbcTemplate.update(
                "DELETE FROM film_genre " +
                        "WHERE film_id = ?",
                film.getId());

        if (!film.getGenres().isEmpty()) {

            updateAllGenresByFilmId(film.getId(), film.getGenres());

            film.getGenres().clear();

            final String sqlGenresByFilmId =
                    "SELECT id, name " +
                            "FROM genres " +
                            "JOIN film_genre on genres.id = film_genre.genre_id " +
                            "AND film_genre.film_id = ? " +
                            "ORDER BY id";

            film.getGenres()
                    .addAll(
                            jdbcTemplate.query(sqlGenresByFilmId,
                                    dbGenreStorageImpl::makeGenre,
                                    film.getId()));
        }



        if (!film.getUserFilmLike().isEmpty()) {

            jdbcTemplate.update(
                    "DELETE FROM user_film_like " +
                            "WHERE film_id = ?",
                    film.getId());

            film.getUserFilmLike().forEach((userId, mark) -> jdbcTemplate.update(
                    "INSERT INTO user_film_like " +
                            "(user_id, film_id, mark) " +
                            "VALUES (?, ?, ?)",
                    userId, film.getId(), mark));
        }


        return film;
    }

    @Override
    public void resetGlobalId() {
        jdbcTemplate.update(
                "ALTER TABLE films " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 1");
    }

    @Override
    public Film getFilmById(long filmId) {

        checkFilmById(filmId);

        final String sql =
                "SELECT * " +
                        "FROM films " +
                        "WHERE id = ?";

        return jdbcTemplate.queryForObject(sql,
                this::makeFilm,
                filmId);
    }

    @Override
    public List<Film> getAllFilm() {

        final String sql =
                "SELECT * " +
                        "FROM films";

        return jdbcTemplate.query(sql,
                this::makeFilm);
    }

    @Override
    public List<Film> getFilmByPopular(int count) {

        final String sql =
                "SELECT f.id, f.name, f.description, f.duration, f.release_date, f.rate " +
                        "FROM films AS f " +
                        "LEFT JOIN user_film_like AS ufl ON f.id = ufl.film_id " +
                        "GROUP BY f.id " +
                        "ORDER BY count(ufl.film_id) DESC " +
                        "LIMIT ?";

        return jdbcTemplate.query(sql,
                this::makeFilm,
                count);
    }

    @Override
    public void removeFilmById(long filmId) {

        checkFilmById(filmId);

        jdbcTemplate.update(
                "DELETE FROM films " +
                        "WHERE id = ?",
                filmId);
    }

    @Override
    public void removeAllFilm() {

        jdbcTemplate.update(
                "DELETE FROM user_film_like");

        jdbcTemplate.update(
                "DELETE FROM film_genre");

        jdbcTemplate.update(
                "DELETE FROM film_mpa");

        jdbcTemplate.update(
                "DELETE FROM films");
        jdbcTemplate.update(
                "ALTER TABLE films " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 1");

        jdbcTemplate.update(
                "DELETE FROM genres " +
                        "WHERE (id > 6)");
        jdbcTemplate.update(
                "ALTER TABLE genres " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 7");
    }

    @Override
    public void checkFilmById(long filmId) throws NotFoundException {

        final String sql =
                "SELECT id " +
                        "FROM films " +
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
                    "FROM films " +
                    "WHERE name = ? " +
                    "AND duration = ? " +
                    "AND release_date = ?";
            params = new Object[]{film.getName(), film.getDuration(), film.getReleaseDate()};
        } else {
            sql = "SELECT id " +
                    "FROM films " +
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
                        "FROM user_film_like " +
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
    public void addUserLikeOnFilm(long filmId, long userId, int mark) {

        if (mark > 0) {

            final String sql =
                    "INSERT INTO user_film_like " +
                            "(user_id, film_id, mark) " +
                            "VALUES (?, ?, ?)";

            jdbcTemplate.update(sql,
                    userId, filmId, mark);
        } else {

            final String sql =
                    "INSERT INTO user_film_like " +
                            "(user_id, film_id) " +
                            "VALUES (?, ?)";

            jdbcTemplate.update(sql,
                    userId, filmId);
        }
    }

    @Override
    public void removeUserLikeOnFilm(long filmId, long userId, int mark) {

        if (mark > 0) {

            final String sql =
                    "DELETE FROM user_film_like " +
                            "WHERE user_id = ? " +
                            "AND film_id = ? " +
                            "AND mark = ?";

            jdbcTemplate.update(sql,
                    userId, filmId, mark);
        } else {

            final String sql =
                    "DELETE FROM user_film_like " +
                            "WHERE user_id = ? " +
                            "AND film_id = ?";

            jdbcTemplate.update(sql,
                    userId, filmId);
        }
    }

    private Film makeFilm(ResultSet resultSet, int rowNumber) throws SQLException {

        final String sqlQueryMpaByFilmId =
                "SELECT id, name " +
                        "FROM mpas " +
                        "JOIN film_mpa on mpas.id = film_mpa.mpa_id " +
                        "AND film_mpa.film_id = ? " +
                        "ORDER BY id";

        final String sqlGenresByFilmId =
                "SELECT id, name " +
                        "FROM genres " +
                        "JOIN film_genre on genres.id = film_genre.genre_id " +
                        "AND film_genre.film_id = ? " +
                        "ORDER BY id";

        final String sqlLikesByFilmId =
                "SELECT user_id, mark " +
                        "FROM user_film_like " +
                        "WHERE film_id = ?";


        final Film filmBuilder = Film
                .builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .rate(resultSet.getFloat("rate"))
                .build();

        try {
            filmBuilder
                    .setMpa(
                            jdbcTemplate.queryForObject(sqlQueryMpaByFilmId,
                                    dbMpaStorageImpl::makeMpa,
                                    filmBuilder.getId()));

        } catch (EmptyResultDataAccessException e) {
            filmBuilder.setMpa(null);
        }

        filmBuilder
                .getGenres()
                .addAll(
                        jdbcTemplate.query(sqlGenresByFilmId,
                                dbGenreStorageImpl::makeGenre,
                                filmBuilder.getId()));

        filmBuilder
                .getUserFilmLike()
                .putAll(
                        (Map<? extends Long, ? extends Integer>) jdbcTemplate.query(sqlLikesByFilmId,
                                        this::makeMark,
                                        filmBuilder.getId()
                                )
                                .stream()
                                .flatMap(
                                        map -> map.entrySet()
                                                .stream()
                                )
                                .collect(
                                        Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (oldValue, newValue) -> newValue,
                                                HashMap::new
                                        )
                                )
                );

        return filmBuilder;
    }

    private Map<Long, Integer> makeMark(ResultSet rs, int rowNum) throws SQLException {
        return Map.of(rs.getLong("user_id"), rs.getInt("mark"));
    }

    private void updateAllGenresByFilmId(long filmId, List<Genre> allGenresOnFilm) {

        jdbcTemplate.batchUpdate("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, allGenresOnFilm.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return allGenresOnFilm.size();
            }
        });
    }
}

