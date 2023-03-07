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
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static ru.yandex.practicum.filmorate.storage.Managers.getDefaultUserManager;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private final InMemoryUserManager userManager = getDefaultUserManager();

    @AfterEach
    void ternDown() {
        userManager.removeAllUser();
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
                        .is(201));

        String response = mockMvc.perform(get("/users"))
                .andExpect(status()
                        .is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<User> userList = objectMapper
                .readValue(response, new TypeReference<>() {
                });

        assertEquals(1, userList.size());


        mockMvc.perform(delete("/users"))
                .andExpect(status()
                        .is(205));

        response = mockMvc
                .perform(get("/users"))
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
                        .is(201));


        String response = mockMvc.perform(get("/users"))
                .andExpect(status()
                        .is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<User> users = objectMapper
                .readValue(response, new TypeReference<>() {
                });

        assertEquals("dolore", users.get(0).getName());
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
                        .is(400));
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
                        .is(400));

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
                        .is(400));

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
                        .is(400));
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
                        .is(201));


        String loginAlreadyUsedUser = "{\n" +
                "  \"login\": \"dolore123\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginAlreadyUsedUser))
                .andExpect(status()
                        .is(409));


        String emailAlreadyUsedUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail123@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emailAlreadyUsedUser))
                .andExpect(status()
                        .is(409));
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
                        .is(201));


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
                        .is(200));
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
                        .is(201));

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
                        .is(200));

        String response = mockMvc.perform(get("/users"))
                .andExpect(status()
                        .is(200))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<User> users = objectMapper
                .readValue(response, new TypeReference<>() {
                });

        assertEquals("doloreUpdate", users.get(0).getName());
    }


    @Test
    @SneakyThrows
    public void putFailUserIdNotFound() {

        String notFoundIdUser = "{\n" +
                "  \"login\": \"doloreUpdate\",\n" +
                "  \"name\": \"est adipisicing\",\n" +
                "  \"id\": 1,\n" +
                "  \"email\": \"mail@yandex.ru\",\n" +
                "  \"birthday\": \"1976-09-20\"\n" +
                "}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notFoundIdUser))
                .andExpect(status()
                        .is(404));
    }


    @Test
    @SneakyThrows
    public void putFailUserIdIdIsEmpty() {

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
                        .is(400));

        String failIdUser = "{\n" +
                "  \"login\": \"doloreUpdate\",\n" +
                "  \"name\": \"est adipisicing\",\n" +
                "  \"id\": 9999,\n" +
                "  \"email\": \"mail@yandex.ru\",\n" +
                "  \"birthday\": \"1976-09-20\"\n" +
                "}";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failIdUser))
                .andExpect(status()
                        .is(404));

    }
}
