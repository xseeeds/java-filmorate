package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;


@RestController
@RequestMapping("/films")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmController {
    private final FilmService filmService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        if (film.getId() != null) {
            throw new BadRequestException("POST request. Для обновления используй PUT запрос, film имеет id!!!");
        }
        return filmService.createFilm(film);
    }

    @GetMapping("/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilmById(@PathVariable long filmId) {
        return filmService.getFilmById(filmId);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@RequestBody Film film) {
        if (film.getId() == null) {
            throw new BadRequestException("PUT request. Для обновления используй id!!! в теле запроса film");
        }
        return filmService.updateFilm(film);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> getAllFilm() {
        return filmService.getAllFilm();
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllFilm() {
        return ResponseEntity.ok(filmService.removeAllFilm());
    }

    @DeleteMapping("/{filmId}")
    public ResponseEntity<String> removeFilmById(@PathVariable long filmId) {
        return ResponseEntity.ok(filmService.removeFilmById(filmId));
    }

    @PutMapping("/{filmId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addUserLikeByFilmId(@PathVariable long filmId, @PathVariable long userId) {
        filmService.addUserLikeByFilmId(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeUserLikeByFilmId(@PathVariable long filmId, @PathVariable long userId) {
        filmService.removeUserLikeByFilmId(filmId, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> getFilmByPopular(
            @RequestParam(value = "count", defaultValue = "10", required = false) int count) {
        return filmService.getFilmByPopular(count);                         //?count={count}
    }


}