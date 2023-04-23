package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
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


    @AfterEach
    public void ternDown() {
        filmStorage.removeAllFilm();
    }

    @Test
    public void testFilmCreateGetUpdateCheck() {

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


        final Collection<Film> films = filmStorage.getAllFilm();

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
                        .mpa(Mpa.builder().id(1).build())
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

        final Collection<Film> allFilms = filmStorage.getAllFilm();

        assertThat(
                allFilms)
                .size()
                .isEqualTo(1);

    }

    @Test
    public void testCheckFilmByNameReleaseDateDuration() {

        filmStorage.createFilm(
                Film
                        .builder()
                        .name("The Shawshank Redemption")
                        .description("Nominated for 7 Oscars")
                        .releaseDate(LocalDate.of(1994, 9, 22))
                        .duration(144)
                        .mpa(Mpa.builder().id(1).build())
                        .build());

        assertThatThrownBy(
                () -> filmStorage.checkFilmByNameReleaseDateDuration(
                        Film
                                .builder()
                                .name("The Shawshank Redemption")
                                .description("Nominated for 7 Oscars")
                                .releaseDate(LocalDate.of(1994, 9, 22))
                                .duration(144)
                                .mpa(Mpa.builder().id(1).build())
                                .build()))
                .isInstanceOf(
                        ConflictException.class)
                .hasMessageContaining(
                        "Такой фильм с именем => The Shawshank Redemption уже существует");
    }

    @Test
    public void testAddAndRemoveUserLikeOnFilm() {

        filmStorage.createFilm(
                Film
                        .builder()
                        .name("The Shawshank Redemption")
                        .description("Nominated for 7 Oscars")
                        .releaseDate(LocalDate.of(1994, 9, 22))
                        .duration(144)
                        .mpa(Mpa.builder().id(1).build())
                        .build());

        filmStorage.createFilm(
                Film
                        .builder()
                        .name("The Godfather")
                        .description("Won 3 Oscars")
                        .releaseDate(LocalDate.of(1972, 3, 17))
                        .duration(144)
                        .mpa(Mpa.builder().id(1).build())
                        .build());

        userStorage.createUser(
                User
                        .builder()
                        .name("John")
                        .email("john@beatles.uk")
                        .login("john")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());

        filmStorage.addUserLikeOnFilm(2L, 1L);

        assertThat(
                new ArrayList<>(filmStorage.getFilmByPopular(10)).get(0))
                .hasFieldOrPropertyWithValue(
                        "name", "The Godfather");

        assertThat(
                new ArrayList<>(filmStorage.getFilmByPopular(10)).get(1))
                .hasFieldOrPropertyWithValue(
                        "name", "The Shawshank Redemption");


        filmStorage.removeUserLikeOnFilm(2L, 1L);
        filmStorage.addUserLikeOnFilm(1L, 1L);

        assertThat(
                new ArrayList<>(
                        filmStorage.getFilmByPopular(10)).get(0))
                .hasFieldOrPropertyWithValue(
                        "name", "The Shawshank Redemption");

        assertThat(
                new ArrayList<>(
                        filmStorage.getFilmByPopular(10)).get(1))
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

    @Test
    public void testGenreCreateCheck() {

        final List<Genre> genres = genreStorage.getGenreList();

        assertThat(genres)
                .size()
                .isEqualTo(6);

        genreStorage.createGenre(Genre
                .builder()
                .name("New genre")
                .build());

        assertThat(
                genreStorage.getGenreList())
                .size()
                .isEqualTo(7);

        assertThat(
                genreStorage.getGenreById(7))
                .hasFieldOrPropertyWithValue(
                        "name", "New genre");


        genreStorage.updateGenre(Genre
                .builder()
                .id(7)
                .name("Update genre")
                .build());

        assertThatThrownBy(
                () -> genreStorage.checkGenre(Genre
                        .builder()
                        .name("Update genre")
                        .build()))
                .isInstanceOf(
                        NotFoundException.class)
                .hasMessageContaining(
                        "Жанр => Update genre уже существует по id => 7");


        assertThatNoException()
                .isThrownBy(
                        () -> genreStorage.checkGenre(
                                Genre
                                        .builder()
                                        .name("Some genre")
                                        .build()));

    }
}
