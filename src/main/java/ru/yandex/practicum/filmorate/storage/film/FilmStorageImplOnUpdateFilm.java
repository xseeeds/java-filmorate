package ru.yandex.practicum.filmorate.storage.film;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Optional;

@Component
public class FilmStorageImplOnUpdateFilm implements FilmStorage.OnUpdate{
    @Override
    public Film updateFilm(Film film) {
        Optional<Film> oldFilm = FilmStorageImpl.films
                .stream()
                .filter(f -> f.getId().equals(film.getId()))
                .findFirst();
        if (oldFilm.isPresent()) {
            FilmStorageImpl.films.remove(oldFilm.get());
            FilmStorageImpl.films.add(film);
        }
        return film;
    }
}
