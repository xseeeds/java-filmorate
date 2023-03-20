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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserManager;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Autowired
    public UserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, UserService userService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @AfterEach
    void ternDown() {
        userService.removeAllUser();
    }


    @Test
    @SneakyThrows
    void postAndGetAndDeleteUsersTest() {

        String testUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated());

        String response = mockMvc
                .perform(get("/users"))
                .andExpect(status()
                        .isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<User> userList = objectMapper
                .readValue(response, new TypeReference<>() {
                });

        assertEquals(1, userList.size());


        mockMvc.perform(delete("/users"))
                .andExpect(status()
                        .isResetContent());

        response = mockMvc
                .perform(get("/users"))
                .andExpect(status()
                        .isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals("[]", response);
    }

    @Test
    @SneakyThrows
    public void postUserSetNameTest() {

        String testUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(jsonPath("$.name")
                        .value("dolore"));
    }

    @Test
    @SneakyThrows
    void postFailUserLoginTest() {

        String failLoginUser = "{\n" +
                "  \"login\": \"dolore ullamco whitSpaces\",\n" +
                "  \"email\": \"yandex@mail.ru\",\n" +
                "  \"birthday\": \"2446-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failLoginUser))
                .andExpect(status()
                        .isBadRequest());
    }

    @Test
    @SneakyThrows
    public void postFailUserEmailTest() {

        String failEmailUser = "{\n" +
                "  \"login\": \"doloreullamco\",\n" +
                "  \"name\": \"\",\n" +
                "  \"email\": \"это-неправильный?эмейл@\",\n" +
                "  \"birthday\": \"1980-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failEmailUser))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    public void postFailUserBirthdayTest() {

        String failBirthdayUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"\",\n" +
                "  \"email\": \"test@mail.ru\",\n" +
                "  \"birthday\": \"2446-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failBirthdayUser))
                .andExpect(status()
                        .isBadRequest());
    }

    @Test
    @SneakyThrows
    public void postBadRequestUserWithIdTest() {

        String userWithId =
                "{\"id\" : 100, " +
                        "  \"login\": \"doloreullamco\",\n" +
                        "  \"name\": \"\",\n" +
                        "  \"email\": \"test@mail.ru\",\n" +
                        "  \"birthday\": \"1980-08-20\"\n" +
                        "}";

        mockMvc
                .perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userWithId))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    public void postUserEmailAndLoginConflictTest() {

        String testUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser));


        String existentLoginByUser = "{\n" +
                "  \"login\": \"dolore123\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existentLoginByUser))
                .andExpect(status()
                        .isConflict());


        String existentEmailByUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail123@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existentEmailByUser))
                .andExpect(status()
                        .isConflict());
    }


    @Test
    @SneakyThrows
    public void putUserTest() {

        String testUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser));


        String userToUpdate = "{\n" +
                "  \"login\": \"doloreUpdate\",\n" +
                "  \"name\": \"est adipisicing\",\n" +
                "  \"id\": 1,\n" +
                "  \"email\": \"mail@yandex.ru\",\n" +
                "  \"birthday\": \"1976-09-20\"\n" +
                "}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userToUpdate))
                .andExpect(status()
                        .isOk())
                .andExpect(content()
                        .json(userToUpdate));
    }


    @Test
    @SneakyThrows
    public void putUserSetNameTest() {

        String testUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser));

        String userToUpdate = "{\n" +
                "  \"login\": \"doloreUpdate\",\n" +
                "  \"id\": 1,\n" +
                "  \"email\": \"mail@yandex.ru\",\n" +
                "  \"birthday\": \"1976-09-20\"\n" +
                "}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userToUpdate))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.name")
                        .value("doloreUpdate"));
    }


    @Test
    @SneakyThrows
    public void putFailUserIdNotFound() {

        String notFoundIdUser = "{\n" +
                "  \"login\": \"doloreUpdate\",\n" +
                "  \"name\": \"est adipisicing\",\n" +
                "  \"id\": 9999,\n" +
                "  \"email\": \"mail@yandex.ru\",\n" +
                "  \"birthday\": \"1976-09-20\"\n" +
                "}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notFoundIdUser))
                .andExpect(status()
                        .isNotFound());
    }


    @Test
    @SneakyThrows
    public void putFailUserIdIsEmpty() {

        String IdIsEmptyUser = "{\n" +
                "  \"login\": \"doloreUpdate\",\n" +
                "  \"name\": \"est adipisicing\",\n" +
                "  \"email\": \"mail@yandex.ru\",\n" +
                "  \"birthday\": \"1976-09-20\"\n" +
                "}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(IdIsEmptyUser))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    public void putUserEmailAndLoginConflictTest() {

        String testUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser));


        String testUser2 = "{\n" +
                "  \"login\": \"dolore123\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail123@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser2))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser2));



        String existentLoginByUser = "{\n" +
                "  \"login\": \"dolore123\",\n" +
                "  \"name\": \"Nick123 Name123\",\n" +
                "  \"id\": 1,\n" +
                "  \"email\": \"mail@ya.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existentLoginByUser))
                .andExpect(status()
                        .isConflict());


        String existentEmailByUser = "{\n" +
                "  \"login\": \"doloreYa\",\n" +
                "  \"name\": \"Nick123 Name123\",\n" +
                "  \"id\": 1,\n" +
                "  \"email\": \"mail123@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existentEmailByUser))
                .andExpect(status()
                        .isConflict());
    }
}
