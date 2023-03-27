package ru.yandex.practicum.filmorate.storage.film;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

@Component
public class FilmStorageImplOnCreateFilm implements FilmStorage.OnCreate {

    private long globalId = 0;

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        FilmStorageImpl.films.add(film);
        FilmStorageImpl.idsFilms.add(film.getId());
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
