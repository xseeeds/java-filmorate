package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final FilmService filmService;
    private final Film film1 = Film.builder().title("nisi eiusmod").description("adipisicing").releaseDate(LocalDate.of(1967, 3, 25)).duration(100).build();
    private final Film film1WithId = Film.builder().id(1L).title("nisi eiusmod").description("adipisicing").releaseDate(LocalDate.of(1967, 3, 25)).duration(100).build();
    private final Film film2 = Film.builder().title("Film Updated").description("New film update decription").releaseDate(LocalDate.of(1989, 4, 17)).duration(190).rate(4).build();
    private final Film film2WithId = Film.builder().id(2L).title("Film Updated").description("New film update decription").releaseDate(LocalDate.of(1989, 4, 17)).duration(190).rate(4).build();
    private final Film filmWithNotFoundId = Film.builder().id(9999L).title("Film Updated").description("New film update decription").releaseDate(LocalDate.of(1989, 4, 17)).duration(190).rate(4).build();
    private final Film emptyNameFilm = Film.builder().title(" ").description("Description").releaseDate(LocalDate.of(1900, 3, 25)).duration(200).build();
    private final Film longDescriptionFilm = Film.builder().title("Film title").description("Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова, который задолжал им деньги, а именно 20 миллионов. о Куглов, который за время «своего отсутствия», стал кандидатом Коломбани.").releaseDate(LocalDate.of(1900, 3, 25)).duration(200).build();
    private final Film failReleaseDateFilm = Film.builder().title("Title").description("Description").releaseDate(LocalDate.of(1895, 12, 27)).duration(200).build();
    private final Film boundaryReleaseDateFilm = Film.builder().title("Title").description("Description").releaseDate(LocalDate.of(1895, 12, 28)).duration(200).build();
    private final Film boundaryReleaseDateFilmWithId = Film.builder().id(1L).title("Title").description("Description").releaseDate(LocalDate.of(1895, 12, 28)).duration(200).build();
    private final Film zeroDurationFilm = Film.builder().title("Title").description("Descrition").releaseDate(LocalDate.of(1980, 3, 25)).duration(0).build();
    private final Film negativeDurationFilm = Film.builder().title("Title").description("Descrition").releaseDate(LocalDate.of(1980, 3, 25)).duration(-1).build();
    private final Film filmToUpdate = Film.builder().id(1L).title("Film Updated").description("New film update decription").releaseDate(LocalDate.of(1989, 4, 17)).duration(190).rate(4).build();
    private final Film emptyFilmId = Film.builder().title("Film Updated").description("New film update decription").releaseDate(LocalDate.of(1989, 4, 17)).duration(190).rate(4).build();
    private final Film filmDuplicate = Film.builder().id(2L).title("nisi eiusmod").description("adipisicing").releaseDate(LocalDate.of(1967, 3, 25)).duration(100).build();
    private final User user = User.builder().login("dolore").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userWithId = User.builder().id(1L).login("dolore").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();


    @AfterEach
    void ternDown() {
        filmService.removeAllFilm();
    }


    @Test
    @SneakyThrows
    void postAndGetAndDeleteAllFilmsTest() {

        String testFilm = objectMapper.writeValueAsString(film1);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm))
                .andExpect(status()
                        .isCreated());

        String contentAsString = mockMvc
                .perform(get("/films"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<Film> filmList = objectMapper
                .readValue(contentAsString, new TypeReference<>() {
                });

        assertEquals(1, filmList.size());


        mockMvc.perform(delete("/films"))
                .andExpect(status()
                        .isOk());

        contentAsString = mockMvc
                .perform(get("/films"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("[]", contentAsString);
    }


    @Test
    @SneakyThrows
    void postBadRequestFilmWithIdTest() {

        String testFilmWithId = objectMapper.writeValueAsString(filmWithNotFoundId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilmWithId))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    void postFailFilmEmptyName() {

        String testEmptyNameFilm = objectMapper.writeValueAsString(emptyNameFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testEmptyNameFilm))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    void postFailFilmLongDescription() {

        String testLongDescriptionFilm = objectMapper.writeValueAsString(longDescriptionFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testLongDescriptionFilm))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    void postFailFilmReleaseDate() {

        String testFailReleaseDateFilm = objectMapper.writeValueAsString(failReleaseDateFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFailReleaseDateFilm))
                .andExpect(status()
                        .isBadRequest());


        String testBoundaryReleaseDateFilm = objectMapper.writeValueAsString(boundaryReleaseDateFilm);
        String testBoundaryReleaseDateFilmWithId = objectMapper.writeValueAsString(boundaryReleaseDateFilmWithId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testBoundaryReleaseDateFilm))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testBoundaryReleaseDateFilmWithId));
    }

    @Test
    @SneakyThrows
    void postFailFilmNegativeDuration() {

        String testZeroDurationFilm = objectMapper.writeValueAsString(zeroDurationFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testZeroDurationFilm))
                .andExpect(status()
                        .isBadRequest());


        String testNegativeDurationFilm = objectMapper.writeValueAsString(negativeDurationFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testNegativeDurationFilm))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    void postFailFilmDuplicate() {

        String testFilm = objectMapper.writeValueAsString(film1);
        String testFilm1WithId = objectMapper.writeValueAsString(film1WithId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm1WithId));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm))
                .andExpect(status()
                        .isConflict());
    }


    @Test
    @SneakyThrows
    void putFilmTest() {

        String testFilm = objectMapper.writeValueAsString(film1);
        String testFilm1WithId = objectMapper.writeValueAsString(film1WithId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm1WithId));


        String testFilmToUpdate = objectMapper.writeValueAsString(filmToUpdate);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilmToUpdate))
                .andExpect(status()
                        .isOk())
                .andExpect(content()
                        .json(testFilmToUpdate));
    }


    @Test
    @SneakyThrows
    void putFailFilmEmptyId() {

        String testEmptyFilmId = objectMapper.writeValueAsString(emptyFilmId);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testEmptyFilmId))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    void putFailFilmIdNotFound() {

        String testNotFoundFilmId = objectMapper.writeValueAsString(filmWithNotFoundId);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testNotFoundFilmId))
                .andExpect(status()
                        .isNotFound());
    }

    @Test
    @SneakyThrows
    void putFailFilmDuplicate() {

        String testFilm1 = objectMapper.writeValueAsString(film1);
        String testFilm1WithId = objectMapper.writeValueAsString(film1WithId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm1))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm1WithId));

        String testFilm2 = objectMapper.writeValueAsString(film2);
        String testFilm2WithId = objectMapper.writeValueAsString(film2WithId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm2))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm2WithId));


        String testFilmDuplicate = objectMapper.writeValueAsString(filmDuplicate);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilmDuplicate))
                .andExpect(status()
                        .isConflict());
    }


    @Test
    @SneakyThrows
    void addAndDeleteUserLikeOnFilmAndGetPopularListAndRepeatedLike() {

        String testFilm1 = objectMapper.writeValueAsString(film1);
        String testFilm1WithId = objectMapper.writeValueAsString(film1WithId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm1))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm1WithId));

        String testFilm2 = objectMapper.writeValueAsString(film2);
        String testFilm2WithId = objectMapper.writeValueAsString(film2WithId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm2))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm2WithId));

        String testUser = objectMapper.writeValueAsString(user);
        String testUserWithId = objectMapper.writeValueAsString(userWithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUserWithId));


        mockMvc.perform(put("/films/2/like/1"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.likes[0]")
                        .value("1"));

        mockMvc.perform(put("/films/2/like/1"))
                .andExpect(status()
                        .isConflict());


        mockMvc.perform(get("/films/popular")
                        .param("count", "2"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*]", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("2"))
                .andExpect(jsonPath("$[1].id").value("1"));


        mockMvc.perform(delete("/films/2/like/1"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.likes")
                        .isEmpty());


        mockMvc.perform(delete("/users"));
    }


    @Test
    @SneakyThrows
    void getAndDeleteFilmByIdTest() {

        String testFilm1 = objectMapper.writeValueAsString(film1);
        String testFilm1WithId = objectMapper.writeValueAsString(film1WithId);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm1))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm1WithId));


        mockMvc.perform(get("/films/1"))
                .andExpect(status()
                        .isOk())
                .andExpect(content()
                        .json(testFilm1WithId));

        mockMvc.perform(delete("/films/2"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/films/1"))
                .andExpect(status().isOk());
    }
}
/*
    Для проверки списка пользователей можно воспользоваться методом `jsonPath`
    и перебрать каждый элемент списка. Например:

        ```
        mockMvc.perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").value("John"))
        .andExpect(jsonPath("$[1].name").value("Mary"))
        .andExpect(jsonPath("$[2].name").value("Bob"))
        // и т.д.
        ```

        Здесь `"$[0].name"` означает "название поля `name` у первого элемента списка".
         Если нужно проверить все элементы списка, можно использовать структуру `$.[*].name`.

        Если список содержит переменное количество элементов, то можно использовать `*.name`,
         например:

        ```
        mockMvc.perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[*].name").value(hasItem("John")))
        .andExpect(jsonPath("$[*].name").value(hasItem("Mary")))
        .andExpect(jsonPath("$[*].name").value(hasItem("Bob")))
        // и т.д.
        ```

        Здесь `hasItem` - это метод из библиотеки Hamcrest, который позволяет проверять,
         есть ли в списке элемент с заданным значением.
*/