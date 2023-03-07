package ru.yandex.practicum.filmorate.controller;


import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmManager;

import javax.validation.Valid;

import java.util.Collection;

import static ru.yandex.practicum.filmorate.storage.Managers.getDefaultFilmManager;


@RestController
public class FilmController {

    private final InMemoryFilmManager filmManager = getDefaultFilmManager();

    @PostMapping("/films")
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        return filmManager.addFilm(film);
    }

    @PutMapping("/films")
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        return filmManager.updateFilm(film);
    }

    @GetMapping("/films")
    public Collection<Film> getAllFilm() {
        return filmManager.getAllFilm();
    }

    @DeleteMapping("/films")
    public ResponseEntity<String> removeAllFilm() {
        return filmManager.removeAllFilm();
    }
}