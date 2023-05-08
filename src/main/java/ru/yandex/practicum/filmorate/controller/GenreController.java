package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
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
    public List<Genre> getAllGenre() {
        return genreService.getAllGenre();
    }

    @GetMapping("/{genreId}")
    @ResponseStatus(HttpStatus.OK)
    public Genre getGenreById(@PathVariable long genreId) {
        return genreService.getGenreById(genreId);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Genre createGenre(@RequestBody Genre genre) {
        if (genre.getId() != null) {
            throw new BadRequestException("POST request. Для обновления используй PUT запрос, genre имеет id!!!");
        }
        return genreService.createGenre(genre);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Genre updateGenre(@RequestBody Genre genre) {
        if (genre.getId() == null) {
            throw new BadRequestException("PUT request. Для обновления используй id!!! в теле запроса genre");
        }
        return genreService.updateGenre(genre);
    }

    @PutMapping("/{genreId}/films/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public void addGenreOnFilm(@PathVariable long genreId, @PathVariable long filmId) {
        genreService.addGenreOnFilm(genreId, filmId);
    }

    @DeleteMapping("/{genreId}/films/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeGenreOnFilm(@PathVariable long genreId, @PathVariable long filmId) {
        genreService.removeGenreOnFilm(genreId, filmId);
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllGenre() {
        return ResponseEntity.ok(genreService.removeAllGenre());
    }

    @DeleteMapping("/{genreId}")
    public ResponseEntity<String> removeGenreById(@PathVariable long genreId) {
        return ResponseEntity.ok(genreService.removeGenreById(genreId));
    }
}