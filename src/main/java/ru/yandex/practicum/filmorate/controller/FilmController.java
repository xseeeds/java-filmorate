package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.NoParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

import java.util.Collection;
import java.util.Map;


@RestController
@RequestMapping("/films")
public class FilmController {
    FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Film>> addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public ResponseEntity<Map<String, Film>> updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<Map<String, Film>> getFilmById(@RequestParam(required = false) @Positive Integer filmId) {
        if (filmId == null) {
            throw new NoParameterException("filmId");
        }
        return filmService.getFilmById(filmId);
    }

    @GetMapping
    public ResponseEntity<Map<String, Collection<Film>>> getAllFilm() {
        return filmService.getAllFilm();
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllFilm() {
        return filmService.removeAllFilm();
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<Map<String, Film>> removeFilmById(@RequestParam(required = false) @Positive Integer filmId) {
        if (filmId == null) {
            throw new NoParameterException("filmId");
        }
        return filmService.removeFilmById(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Map<String, Film>> addUserLikeByFilmId(@RequestParam(required = false) @Positive Integer filmId,
                                                     @RequestParam(required = false) @Positive Integer userId) {
        if (filmId == null) {
            throw new NoParameterException("filmId");
        }
        if (userId == null) {
            throw new NoParameterException("userId");
        }
        return filmService.addUserLikeByFilmId(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Map<String, Film>> removeUserLikeByFilmId(@RequestParam(required = false) @Positive Integer filmId,
                                                                    @RequestParam(required = false) @Positive Integer userId) {
        if (filmId == null) {
            throw new NoParameterException("filmId");
        }
        if (userId == null) {
            throw new NoParameterException("userId");
        }
        return filmService.removeUserLikeByFilmId(filmId, userId);
    }

    @GetMapping("/films/popular?count={count}")
    public ResponseEntity<Map<String, Collection<Film>>> getFilmByPopular(@RequestParam(required = false) @Positive Integer count) {
        if (count == null) {
            throw new NoParameterException("count");
        }
        return filmService.getFilmByPopular(count);
    }
}