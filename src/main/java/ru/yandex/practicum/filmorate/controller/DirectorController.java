package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAllDirector() {
        return directorService.getAllDirector();
    }

    @GetMapping("/{directorId}")
    public Director getDirectorById(@PathVariable int directorId) {
        return directorService.getDirectorById(directorId);
    }

    @PostMapping
    public Director createDirector(@RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        return directorService.update(director);
    }

    @PutMapping("/{directorId}/films/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public void addDirectorOnFilm(@PathVariable long directorId, @PathVariable long filmId) {
        directorService.addDirectorOnFilm(directorId, filmId);
    }

    @DeleteMapping("/{directorId}/films/{filmId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeDirectorOnFilm(@PathVariable long directorId, @PathVariable long filmId) {
        directorService.removeDirectorOnFilm(directorId, filmId);
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllDirector() {
        return ResponseEntity.ok(directorService.removeAllDirector());
    }

    @DeleteMapping("{directorId}")
    public String removeById(@PathVariable int directorId) {
        return directorService.removeById(directorId);
    }
}
