package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static ru.yandex.practicum.filmorate.model.Status.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDatabaseTest {
    private final UserStorage userStorage;
    private final UserService userService;

    @AfterEach
    public void ternDown() {
        userStorage.removeAllUser();
    }

    @Test
    public void testUserCreateGetUpdateCheck() {

        assertThatThrownBy(
                () -> userStorage.checkUserById(1L))
                .isInstanceOf(
                        NotFoundException.class)
                .hasMessageContaining(
                        "Такой пользователь c id => 1 не существует");

        assertThatExceptionOfType(
                NotFoundException.class)
                .isThrownBy(
                        () -> userStorage.getUserById(1L))
                .withMessageMatching(
                        "Такой пользователь c id => 1 не существует");


        final List<User> users = userStorage.getAllUser();

        assertThat(users)
                .size()
                .isZero();

        userStorage.createUser(
                User
                        .builder()
                        .name("John Winston Lennon")
                        .email("john@beatles.uk")
                        .login("john")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());
        User john = userStorage.getUserById(1L);

        assertThat(
                john)
                .isNotNull()
                .hasFieldOrPropertyWithValue(
                        "id", 1L)
                .hasFieldOrPropertyWithValue(
                        "name", "John Winston Lennon");

        User johnNewName = john
                .toBuilder()
                .name("John Ono Lennon")
                .build();

        userStorage.updateUser(johnNewName);
        johnNewName = userStorage.getUserById(1L);

        assertThat(
                johnNewName)
                .hasFieldOrPropertyWithValue(
                        "name", "John Ono Lennon");

        final List<User> allUsers = userStorage.getAllUser();

        assertThat(
                allUsers)
                .size()
                .isEqualTo(1);

    }

    @Test
    public void testUserCheckLoginAndEmail() {

        userStorage.createUser(
                User
                        .builder()
                        .name("John")
                        .email("john@beatles.uk")
                        .login("john")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());

        assertThatExceptionOfType(
                ConflictException.class)
                .isThrownBy(
                        () -> userStorage.checkUserLogin("john"))
                            .withMessageMatching(
                        "Такой пользователь с login: john уже существует, по id => 1 для обновления используй PUT запрос");

        assertThatNoException()
                .isThrownBy(
                        () -> userStorage.checkUserLogin("john1"));


        assertThatExceptionOfType(
                ConflictException.class)
                .isThrownBy(
                        () -> userStorage.checkUserEmail("john@beatles.uk"))
                .withMessageMatching(
                        "Такой пользователь с email:john@beatles.uk уже существует, по id => 1 для обновления используй PUT запрос");

        assertThatNoException()
                .isThrownBy(
                        () -> userStorage.checkUserEmail("john1@beatles.uk"));

    }

    @Test
    public void testUserFriend() {

        userStorage.createUser(
                User
                        .builder()
                        .name("John")
                        .email("john@beatles.uk")
                        .login("john")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());
        userStorage.createUser(
                User
                        .builder()
                        .name("Paul")
                        .email("paul@beatles.uk")
                        .login("paul")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());

        userStorage.addFriend(1L, 2L, FRIENDSHIP);


        assertThat(
                userStorage.getAllFriendsByUserId(1L))
                .size()
                .isEqualTo(1);

        assertThat(
                userStorage.getAllFriendsByUserId(2L))
                .size()
                .isZero();


        assertThatExceptionOfType(
                ConflictException.class)
                .isThrownBy(
                        () -> userStorage.checkUserFriendById(1L, 2L, true))
                .withMessageMatching(
                        "У пользователя с id => 1 уже существует дружба с id => 2");

        assertThatNoException()
                .isThrownBy(
                        () -> userStorage.checkUserFriendById(2L, 1L, true));

        assertThatNoException()
                .isThrownBy(
                        () -> userStorage.checkUserFriendById(1L, 2L, false));


        userStorage.removeFriend(1L, 2L);


        assertThatExceptionOfType(
                NotFoundException.class)
                .isThrownBy(
                        () -> userStorage.checkUserFriendById(1L, 2L, false))
                .withMessageMatching(
                        "У пользователя с id => 1 не существует друга/заявки/подписки c id => 2");
    }

    @Test
    public void testFriendship() {

        userStorage.createUser(
                User
                        .builder()
                        .name("John")
                        .email("john@beatles.uk")
                        .login("john")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());

        userStorage.createUser(
                User
                        .builder()
                        .name("Paul")
                        .email("paul@beatles.uk")
                        .login("paul")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());

        userStorage.createUser(
                User
                        .builder()
                        .name("Simon")
                        .email("simon@beatles.uk")
                        .login("simon")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());


        userService.addFriend(1, 2);

        assertThat(userStorage.checkStatusFriendship(1, 2, SUBSCRIPTION))
                .isTrue();
        assertThat(userStorage.checkStatusFriendship(2, 1, APPLICATION))
                .isTrue();

        userService.addFriend(2, 1);

        assertThat(userStorage.checkStatusFriendship(1, 2, FRIENDSHIP))
                .isTrue();
        assertThat(userStorage.checkStatusFriendship(2, 1, FRIENDSHIP))
                .isTrue();

        userService.addFriend(3, 1);

        assertThat(userStorage.checkStatusFriendship(3, 1, SUBSCRIPTION))
                .isTrue();
        assertThat(userStorage.checkStatusFriendship(1, 3, APPLICATION))
                .isTrue();

        final List<User> allFriends = userStorage.getAllFriendsByUserId(1);

        assertThat(allFriends)
                .size()
                .isEqualTo(1);

        final List<User> commonFriends = userStorage.getCommonFriendsByUser(2, 3);

        assertThat(commonFriends)
                .size()
                .isEqualTo(1);

        userService.removeFriend(2, 1);

        assertThat(userStorage.checkStatusFriendship(1, 2, SUBSCRIPTION))
                .isTrue();
        assertThat(userStorage.checkFriendship(2, 1))
                .isFalse();

        userService.removeFriend(1, 2);

        assertThat(userStorage.checkFriendship(1, 2))
                .isFalse();

        userService.removeFriend(1, 3);

        assertThat(userStorage.checkFriendship(1, 3))
                .isFalse();
        assertThat(userStorage.checkStatusFriendship(3, 1, SUBSCRIPTION))
                .isTrue();
    }



}