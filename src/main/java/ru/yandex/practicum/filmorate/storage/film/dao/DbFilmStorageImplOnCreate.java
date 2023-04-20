package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbFilmStorageImplOnCreate implements FilmStorage.OnCreate {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage dbMpaStorage;
    private final DbGenreStorage dbGenreStorage;

    @Override
    public Film createFilm(Film film) {

        if (film.getMpa() != null) {
            film.setMpa(dbMpaStorage.getMpaById(film.getMpa().getId()));
        }

        final SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        final Map<String, Object> filmToMap = new HashMap<>();
        filmToMap.put("name", film.getName());
        filmToMap.put("description", film.getDescription());
        filmToMap.put("release_date", film.getReleaseDate());
        filmToMap.put("duration", film.getDuration());
        if (film.getMpa() != null) {
            filmToMap.put("mpa", film.getMpa().getId());
        }

        final long id = simpleJdbcInsert.executeAndReturnKey(filmToMap).longValue();
        film.setId(id);

        if (!film.getGenres().isEmpty()) {

            film.getGenres().forEach((genre) -> jdbcTemplate.update(
                    "INSERT INTO public.genre " +
                            "(film_id, genre_id) " +
                            "VALUES (?, ?)",
                    film.getId(), genre.getId()));

            film.getGenres().clear();

            final String sqlGenresByFilmId =
                    "SELECT id, name " +
                            "FROM public.genres " +
                            "JOIN public.genre on public.genres.id = public.genre.genre_id " +
                            "WHERE public.genre.film_id = ?" +
                            "ORDER BY id";

            film.getGenres()
                    .addAll(
                            jdbcTemplate.query(sqlGenresByFilmId,
                                    dbGenreStorage::makeGenre,
                                    film.getId()));
        }

        return film;
    }

    @Override
    public void resetGlobalId() {
        jdbcTemplate.update(
                "ALTER TABLE public.films " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 1");
    }
}
