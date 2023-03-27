package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;


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
    UserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, UserService userService) {
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
        userService.removeAllUser();

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
                        .isOk());

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
    void postUserSetNameTest() {

        String testUserNameNull = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        String testUserNameIsBlank = "{\n" +
                "  \"login\": \"dolore1\",\n" +
                "  \"name\": \" \",\n" +
                "  \"email\": \"mail1@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUserNameNull))
                .andExpect(status()
                        .isCreated())
                .andExpect(jsonPath("$.name")
                        .value("dolore"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUserNameIsBlank))
                .andExpect(status()
                        .isCreated())
                .andExpect(jsonPath("$.name")
                        .value("dolore1"));
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
    void postFailUserEmailTest() {

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
    void postFailUserBirthdayTest() {

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
    void postBadRequestUserWithIdTest() {

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
    void postUserEmailAndLoginConflictTest() {

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
    void putUserTest() {

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
    void putUserSetNameTest() {

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
    void putFailUserIdNotFound() {

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
    void putFailUserIdIsEmpty() {

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
    void putUserEmailAndLoginConflictTest() {

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


    @Test
    @SneakyThrows
    void addAndDeleteUserFriendsAndDeleteUserByIdCheckFriendTest() {

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


        String testUser3 = "{\n" +
                "  \"login\": \"dolore12345\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail12345@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser3))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser3));


        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.friendsIds[0]")
                        .value("2"));

        mockMvc.perform(put("/users/1/friends/3"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.friendsIds[1]")
                        .value("3"));


        mockMvc.perform(get("/users/2"))
                .andExpect(jsonPath("$.friendsIds[0]")
                        .value("1"));

        mockMvc.perform(delete("/users/2/friends/1"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.friendsIds")
                        .isEmpty());


        mockMvc.perform(delete("/users/4"))
                .andExpect(status()
                        .isNotFound());

        mockMvc.perform(delete("/users/3"))
                .andExpect(status()
                        .isOk());

        mockMvc.perform(get("/users/1"))
                .andExpect(jsonPath("$.friendsIds")
                        .isEmpty());
    }


    @Test
    @SneakyThrows
    void getListFriendsByUserAndGetCommonFriendsAndRepeatedFriendship() {

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


        String testUser3 = "{\n" +
                "  \"login\": \"dolore12345\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail12345@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser3))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser3));


        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.friendsIds[0]")
                        .value("2"));

        mockMvc.perform(put("/users/1/friends/3"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.friendsIds[1]")
                        .value("3"));

        mockMvc.perform(put("/users/2/friends/3"))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.friendsIds[1]")
                        .value("3"));


        mockMvc.perform(put("/users/3/friends/2"))
                .andExpect(status()
                        .isConflict());


        String response = mockMvc.perform(get("/users/1/friends"))
                .andExpect(status()
                        .isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<User> userList = objectMapper
                .readValue(response, new TypeReference<>() {
                });
        assertEquals(2, userList.size());


        response = mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status()
                        .isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        userList = objectMapper
                .readValue(response, new TypeReference<>() {
                });
        assertEquals(3, userList.get(0).getId());
    }
}