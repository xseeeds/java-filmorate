package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDatabaseTests {
    private final UserStorage dbUserStorageImpl;
    private final UserStorage.OnCreate dbUserStorageImplOnCreate;
    private final UserStorage.OnUpdate dbUserStorageImplOnUpdate;

    @AfterEach
    public void ternDown() {
        dbUserStorageImpl.removeAllUser();
    }

    @Test
    public void testUserCreateGetUpdateCheck() {

        assertThatThrownBy(
                () -> dbUserStorageImpl.checkUserById(1L))
                .isInstanceOf(
                        NotFoundException.class)
                .hasMessageContaining(
                        "Такой пользователь c id => 1 не существует");

        assertThatExceptionOfType(
                NotFoundException.class)
                .isThrownBy(
                        () -> dbUserStorageImpl.getUserById(1L))
                .withMessageMatching(
                        "Такой пользователь c id => 1 не существует");


        Collection<User> users = dbUserStorageImpl.getAllUser();

        assertThat(users)
                .size()
                .isZero();

        dbUserStorageImplOnCreate.createUser(
                User
                        .builder()
                        .name("John Winston Lennon")
                        .email("john@beatles.uk")
                        .login("john")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());
        User john = dbUserStorageImpl.getUserById(1L);

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

        dbUserStorageImplOnUpdate.updateUser(johnNewName);
        johnNewName = dbUserStorageImpl.getUserById(1L);

        assertThat(
                johnNewName)
                .hasFieldOrPropertyWithValue(
                        "name", "John Ono Lennon");

        Collection<User> allUsers = dbUserStorageImpl.getAllUser();

        assertThat(
                allUsers)
                .size()
                .isEqualTo(
                        1);

    }

    @Test
    public void testUserCheckLoginAndEmail() {

        dbUserStorageImplOnCreate.createUser(
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
                        () -> dbUserStorageImpl.checkUserLogin("john"))
                            .withMessageMatching(
                        "Такой пользователь с login: john уже существует, по id => 1 для обновления используй PUT запрос");

        assertThatNoException()
                .isThrownBy(
                        () -> dbUserStorageImpl.checkUserLogin("john1"));


        assertThatExceptionOfType(
                ConflictException.class)
                .isThrownBy(
                        () -> dbUserStorageImpl.checkUserEmail("john@beatles.uk"))
                .withMessageMatching(
                        "Такой пользователь с email:john@beatles.uk уже существует, по id => 1 для обновления используй PUT запрос");

        assertThatNoException()
                .isThrownBy(
                        () -> dbUserStorageImpl.checkUserEmail("john1@beatles.uk"));

    }

    @Test
    public void testUserFriend() {

        dbUserStorageImplOnCreate.createUser(
                User
                        .builder()
                        .name("John")
                        .email("john@beatles.uk")
                        .login("john")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());
        dbUserStorageImplOnCreate.createUser(
                User
                        .builder()
                        .name("Paul")
                        .email("paul@beatles.uk")
                        .login("paul")
                        .birthday(LocalDate.of(1940, 10, 9))
                        .build());

        dbUserStorageImpl.addFriend(1L, 2L, Status.FRIENDSHIP);


        assertThat(
                dbUserStorageImpl.getAllFriendsByUserId(1L))
                .size()
                .isEqualTo(1);

        assertThat(
                dbUserStorageImpl.getAllFriendsByUserId(2L))
                .size()
                .isZero();


        assertThatExceptionOfType(
                ConflictException.class)
                .isThrownBy(
                        () -> dbUserStorageImpl.checkUserFriendById(1L, 2L, true))
                .withMessageMatching(
                        "У пользователя с id => 1 уже существует дружба с id => 2");

        assertThatNoException()
                .isThrownBy(
                        () -> dbUserStorageImpl.checkUserFriendById(2L, 1L, true));

        assertThatNoException()
                .isThrownBy(
                        () -> dbUserStorageImpl.checkUserFriendById(1L, 2L, false));


        dbUserStorageImpl.removeFriend(1L, 2L);


        assertThatExceptionOfType(
                NotFoundException.class)
                .isThrownBy(
                        () -> dbUserStorageImpl.checkUserFriendById(1L, 2L, false))
                .withMessageMatching(
                        "У пользователя с id => 1 не существует друга/заявки/подписки c id => 2");
    }

}