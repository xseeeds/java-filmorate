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
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.yandex.practicum.filmorate.storage.Managers.getDefaultFilmManager;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private final InMemoryFilmManager filmManager = getDefaultFilmManager();

    @AfterEach
    public void ternDown() {
        filmManager.removeAllFilm();
    }


    @Test
    @SneakyThrows
    public void postAndGetAndDeleteFilmsTest() {

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
                        .is(201));

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
                        .is(205));

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
                        .is(400));
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
                        .is(400));
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
                        .is(400));
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
                .andExpect(status().is(400));


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
                        .is(201));
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
                        .is(400));


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
                        .is(400));
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
                        .is(201));


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
                        .is(409));
    }


    @Test
    @SneakyThrows
    public void putFilmTest() {

        String createFilm = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createFilm))
                .andExpect(status()
                        .is(201));


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
                        .is(200));
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
                        .is(400));
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
                        .is(404));
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
                        .is(201));

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
                        .is(201));


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
                .andExpect(status().is(409));
    }
}
