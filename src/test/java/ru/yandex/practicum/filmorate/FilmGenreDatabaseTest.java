package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmGenreDatabaseTest {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorStorage directorStorage;


    @AfterEach
    void ternDown() {
        filmStorage.removeAllFilm();
    }

    @Test
    void testFilmCreateGetUpdateCheck() {

        assertThatThrownBy(
                () -> filmStorage.checkFilmById(1L))
                .isInstanceOf(
                        NotFoundException.class)
                .hasMessageContaining(
                        "Такой фильм с id => 1 не существует");

        assertThatExceptionOfType(
                NotFoundException.class)
                .isThrownBy(
                        () -> filmStorage.getFilmById(1L))
                .withMessageMatching(
                        "Такой фильм с id => 1 не существует");


        final List<Film> films = filmStorage.getAllFilm();

        assertThat(films)
                .size()
                .isZero();

        filmStorage.createFilm(
                Film
                        .builder()
                        .name("The Shawshank Redemption")
                        .description("Nominated for 7 Oscars")
                        .releaseDate(LocalDate.of(1994, 9, 22))
                        .duration(144)
                        .mpa(Mpa.builder().id(1L).build())
                        .build());

        final Film film1 = filmStorage.getFilmById(1L);

        assertThat(film1)
                .isNotNull()
                .hasFieldOrPropertyWithValue(
                        "id", 1L)
                .hasFieldOrPropertyWithValue(
                        "name", "The Shawshank Redemption")
                .hasFieldOrPropertyWithValue(
                        "description", "Nominated for 7 Oscars");

        film1.setRate((float) 5);

        filmStorage.updateFilm(film1);

        assertThat(
                film1)
                .hasFieldOrPropertyWithValue(
                        "rate", (float) 5);

        final List<Film> allFilms = filmStorage.getAllFilm();

        assertThat(
                allFilms)
                .size()
                .isEqualTo(1);

    }

    @Test
    void testCheckFilmByNameReleaseDateDuration() {

        filmStorage.createFilm(
                Film
                        .builder()
                        .name("The Shawshank Redemption")
                        .description("Nominated for 7 Oscars")
                        .releaseDate(LocalDate.of(1994, 9, 22))
                        .duration(144)
                        .mpa(Mpa.builder().id(1L).build())
                        .build());

        assertThatThrownBy(
                () -> filmStorage.checkFilmByNameReleaseDate(
                        Film
                                .builder()
                                .name("The Shawshank Redemption")
                                .description("Nominated for 7 Oscars")
                                .releaseDate(LocalDate.of(1994, 9, 22))
                                .duration(144)
                                .mpa(Mpa.builder().id(1L).build())
                                .build()))
                .isInstanceOf(
                        ConflictException.class)
                .hasMessageContaining(
                        "Такой фильм с именем => The Shawshank Redemption и датой релиза => 1994-09-22 уже существует по id => 1");
    }

    @Test
    void testAddAndRemoveUserLikeOnFilm() {

        filmStorage.createFilm(
                Film
                        .builder()
                        .name("The Shawshank Redemption")
                        .description("Nominated for 7 Oscars")
                        .releaseDate(LocalDate.of(1994, 9, 22))
                        .duration(144)
                        .mpa(Mpa.builder().id(1L).build())
                        .build());

        filmStorage.createFilm(
                Film
                        .builder()
                        .name("The Godfather")
                        .description("Won 3 Oscars")
                        .releaseDate(LocalDate.of(1972, 3, 17))
                        .duration(144)
                        .mpa(Mpa.builder().id(1L).build())
                        .build());

        userStorage.createUser(
                User
                        .builder()
                        .name("John")
                        .email("john@beatles.uk")
                        .login("john")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());

        filmStorage.addUserMarkOnFilm(2L, 1L, 3);

        assertThat(
                new ArrayList<>(filmStorage.getFilmByPopular(10, null, null)).get(0))
                .hasFieldOrPropertyWithValue(
                        "name", "The Godfather");

        assertThat(
                new ArrayList<>(filmStorage.getFilmByPopular(10, null, null)).get(1))
                .hasFieldOrPropertyWithValue(
                        "name", "The Shawshank Redemption");


        filmStorage.removeUserMarkOnFilm(2L, 1L, 3);
        filmStorage.addUserMarkOnFilm(1L, 1L,  7);

        assertThat(
                new ArrayList<>(
                        filmStorage.getFilmByPopular(10, null, null)).get(0))
                .hasFieldOrPropertyWithValue(
                        "name", "The Shawshank Redemption");

        assertThat(
                new ArrayList<>(
                        filmStorage.getFilmByPopular(10, null, null)).get(1))
                .hasFieldOrPropertyWithValue(
                        "name", "The Godfather");


        assertThatThrownBy(
                () -> filmStorage.checkFilmLikeByUserId(1L, 1L, true))
                .isInstanceOf(
                        ConflictException.class)
                .hasMessageContaining(
                        "У фильма с id => 1 уже существует лайк пользователя с id => 1");

        assertThatThrownBy(
                () ->
                        filmStorage.checkFilmLikeByUserId(2L, 1L, false))
                .isInstanceOf(
                        NotFoundException.class)
                .hasMessageContaining(
                        "У фильма с id => 2 не существует лайка пользователя с id => 1");

        userStorage.removeAllUser();
    }

    //TODO добавить тест checkOnFilm
    @Test
    void testGenreCreateCheck() {

        assertThat(
                mpaStorage.getMpaById(1L))
                .hasFieldOrPropertyWithValue(
                        "name", "G");

        final List<Genre> genres = genreStorage.getAllGenre();

        assertThat(genres)
                .size()
                .isEqualTo(6);

        genreStorage.createGenre(Genre
                .builder()
                .name("New genre")
                .build());

        assertThat(
                genreStorage.getAllGenre())
                .size()
                .isEqualTo(7);

        assertThat(
                genreStorage.getGenreById(7L))
                .hasFieldOrPropertyWithValue(
                        "name", "New genre");


        genreStorage.updateGenre(Genre
                .builder()
                .id(7L)
                .name("Update genre")
                .build());

        assertThatThrownBy(
                () -> genreStorage.checkGenreByName("Update genre", true))
                .isInstanceOf(
                        ConflictException.class)
                .hasMessageContaining(
                        "Жанр => Update genre уже существует по id => 7");


        assertThatThrownBy(
                        () -> genreStorage.checkGenreByName("Some genre", false))
                .isInstanceOf(
                        NotFoundException.class)
                .hasMessageContaining("Жанр => Some genre не существует");

    }

    //TODO добавить тест checkOnFilm
    @Test
    void testDirectorCreateCheck() {

        final List<Director> directors = directorStorage.getAllDirector();

        assertThat(directors)
                .size()
                .isEqualTo(0);

        directorStorage.createDirector(Director
                .builder()
                .name("New director")
                .build());

        assertThat(
                directorStorage.getAllDirector())
                .size()
                .isEqualTo(1);

        assertThat(
                directorStorage.getDirectorById(1L))
                .hasFieldOrPropertyWithValue(
                        "name", "New director");


        directorStorage.updateDirector(Director
                .builder()
                .id(1L)
                .name("Update director")
                .build());

        assertThatThrownBy(
                () -> directorStorage.checkDirectorByName(
                        Director
                                .builder()
                                .name("Update director")
                                .build()))
                .isInstanceOf(
                        ConflictException.class)
                .hasMessageContaining(
                        "Режиссёр => Update director уже существует по id => 1");


        assertThatNoException()
                .isThrownBy(
                        () -> directorStorage.checkDirectorByName(
                                Director
                                        .builder()
                                        .name("Some director")
                                        .build()));

    }
}
