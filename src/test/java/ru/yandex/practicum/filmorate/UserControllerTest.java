package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;


import java.time.LocalDate;
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
    private final User user1 = User.builder().login("dolore").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User user1WithId = User.builder().id(1L).login("dolore").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User user2 = User.builder().login("dolore123").name("Nick Name").email("mail123@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User user2WithId = User.builder().id(2L).login("dolore123").name("Nick Name").email("mail123@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User user3 = User.builder().login("dolore12345").name("Nick Name").email("mail12345@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User user3WithId = User.builder().id(3L).login("dolore12345").name("Nick Name").email("mail12345@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userNameNull = User.builder().login("dolore").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userNameIsBlank = User.builder().login("dolore123").name(" ").email("mail123@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userLoginWithWhitespace = User.builder().login("dolore ullamco whitSpaces").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userFailEmail = User.builder().login("dolore").name("Nick Name").email("это-неправильный?эмейл@").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userFailBirthday = User.builder().login("dolore").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(2446, 8, 20)).build();
    private final User userExistentLogin = User.builder().login("dolore123").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userExistentEmail = User.builder().login("dolore").name("Nick Name").email("mail123@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userToUpdate = User.builder().id(1L).login("doloreUpdate").name("est adipisicing").email("mail@yandex.ru").birthday(LocalDate.of(1976, 9, 20)).build();
    private final User userToUpdateWithoutName = User.builder().id(1L).login("doloreUpdate").email("mail@yandex.ru").birthday(LocalDate.of(1976, 9, 20)).build();
    private final User userNotFoundId = User.builder().id(9999L).login("dolore").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userExistentLoginWithId = User.builder().id(1L).login("dolore123").name("Nick Name").email("mail@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();
    private final User userExistentEmailWithId = User.builder().id(2L).login("dolore").name("Nick Name").email("mail123@mail.ru").birthday(LocalDate.of(1946, 8, 20)).build();

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

        String testUser = objectMapper.writeValueAsString(user1);

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

        String testUserNameNull = objectMapper.writeValueAsString(userNameNull);

        String testUserNameIsBlank = objectMapper.writeValueAsString(userNameIsBlank);

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
                        .value("dolore123"));
    }

    @Test
    @SneakyThrows
    void postFailUserLoginWithWhitespaceTest() {

        String testUserLoginWithWhitespace = objectMapper.writeValueAsString(userLoginWithWhitespace);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUserLoginWithWhitespace))
                .andExpect(status()
                        .isBadRequest());
    }

    @Test
    @SneakyThrows
    void postFailUserEmailTest() {

        String failEmailUser = objectMapper.writeValueAsString(userFailEmail);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failEmailUser))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    void postFailUserBirthdayTest() {

        String failBirthdayUser = objectMapper.writeValueAsString(userFailBirthday);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(failBirthdayUser))
                .andExpect(status()
                        .isBadRequest());
    }

    @Test
    @SneakyThrows
    void postBadRequestUserWithIdTest() {

        String userWithId = objectMapper.writeValueAsString(user1WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userWithId))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    void postUserEmailAndLoginConflictTest() {

        String testUser =  objectMapper.writeValueAsString(user1);
        String testUser1WithId = objectMapper.writeValueAsString(user1WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser1WithId));


        String existentLoginByUser = objectMapper.writeValueAsString(userExistentLogin);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existentLoginByUser))
                .andExpect(status()
                        .isConflict());


        String existentEmailByUser = objectMapper.writeValueAsString(userExistentEmail);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existentEmailByUser))
                .andExpect(status()
                        .isConflict());
    }


    @Test
    @SneakyThrows
    void putUserTest() {

        String testUser = objectMapper.writeValueAsString(user1);
        String testUser1WithId = objectMapper.writeValueAsString(user1WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser1WithId));


        String testUserToUpdate = objectMapper.writeValueAsString(userToUpdate);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUserToUpdate))
                .andExpect(status()
                        .isOk())
                .andExpect(content()
                        .json(testUserToUpdate));
    }


    @Test
    @SneakyThrows
    void putUserSetNameTest() {

        String testUser = objectMapper.writeValueAsString(user1);
        String testUser1WithId = objectMapper.writeValueAsString(user1WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser1WithId));

        String testUserToUpdateWithoutName = objectMapper.writeValueAsString(userToUpdateWithoutName);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUserToUpdateWithoutName))
                .andExpect(status()
                        .isOk())
                .andExpect(jsonPath("$.name")
                        .value("doloreUpdate"));
    }


    @Test
    @SneakyThrows
    void putFailUserIdNotFound() {

        String testNotFoundIdUser = objectMapper.writeValueAsString(userNotFoundId);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testNotFoundIdUser))
                .andExpect(status()
                        .isNotFound());
    }


    @Test
    @SneakyThrows
    void putFailUserIdIsEmpty() {

        String testIdIsEmptyUser = objectMapper.writeValueAsString(user1);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testIdIsEmptyUser))
                .andExpect(status()
                        .isBadRequest());
    }


    @Test
    @SneakyThrows
    void putUserEmailAndLoginConflictTest() {

        String testUser = objectMapper.writeValueAsString(user1);
        String testUser1WithId = objectMapper.writeValueAsString(user1WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser1WithId));


        String testUser2 = objectMapper.writeValueAsString(user2);
        String testUser2WithId = objectMapper.writeValueAsString(user2WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser2))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser2WithId));


        String existentLoginByUser = objectMapper.writeValueAsString(userExistentLoginWithId);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existentLoginByUser))
                .andExpect(status()
                        .isConflict());


        String existentEmailByUser = objectMapper.writeValueAsString(userExistentEmailWithId);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(existentEmailByUser))
                .andExpect(status()
                        .isConflict());
    }


    @Test
    @SneakyThrows
    void addAndDeleteUserFriendsAndDeleteUserByIdCheckFriendTest() {

        String testUser = objectMapper.writeValueAsString(user1);
        String testUser1WithId = objectMapper.writeValueAsString(user1WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser1WithId));

        String testUser2 = objectMapper.writeValueAsString(user2);
        String testUser2WithId = objectMapper.writeValueAsString(user2WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser2))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser2WithId));


        String testUser3 = objectMapper.writeValueAsString(user3);
        String testUser3WithId = objectMapper.writeValueAsString(user3WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser3))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser3WithId));


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

        String testUser = objectMapper.writeValueAsString(user1);
        String testUser1WithId = objectMapper.writeValueAsString(user1WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser1WithId));

        String testUser2 = objectMapper.writeValueAsString(user2);
        String testUser2WithId = objectMapper.writeValueAsString(user2WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser2))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser2WithId));


        String testUser3 = objectMapper.writeValueAsString(user3);
        String testUser3WithId = objectMapper.writeValueAsString(user3WithId);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(testUser3))
                .andExpect(status()
                        .isCreated())
                .andExpect(content()
                        .json(testUser3WithId));


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