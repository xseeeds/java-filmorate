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
    private final UserStorage.OnCreate dbUserStorageImplOnCreate;
    private final UserStorage.OnUpdate dbUserStorageImplOnUpdate;


    @Validated({UserStorage.OnCreate.class, UserStorage.class})
    public User createUser(@Valid User createdUser) throws ConflictException {

        dbUserStorageImpl.checkUserLogin(createdUser.getLogin());
        dbUserStorageImpl.checkUserEmail(createdUser.getEmail());

        final User user = dbUserStorageImplOnCreate.createUser(createdUser);

        log.info("newUser {}", user);

        return user;
    }


    @Validated({UserStorage.OnUpdate.class, UserStorage.class})
    public User updateUser(@Valid User updatedUser) throws NotFoundException, ConflictException {

        dbUserStorageImpl.checkUserById(updatedUser.getId());
        dbUserStorageImpl.checkUserIdOnLogin(updatedUser.getLogin(), updatedUser.getId());
        dbUserStorageImpl.checkUserIdOnEmail(updatedUser.getEmail(), updatedUser.getId());

        final User user = dbUserStorageImplOnUpdate.updateUser(updatedUser);

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

    public User addFriends(@Positive long userId, @Min(-1) long otherId) throws NotFoundException, ConflictException {

        dbUserStorageImpl.checkUserById(userId);
        dbUserStorageImpl.checkUserById(otherId);

        dbUserStorageImpl.checkUserFriendById(userId, otherId, true);
        dbUserStorageImpl.checkUserFriendById(otherId, userId, true);
/*
        final User user = dbUserStorageImpl.getUserById(userId);

        if (user.getFriendsIdsStatus().containsKey(otherId) && user.getFriendsIdsStatus().get(otherId) == Status.APPLICATION) {

            dbUserStorageImpl.updateStatusFriendShip(userId, otherId, Status.FRIENDSHIP);
            dbUserStorageImpl.updateStatusFriendShip(otherId, userId, Status.FRIENDSHIP);

            log.info("User с id => {} подтвердил дружбу c пользователем c id => {}", userId, otherId);
            return dbUserStorageImpl.getUserById(userId);
        }

        dbUserStorageImpl.addFriend(userId, otherId, Status.SUBSCRIPTION);
        dbUserStorageImpl.addFriend(otherId, userId, Status.APPLICATION);

        log.info("Подписчик с id => {} добавлен пользователю c id => {} неподтвержденная дружба", otherId, userId);

        return dbUserStorageImpl.getUserById(userId);
*/
        dbUserStorageImpl.addFriend(userId, otherId, FRIENDSHIP);

        log.info("Подписчик с id => {} добавлен пользователю c id => {} неподтвержденная дружба", otherId, userId);
        return dbUserStorageImpl.getUserById(userId);
    }


    public User removeFriends(@Positive long userId, @Positive long otherId) throws NotFoundException, ConflictException {

        dbUserStorageImpl.checkFriendByUserId(userId);
        dbUserStorageImpl.checkUserByFriendId(otherId);
/*
        final User user = dbUserStorageImpl.getUserById(userId);
        final User otherUser;

        try {
            otherUser = dbUserStorageImpl.getUserById(otherId);
            dbUserStorageImpl.checkUserFriendById(userId, otherId, false);

        } catch (NotFoundException e) {

            if (user.getFriendsIdsStatus().get(otherId) == Status.SUBSCRIPTION || user.getFriendsIdsStatus().get(otherId) == Status.APPLICATION) {

                dbUserStorageImpl.removeFriend(userId, otherId);

                log.info(e.getMessage() + " => Подписчик с id => {} удален от пользователя c id => {}", otherId, user);
                return dbUserStorageImpl.getUserById(userId);
            }
            throw new NotFoundException(e.getMessage());
        }

        if (user.getFriendsIdsStatus().get(otherId) == Status.SUBSCRIPTION || user.getFriendsIdsStatus().get(otherId) == Status.APPLICATION) {

            if (otherUser.getFriendsIdsStatus().get(userId) != null && otherUser.getFriendsIdsStatus().get(userId) == Status.APPLICATION) {

                dbUserStorageImpl.removeFriend(otherId, userId);

            }

            dbUserStorageImpl.removeFriend(userId, otherId);
            log.info("Подписчик с id => {} удален от пользователя c id => {}", otherId, user);
            return dbUserStorageImpl.getUserById(userId);
        }

        dbUserStorageImpl.checkUserFriendById(otherId, userId, false);

        dbUserStorageImpl.removeFriend(userId, otherId);
        dbUserStorageImpl.updateStatusFriendShip(otherId, userId, Status.SUBSCRIPTION);

        log.info("Friend с id => {} удален от пользователя c id => {} и оставлен в подписках", otherId, user);
*/
        dbUserStorageImpl.removeFriend(userId, otherId);

        return dbUserStorageImpl.getUserById(userId);
    }

    public Collection<User> getAllFriendsByUser(@Positive long userId) throws NotFoundException, ConflictException {

        dbUserStorageImpl.checkUserById(userId);

        Collection<User> allFriends = new ArrayList<>();

        try {
            dbUserStorageImpl.checkFriendByUserId(userId);

        } catch (ConflictException e) {

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

        } catch (ConflictException e) {

            log.info(e.getMessage());

            return commonFriends;
        }

        commonFriends = dbUserStorageImpl.getCommonFriendsByUser(userId, otherId);

        log.info("Текущее количество общих друзей пользователя с id => {} и пользователя с id => {} ===> {}",
                userId, otherId, commonFriends.size());

        return commonFriends;
    }
}