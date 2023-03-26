package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<Film> getFilmById(@PathVariable @Positive int filmId) {
        return filmService.getFilmById(filmId);
    }

    @GetMapping
    public ResponseEntity<Collection<Film>> getAllFilm() {
        return filmService.getAllFilm();
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllFilm() {
        return filmService.removeAllFilm();
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Film> removeFilmById(@PathVariable @Positive int filmId) {
        return filmService.removeFilmById(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Film> addUserLikeByFilmId(@PathVariable @Positive int filmId,
                                                    @PathVariable @Positive int userId) {
        return filmService.addUserLikeByFilmId(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Film> removeUserLikeByFilmId(@PathVariable @Positive int filmId,
                                                       @PathVariable @Positive int userId) {
        return filmService.removeUserLikeByFilmId(filmId, userId);
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<Film>> getFilmByPopular(
            @RequestParam                                                           //?count={count}
                    (value = "count", defaultValue = "10", required = false) @Positive int count) {
        return filmService.getFilmByPopular(count);
    }
}