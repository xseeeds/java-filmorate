package ru.yandex.practicum.filmorate.storage.film.storage;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Optional;

@Component
public class InMemoryFilmStorageImplOnUpdate implements FilmStorage.OnUpdate {
    @Override
    public Film updateFilm(Film film) {
        Optional<Film> oldFilm = InMemoryFilmStorageImpl.films
                .stream()
                .filter(f -> f.getId().equals(film.getId()))
                .findFirst();
        if (oldFilm.isPresent()) {
            InMemoryFilmStorageImpl.films.remove(oldFilm.get());
            InMemoryFilmStorageImpl.films.add(film);
        }
        return film;
    }
}
