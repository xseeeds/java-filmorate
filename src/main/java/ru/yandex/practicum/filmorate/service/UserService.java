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
import java.util.Collection;

import static ru.yandex.practicum.filmorate.model.Status.*;


@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserStorage dbUserStorageImpl;


    @Validated
    public User createUser(@Valid User createdUser) throws ConflictException {

        dbUserStorageImpl.checkUserLogin(createdUser.getLogin());
        dbUserStorageImpl.checkUserEmail(createdUser.getEmail());

        final User user = dbUserStorageImpl.createUser(createdUser);

        log.info("newUser {}", user);

        return user;
    }

    @Validated
    public User updateUser(@Valid User updatedUser) throws NotFoundException, ConflictException {

        dbUserStorageImpl.checkUserById(updatedUser.getId());
        dbUserStorageImpl.checkUserIdOnLogin(updatedUser.getLogin(), updatedUser.getId());
        dbUserStorageImpl.checkUserIdOnEmail(updatedUser.getEmail(), updatedUser.getId());

        final User user = dbUserStorageImpl.updateUser(updatedUser);

        log.info("Пользователь обновлен {}", user);

        return user;
    }

    public Collection<User> getAllUser() {

        final Collection<User> allUser = dbUserStorageImpl.getAllUser();

        log.info("Текущее количество пользователей : {}", allUser.size());

        return allUser;
    }

    public User getUserById(@Positive long userId) throws NotFoundException {

        final User user = dbUserStorageImpl.getUserById(userId);

        log.info("Пользователь получен : {}", user);

        return user;
    }

    public String removeAllUser() {

        dbUserStorageImpl.removeAllUser();

        log.info("Все пользователи удалены. id сброшен");

        return "Все пользователи удалены. id сброшен";
    }

    public String removeUserById(@Positive long userId) throws NotFoundException {

        dbUserStorageImpl.removeUserById(userId);

        log.info("Пользователь с id => {} удален/удалены все его подписки", userId);

        return "Пользователь c id => " + userId + "удален/удалены все его подписки";
    }

    public void addFriend(@Positive long userId, @Min(-1) long otherId) throws NotFoundException, ConflictException {

        dbUserStorageImpl.checkUserById(userId);
        dbUserStorageImpl.checkUserById(otherId);

        dbUserStorageImpl.checkUserFriendById(userId, otherId, true);
        dbUserStorageImpl.checkUserFriendById(otherId, userId, true);

        if (dbUserStorageImpl.checkFriendship(userId, otherId)
                && dbUserStorageImpl.checkStatusFriendship(userId, otherId, APPLICATION)) {

            dbUserStorageImpl.updateStatusFriendShip(userId, otherId, FRIENDSHIP);
            dbUserStorageImpl.updateStatusFriendShip(otherId, userId, FRIENDSHIP);

            log.info("User с id => {} подтвердил дружбу c пользователем c id => {} статус {}", userId, otherId, FRIENDSHIP);

        } else {

            dbUserStorageImpl.addFriend(userId, otherId, SUBSCRIPTION);
            dbUserStorageImpl.addFriend(otherId, userId, APPLICATION);

            log.info("У пользователя с id => {} добавлена заявка от пользователю c id => {} неподтвержденная дружба статус {}", userId, otherId, SUBSCRIPTION);
            log.info("Подписчик с id => {} добавлен пользователю c id => {} неподтвержденная дружба статус {}", otherId, userId, APPLICATION);
        }
    }

    public void removeFriend(@Positive long userId, @Positive long otherId) throws NotFoundException, ConflictException {

        dbUserStorageImpl.checkFriendByUserId(userId);

        try {
            dbUserStorageImpl.checkUserFriendById(userId, otherId, false);

            dbUserStorageImpl.checkUserByFriendId(otherId);

        } catch (NotFoundException e) {

            if (dbUserStorageImpl.checkStatusFriendship(userId, otherId, SUBSCRIPTION)
                    || dbUserStorageImpl.checkStatusFriendship(userId, otherId, APPLICATION)) {

                dbUserStorageImpl.removeFriend(userId, otherId);

                log.info(e.getMessage() + " => Подписчик с id => {} удален от пользователя c id => {}", otherId, userId);
                return;
            }
            throw new NotFoundException(e.getMessage());
        }

        if (dbUserStorageImpl.checkStatusFriendship(userId, otherId, SUBSCRIPTION)
                || dbUserStorageImpl.checkStatusFriendship(userId, otherId, APPLICATION)) {

            if (dbUserStorageImpl.checkFriendship(otherId, userId)
                    && dbUserStorageImpl.checkStatusFriendship(otherId, userId, APPLICATION)) {

                dbUserStorageImpl.removeFriend(otherId, userId);

                log.info("Пользователь с id => {} удалил от пользователя c id => {} заявку", userId, otherId);
            }

            dbUserStorageImpl.removeFriend(userId, otherId);

            log.info("У пользователя с id => {} удалена подписка/заявка от пользователя c id => {}", userId, otherId);

        } else {

            dbUserStorageImpl.checkUserFriendById(otherId, userId, false);

            dbUserStorageImpl.removeFriend(userId, otherId);
            dbUserStorageImpl.updateStatusFriendShip(otherId, userId, SUBSCRIPTION);

            log.info("Пользователь с id => {} удален от пользователя c id => {} и оставлен в подписках", otherId, userId);
        }
    }

    public Collection<User> getAllFriendsByUser(@Positive long userId) throws NotFoundException {

        dbUserStorageImpl.checkUserById(userId);

        Collection<User> allFriends = new ArrayList<>();

        try {
            dbUserStorageImpl.checkFriendByUserId(userId);

        } catch (NotFoundException e) {

            log.info(e.getMessage());

            return allFriends;
        }

        allFriends = dbUserStorageImpl.getAllFriendsByUserId(userId);

        log.info("Текущее количество друзей пользователя с id => {}; => {}", userId, allFriends.size());

        return allFriends;
    }

    public Collection<User> getCommonFriendsByUser(@Positive long userId, @Positive long otherId) throws NotFoundException, ConflictException {

        dbUserStorageImpl.checkUserById(userId);

        dbUserStorageImpl.checkUserById(otherId);

        Collection<User> commonFriends = new ArrayList<>();

        try {
            dbUserStorageImpl.checkFriendByUserId(userId);
            dbUserStorageImpl.checkFriendByUserId(otherId);

        } catch (NotFoundException e) {

            log.info(e.getMessage());

            return commonFriends;
        }

        commonFriends = dbUserStorageImpl.getCommonFriendsByUser(userId, otherId);

        log.info("Текущее количество общих друзей пользователя с id => {} и пользователя с id => {} ===> {}",
                userId, otherId, commonFriends.size());

        return commonFriends;
    }
}