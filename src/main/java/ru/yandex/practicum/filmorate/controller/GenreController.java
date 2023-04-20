package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Genre> getGenres() {
        return genreService.getGenres();
    }

    @GetMapping("/{genreId}")
    @ResponseStatus(HttpStatus.OK)
    public Genre getGenreById(@PathVariable int genreId) {
        return genreService.getGenreById(genreId);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Genre addGenre(@RequestBody Genre genre) {
        return genreService.addGenre(genre);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Genre updateGenre(@RequestBody Genre genre) {
        return genreService.updateGenre(genre);
    }

    @PutMapping("/{genreId}/films/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public void addGenreOnFilm(@PathVariable int genreId, @PathVariable long filmId) {
        genreService.addGenreOnFilm(genreId, filmId);
    }

    @DeleteMapping("/{genreId}/films/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeGenreOnFilm(@PathVariable int genreId, @PathVariable long filmId) {
        genreService.removeGenreOnFilm(genreId, filmId);
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllFilm() {
        return ResponseEntity.ok(genreService.removeAllGenre());
    }

    @DeleteMapping("/{genreId}")
    public ResponseEntity<String> removeFilmById(@PathVariable int genreId) {
        return ResponseEntity.ok(genreService.removeGenreById(genreId));
    }
}