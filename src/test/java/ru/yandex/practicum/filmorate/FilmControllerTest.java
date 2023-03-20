package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final FilmService filmService;

    @Autowired
    public FilmControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, FilmService filmService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.filmService = filmService;
    }

    @AfterEach
    public void ternDown() {
        filmService.removeAllFilm();
    }


    @Test
    @SneakyThrows
    public void postAndGetAndDeleteAllFilmsTest() {

        String testFilm = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

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
                        .isResetContent());

        contentAsString = mockMvc
                .perform(get("/films"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("[]", contentAsString);
    }


    @Test
    @SneakyThrows
    public void postBadRequestFilmWithIdTest() {

        String filmWithId = "{\n" +
                "  \"id\": 9999,\n" +
                "  \"name\": \"Film Updated\",\n" +
                "  \"releaseDate\": \"1989-04-17\",\n" +
                "  \"description\": \"New film update decription\",\n" +
                "  \"duration\": 190,\n" +
                "  \"rate\": 4\n" +
                "}";

        mockMvc
                .perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmWithId))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    public void postFailFilmEmptyName() {

        String emptyNameFilm = "{\n" +
                "  \"name\": \"\",\n" +
                "  \"description\": \"Description\",\n" +
                "  \"releaseDate\": \"1900-03-25\",\n" +
                "  \"duration\": 200\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyNameFilm))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    public void postFailFilmLongDescription() {

        String longDescriptionFilm = "{\n" +
                "  \"name\": \"Film name\",\n" +
                "  \"description\": \"Пятеро друзей ( комик-группа «Шарло»), приезжают в город Бризуль." +
                " Здесь они хотят разыскать господина Огюста Куглова, который задолжал им деньги," +
                " а именно 20 миллионов. о Куглов, который за время «своего отсутствия»," +
                " стал кандидатом Коломбани.\",\n" +
                "    \"releaseDate\": \"1900-03-25\",\n" +
                "  \"duration\": 200\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(longDescriptionFilm))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    public void postFailFilmReleaseDate() {

        String failReleaseDateFilm = "{\n" +
                "  \"name\": \"Name\",\n" +
                "  \"description\": \"Description\",\n" +
                "  \"releaseDate\": \"1895-12-27\",\n" +
                "  \"duration\": 200\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failReleaseDateFilm))
                .andExpect(status().isBadRequest());


        String testBoundaryReleaseDateFilm = "{\n" +
                "  \"name\": \"Name\",\n" +
                "  \"description\": \"Description\",\n" +
                "  \"releaseDate\": \"1895-12-28\",\n" +
                "  \"duration\": 200\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testBoundaryReleaseDateFilm))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testBoundaryReleaseDateFilm));
    }

    @Test
    @SneakyThrows
    public void postFailFilmNegativeDuration() {

        String zeroDurationFilm = "{\n" +
                "  \"name\": \"Name\",\n" +
                "  \"description\": \"Descrition\",\n" +
                "  \"releaseDate\": \"1980-03-25\",\n" +
                "  \"duration\": 0\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(zeroDurationFilm))
                .andExpect(status()
                        .isBadRequest());


        String negativeDurationFilm = "{\n" +
                "  \"name\": \"Name\",\n" +
                "  \"description\": \"Descrition\",\n" +
                "  \"releaseDate\": \"1980-03-25\",\n" +
                "  \"duration\": -1\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(negativeDurationFilm))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    public void postFailFilmDuplicate() {

        String testFilm = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm));


        String twinFilm = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(twinFilm))
                .andExpect(status()
                        .isConflict());
    }


    @Test
    @SneakyThrows
    public void putFilmTest() {

        String testFilm = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm));


        String filmToUpdate = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Film Updated\",\n" +
                "  \"releaseDate\": \"1989-04-17\",\n" +
                "  \"description\": \"New film update decription\",\n" +
                "  \"duration\": 190,\n" +
                "  \"rate\": 4\n" +
                "}";

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmToUpdate))
                .andExpect(status()
                        .isOk())
                .andExpect(content()
                        .json(filmToUpdate));
    }


    @Test
    @SneakyThrows
    public void putFailFilmEmptyId() {

        String emptyFilm = "{\n" +
                "  \"name\": \"Film Updated\",\n" +
                "  \"releaseDate\": \"1989-04-17\",\n" +
                "  \"description\": \"New film update decription\",\n" +
                "  \"duration\": 190,\n" +
                "  \"rate\": 4\n" +
                "}";

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyFilm))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    public void putFailFilmIdNotFound() {

        String notFoundIdFilm = "{\n" +
                "  \"id\": 9999,\n" +
                "  \"name\": \"Film Updated\",\n" +
                "  \"releaseDate\": \"1989-04-17\",\n" +
                "  \"description\": \"New film update decription\",\n" +
                "  \"duration\": 190,\n" +
                "  \"rate\": 4\n" +
                "}";

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notFoundIdFilm))
                .andExpect(status()
                        .isNotFound());
    }

    @Test
    @SneakyThrows
    public void putFailFilmDuplicate() {

        String testFilm1 = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm1))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm1));

        String testFilm2 = "{\n" +
                "  \"name\": \"Film Updated\",\n" +
                "  \"releaseDate\": \"1989-04-17\",\n" +
                "  \"description\": \"New film update decription\",\n" +
                "  \"duration\": 190,\n" +
                "  \"rate\": 4\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testFilm2))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testFilm2));


        String filmDuplicate =
                "{\"id\" : 2, " +
                        "  \"name\": \"nisi eiusmod\",\n" +
                        "  \"description\": \"adipisicing\",\n" +
                        "  \"releaseDate\": \"1967-03-25\",\n" +
                        "  \"duration\": 100\n" +
                        "}";

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmDuplicate))
                .andExpect(status()
                        .isConflict());
    }
}
