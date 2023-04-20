package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbFilmStorageImplOnUpdate implements FilmStorage.OnUpdate {
    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage dbMpaStorage;
    private final DbGenreStorage dbGenreStorage;

    @Override
    public Film updateFilm(Film film) {
        final Integer mpaId = film.getMpa() == null ? null : film.getMpa().getId();

        if (mpaId != null) {
            film.setMpa(dbMpaStorage.getMpaById(mpaId));
        }

        final String sqlUpdateFilm =
                "UPDATE public.films " +
                        "SET name = ?, description = ?, duration = ?, release_date = ?, mpa = ?" +
                        "WHERE id = ?";

        jdbcTemplate.update(sqlUpdateFilm,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                mpaId,
                film.getId());


        jdbcTemplate.update(
                "DELETE FROM public.genre " +
                        "WHERE film_id = ?",
                film.getId());

        if (!film.getGenres().isEmpty()) {

            film.getGenres().forEach(genre -> jdbcTemplate.update(
                    "INSERT INTO public.genre " +
                            "(film_id, genre_id) " +
                            "VALUES (?, ?)",
                    film.getId(), genre.getId()));

            film.getGenres().clear();

            final String sqlGenresByFilmId =
                    "SELECT id, name " +
                            "FROM public.genres " +
                            "JOIN public.genre on public.genres.id = public.genre.genre_id " +
                            "WHERE public.genre.film_id = ? " +
                            "ORDER BY id";

            film.getGenres()
                    .addAll(
                            jdbcTemplate.query(sqlGenresByFilmId,
                                    dbGenreStorage::makeGenre,
                                    film.getId()));
        }

        jdbcTemplate.update(
                "DELETE FROM public.\"LIKE\" " +
                        "WHERE film_id = ?",
                film.getId());

        if (!film.getLikes().isEmpty()) {

            film.getLikes().forEach(like -> jdbcTemplate.update(
                    "INSERT INTO public.\"LIKE\" " +
                            "(user_id, film_id) " +
                            "VALUES (?, ?)",
                    film.getId(), like));

        }

        return film;
    }
}
