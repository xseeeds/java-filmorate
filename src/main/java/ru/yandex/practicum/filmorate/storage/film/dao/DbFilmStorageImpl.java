package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
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
import ru.yandex.practicum.filmorate.storage.film.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Repository("filmStorage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbFilmStorageImpl implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    public final DirectorStorage directorStorage;

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

        updateMpaByFilm(film);

        updateAllDirectorByFilm(film);

        updateAllGenresByFilmId(film);

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

        updateMpaByFilm(film);

        updateAllDirectorByFilm(film);

        updateAllGenresByFilmId(film);

        updateAllUserLikeByFilmId(film);

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
    public Film getFilmById(long filmId) throws NotFoundException {

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
    public void checkFilmByNameReleaseDate(Film film) throws ConflictException {

        final boolean newFilm = (film.getId() == null);

        String sql;
        Object[] params;

        if (newFilm) {
            sql = "SELECT id " +
                    "FROM films " +
                    "WHERE name = ? " +
                    "AND release_date = ?";
            params = new Object[]{film.getName(), film.getReleaseDate()};
        } else {
            sql = "SELECT id " +
                    "FROM films " +
                    "WHERE name = ? " +
                    "AND release_date = ? " +
                    "AND id <> ?";
            params = new Object[]{film.getName(), film.getReleaseDate(), film.getId()};
        }

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                params);

        if (rows.next()) {
            throw new ConflictException("Такой фильм с именем => " + film.getName() + " и датой релиза => " + film.getReleaseDate()
                    + " уже существует по id => " + rows.getLong("id"));
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

    @Override
    public List<Film> getFilmsByDirector(long directorId, String sortBy) throws NotFoundException {

        String sqlQuery;

        if (sortBy.equalsIgnoreCase("year")) {

            sqlQuery =
                    "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rate " +
                            "FROM films f " +
                            "JOIN film_director fd ON fd.film_id = f.id AND fd.director_id = ? " +
                            "ORDER BY f.release_date";

        } else if (sortBy.equalsIgnoreCase("likes")) {

            sqlQuery =
                    "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rate " +
                            "FROM films f " +
                            "JOIN film_director fd ON fd.film_id = f.id AND fd.director_id = ? " +
                            "LEFT JOIN user_film_like ufl ON f.id = ufl.film_id " +
                            "GROUP BY f.id " +
                            "ORDER BY COUNT(ufl.user_id) DESC";

        } else {
            throw new NotFoundException("Тип сортирорки " + sortBy + " не существует!");
        }

        return jdbcTemplate.query(sqlQuery, this::makeFilm, directorId);
    }

    @Override
    public Film makeFilm(ResultSet resultSet, int rowNumber) throws SQLException {

        final String sqlMpaByFilmId =
                "SELECT id, name " +
                        "FROM mpas " +
                        "JOIN film_mpa on mpas.id = film_mpa.mpa_id " +
                        "AND film_mpa.film_id = ? " +
                        "ORDER BY id";

        final String sqlDirectorsByFilmId =
                "SELECT d.id, d.name " +
                        "FROM film_director fd " +
                        "JOIN directors d ON d.id = fd.director_id " +
                        "AND fd.film_id = ?";

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
                            jdbcTemplate.queryForObject(sqlMpaByFilmId,
                                    mpaStorage::makeMpa,
                                    filmBuilder.getId()));

        } catch (EmptyResultDataAccessException e) {
            filmBuilder.setMpa(null);
        }

        filmBuilder
                .getDirectors()
                .addAll(
                        jdbcTemplate.query(sqlDirectorsByFilmId,
                                directorStorage::makeDirector,
                                filmBuilder.getId()));

        filmBuilder
                .getGenres()
                .addAll(
                        jdbcTemplate.query(sqlGenresByFilmId,
                                genreStorage::makeGenre,
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

    private void updateMpaByFilm(Film film) {

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
                                mpaStorage::makeMpa,
                                film.getId()));
    }

    private void updateAllGenresByFilmId(Film film) {

        /*for (int i = 0; i < film.getGenres().size(); i++) {
            try {
                genreStorage
                        .checkGenreOnFilm(
                                film.getGenres().get(i).getId(),
                                film.getId(),
                                true);

            } catch (ConflictException e) {
                film.getGenres().remove(i);
                log.info("Жанр удален при создании/обновлении фильма. " + e.getMessage());
                i--;
            }
        }*/

        jdbcTemplate.update(
                "DELETE FROM film_genre " +
                        "WHERE film_id = ?",
                film.getId());


        final String sqlQuery =
                "INSERT INTO film_genre " +
                        "(film_id, genre_id) " +
                        "VALUES (?, ?)";

        final List<Genre> allGenreByFilm = film.getGenres()
                .stream()
                .distinct()
                .collect(
                        Collectors.toList());

        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setLong(2, allGenreByFilm.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return allGenreByFilm.size();
            }
        });

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
                                genreStorage::makeGenre,
                                film.getId()));
    }

    private void updateAllUserLikeByFilmId(Film film) {

        jdbcTemplate.update(
                "DELETE FROM user_film_like " +
                        "WHERE film_id = ?",
                film.getId());

        //TODO преобразование id из long в int для batchUpdate

        List<Integer> userIds = new ArrayList<>();

        //TODO надо проверить поведение, если пользователь не поставил оценку.
        // Добавляется null или я теряю эту ячейку и потом mark сбиваеться по порядкому номеру с идентичным ему userId

        List<Integer> allMarks = new ArrayList<>();

        film.getUserFilmLike().forEach((userId, mark) -> {
            userIds.add(userId.intValue());
            allMarks.add(mark);
        });

        final String sqlQuery =
                "INSERT INTO user_film_like " +
                        "(user_id, film_id, mark) " +
                        "VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, userIds.get(i));
                ps.setLong(2, film.getId());
                ps.setInt(3, allMarks.get(i));
            }

            @Override
            public int getBatchSize() {
                return film.getUserFilmLike().size();
            }
        });
    }

    private void updateAllDirectorByFilm(Film film) {

        jdbcTemplate.update("DELETE FROM film_director " +
                        "WHERE film_id = ?",
                film.getId());

        //TODO преобразование id из long в int для batchUpdate

        final List<Integer> directorIds = film
                .getDirectors()
                .stream()
                .map(
                        director -> director.getId().intValue())
                .collect(
                        Collectors.toList());

        final String sqlQuery =
                "INSERT INTO film_director " +
                        "(film_id, director_id) " +
                        "VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sqlQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setLong(2, directorIds.get(i));
            }

            @Override
            public int getBatchSize() {
                return directorIds.size();
            }
        });
    }
}

