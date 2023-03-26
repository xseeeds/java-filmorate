package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Response;
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
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @GetMapping("/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilmById(@PathVariable @Positive int filmId) {
        return filmService.getFilmById(filmId);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> getAllFilm() {
        return filmService.getAllFilm();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.RESET_CONTENT)
    public Response removeAllFilm() {
        return new Response(filmService.removeAllFilm());
        //Не понимаю почему здесь не приходит ответ с сервера(
    }

    @DeleteMapping("/{filmId}")
    @ResponseStatus(HttpStatus.RESET_CONTENT)
    public Film removeFilmById(@PathVariable @Positive int filmId) {
        return filmService.removeFilmById(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Film addUserLikeByFilmId(@PathVariable @Positive int filmId,
                                    @PathVariable @Positive int userId) {
        return filmService.addUserLikeByFilmId(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public Film removeUserLikeByFilmId(@PathVariable @Positive int filmId,
                                       @PathVariable @Positive int userId) {
        return filmService.removeUserLikeByFilmId(filmId, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> getFilmByPopular(
            @RequestParam                                                           //?count={count}
                    (value = "count", defaultValue = "10", required = false) @Positive int count) {
        return filmService.getFilmByPopular(count);
    }
}