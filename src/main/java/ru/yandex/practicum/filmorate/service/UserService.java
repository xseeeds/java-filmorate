package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

import static ru.yandex.practicum.filmorate.model.Status.*;


@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserStorage userStorage;


    @Validated
    public User createUser(@Valid User createdUser) throws ConflictException {

        userStorage.checkUserLogin(createdUser.getLogin());
        userStorage.checkUserEmail(createdUser.getEmail());

        final User user = userStorage.createUser(createdUser);

        log.info("newUser {}", user);

        return user;
    }

    @Validated
    public User updateUser(@Valid User updatedUser) throws NotFoundException, ConflictException {

        userStorage.checkUserById(updatedUser.getId());
        userStorage.checkUserIdOnLogin(updatedUser.getLogin(), updatedUser.getId());
        userStorage.checkUserIdOnEmail(updatedUser.getEmail(), updatedUser.getId());

        final User user = userStorage.updateUser(updatedUser);

        log.info("Пользователь обновлен {}", user);

        return user;
    }

    public List<User> getAllUser() {

        final List<User> allUser = userStorage.getAllUser();

        log.info("Текущее количество пользователей : {}", allUser.size());

        return allUser;
    }

    public User getUserById(@Positive long userId) throws NotFoundException {

        final User user = userStorage.getUserById(userId);

        log.info("Пользователь получен : {}", user);

        return user;
    }

    public String removeAllUser() {

        userStorage.removeAllUser();

        log.info("Все пользователи удалены. id сброшен");

        return "Все пользователи удалены. id сброшен";
    }

    public String removeUserById(@Positive long userId) throws NotFoundException {

        userStorage.removeUserById(userId);

        log.info("Пользователь с id => {} удален/удалены все его подписки", userId);

        return "Пользователь c id => " + userId + "удален/удалены все его подписки";
    }

    public void addFriend(@Positive long userId, @Min(-1) long otherId) throws NotFoundException, ConflictException {

        userStorage.checkUserById(userId);
        userStorage.checkUserById(otherId);

        userStorage.checkUserFriendById(userId, otherId, true);
        userStorage.checkUserFriendById(otherId, userId, true);

        if (userStorage.checkFriendship(userId, otherId)
                && userStorage.checkStatusFriendship(userId, otherId, APPLICATION)) {

            userStorage.updateStatusFriendShip(userId, otherId, FRIENDSHIP);
            userStorage.updateStatusFriendShip(otherId, userId, FRIENDSHIP);

            log.info("User с id => {} подтвердил дружбу c пользователем c id => {} статус {}", userId, otherId, FRIENDSHIP);

        } else {

            userStorage.addFriend(userId, otherId, SUBSCRIPTION);
            userStorage.addFriend(otherId, userId, APPLICATION);

            log.info("У пользователя с id => {} добавлена заявка от пользователю c id => {} неподтвержденная дружба статус {}", userId, otherId, SUBSCRIPTION);
            log.info("Подписчик с id => {} добавлен пользователю c id => {} неподтвержденная дружба статус {}", otherId, userId, APPLICATION);
        }
    }

    public void removeFriend(@Positive long userId, @Positive long otherId) throws NotFoundException, ConflictException {

        userStorage.checkFriendByUserId(userId);

        try {
            userStorage.checkUserFriendById(userId, otherId, false);

            userStorage.checkUserByFriendId(otherId);

        } catch (NotFoundException e) {

            if (userStorage.checkStatusFriendship(userId, otherId, SUBSCRIPTION)
                    || userStorage.checkStatusFriendship(userId, otherId, APPLICATION)) {

                userStorage.removeFriend(userId, otherId);

                log.info(e.getMessage() + " => Подписчик с id => {} удален от пользователя c id => {}", otherId, userId);
                return;
            }
            throw new NotFoundException(e.getMessage());
        }

        if (userStorage.checkStatusFriendship(userId, otherId, SUBSCRIPTION)
                || userStorage.checkStatusFriendship(userId, otherId, APPLICATION)) {

            if (userStorage.checkFriendship(otherId, userId)
                    && userStorage.checkStatusFriendship(otherId, userId, APPLICATION)) {

                userStorage.removeFriend(otherId, userId);

                log.info("Пользователь с id => {} удалил от пользователя c id => {} заявку", userId, otherId);
            }

            userStorage.removeFriend(userId, otherId);

            log.info("У пользователя с id => {} удалена подписка/заявка от пользователя c id => {}", userId, otherId);

        } else {

            userStorage.checkUserFriendById(otherId, userId, false);

            userStorage.removeFriend(userId, otherId);
            userStorage.updateStatusFriendShip(otherId, userId, SUBSCRIPTION);

            log.info("Пользователь с id => {} удален от пользователя c id => {} и оставлен в подписках", otherId, userId);
        }
    }

    public List<User> getAllFriendsByUser(@Positive long userId) throws NotFoundException {

        userStorage.checkUserById(userId);

        List<User> allFriends = new ArrayList<>();

        try {
            userStorage.checkFriendByUserId(userId);

        } catch (NotFoundException e) {

            log.info(e.getMessage());

            return allFriends;
        }

        allFriends = userStorage.getAllFriendsByUserId(userId);

        log.info("Текущее количество друзей пользователя с id => {}; => {}", userId, allFriends.size());

        return allFriends;
    }

    public List<User> getCommonFriendsByUser(@Positive long userId, @Positive long otherId) throws NotFoundException, ConflictException {

        userStorage.checkUserById(userId);

        userStorage.checkUserById(otherId);

        List<User> commonFriends = new ArrayList<>();

        try {
            userStorage.checkFriendByUserId(userId);
            userStorage.checkFriendByUserId(otherId);

        } catch (NotFoundException e) {

            log.info(e.getMessage());

            return commonFriends;
        }

        commonFriends = userStorage.getCommonFriendsByUser(userId, otherId);

        log.info("Текущее количество общих друзей пользователя с id => {} и пользователя с id => {} ===> {}",
                userId, otherId, commonFriends.size());

        return commonFriends;
    }
}