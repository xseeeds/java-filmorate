package ru.yandex.practicum.filmorate.storage.film.storage;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

@Component
public class InMemoryFilmStorageImplOnCreate implements FilmStorage.OnCreate {

    private long globalId = 0;

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        InMemoryFilmStorageImpl.films.add(film);
        InMemoryFilmStorageImpl.idsFilms.add(film.getId());
        return film;
    }

    @Override
    public void resetGlobalId() {
        globalId = 0;
    }

    private long getNextId() {
        return ++globalId;
    }
}
