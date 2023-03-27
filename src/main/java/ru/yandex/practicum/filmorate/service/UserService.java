package ru.yandex.practicum.filmorate.service;

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

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@Validated
public class UserService {
    private final UserStorage userStorage;
    private final UserStorage.OnCreate userStorageOnCreate;
    private final UserStorage.OnUpdate userStorageOnUpdate;

    @Autowired
    public UserService(UserStorage userStorage,
                       UserStorage.OnCreate userStorageOnCreate,
                       UserStorage.OnUpdate userStorageOnUpdate) {
        this.userStorage = userStorage;
        this.userStorageOnCreate = userStorageOnCreate;
        this.userStorageOnUpdate =userStorageOnUpdate;
    }


    @Validated({UserStorage.OnCreate.class, UserStorage.class})
    public User createUser(@Valid User user) throws ConflictException {

        /*if (user.getId() != 0) {
            throw new BadRequestException("POST request. Для обновления используй PUT запрос, user имеет id!!! => " + user);
        }*/

        checkUserLogin(user.getLogin());
        checkUserEmail(user.getEmail());

        final User createdUser = userStorageOnCreate.createUser(user);

        log.info("newUser {}", createdUser);

        return createdUser;
    }


    @Validated({UserStorage.OnUpdate.class, UserStorage.class})
    public User updateUser(@Valid User user) throws NotFoundException, ConflictException {

        /*if (user.getId() == 0) {
            throw new BadRequestException("PUT request. Для обновления используй id!!! в теле запроса => " + user);
        }*/

        checkUserById(user.getId());
        checkUserIdOnLogin(user.getLogin(), user.getId());
        checkUserIdOnEmail(user.getEmail(), user.getId());

        final User updatedUser =  userStorageOnUpdate.updateUser(user);

        log.info("Пользователь обновлен {}", updatedUser);

        return updatedUser;
    }

    public Collection<User> getAllUser() {

        final Collection<User> allUser = userStorage.getAllUser();

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

        userStorageOnCreate.resetGlobalId();

        log.info("Все пользователи удалены. id сброшен");

        return "Все пользователи удалены. id сброшен";
    }

    public String removeUserById(@Positive long userId) throws NotFoundException {

        userStorage.removeUserById(userId);

        userStorage.getAllUser().forEach(u -> u.getFriendsIds().remove(userId));

        log.info("Пользователь с id=>{} удален/удален от всех друзей", userId);

        return "Пользователь c id=>" + userId + "удален/удален от всех друзей";
    }

    public User addFriends(@Positive long userId, @Min(-1) long friendId) throws NotFoundException, ConflictException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        checkUserFriendById(user, friendId, true);
        checkUserFriendById(friend, userId, true);

        user.getFriendsIds().add(friendId);
        friend.getFriendsIds().add(userId);

        userStorageOnUpdate.updateUser(user);
        userStorageOnUpdate.updateUser(friend);

        log.info("Friend с id=>{} добавлен пользователю c id=>{}", friendId, user);

        return user;
    }

    public User removeFriends(@Positive long userId, @Positive long friendId) throws NotFoundException, ConflictException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        checkUserFriendById(user, friendId, false);
        checkUserFriendById(friend, userId, false);

        user.getFriendsIds().remove(friendId);
        friend.getFriendsIds().remove(userId);

        userStorageOnUpdate.updateUser(user);
        userStorageOnUpdate.updateUser(friend);

        log.info("Friend с id=>{} удален от пользователя c id=>{}", friendId, user);

        return user;
    }

    public Collection<User> getFriends(@Positive long userId) throws NotFoundException, ConflictException {

        final User user = userStorage.getUserById(userId);

        Collection<User> allFriends = new ArrayList<>();

        try {
            checkFriendsByUser(user);

        } catch (ConflictException e) {

            log.info(e.getMessage());

            return allFriends;
        }

        allFriends = user
                .getFriendsIds()
                .stream()
                .map(id -> {
                    User friend;
                    try {
                        friend = userStorage.getUserById(id);

                    } catch (NotFoundException e) {

                        return null;
                    }
                    return friend;
                })
                .collect(toList());

        log.info("Текущее количество друзей пользователя с id=>{}; =>{}", userId, allFriends.size());

        return allFriends;
    }

    public Collection<User> getCommonFriends(@Positive long userId, @Positive long friendId) throws NotFoundException, ConflictException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        Collection<User> commonFriends = new ArrayList<>();

        try {
            checkFriendsByUser(user);
            checkFriendsByUser(friend);

        } catch (ConflictException e) {

            log.info(e.getMessage());

            return commonFriends;
        }

        commonFriends = user
                .getFriendsIds()
                .stream()
                .filter(friend.getFriendsIds()::contains)
                .map(id -> {
                    User commonFriend;
                    try {

                        commonFriend = userStorage.getUserById(id);

                    } catch (NotFoundException e) {

                        return null;
                    }
                    return commonFriend;
                })
                .collect(toList());

        log.info("Текущее количество общих друзей пользователя с id=>{} и пользователя с id=>{} => {}"
                , userId, friendId, commonFriends.size());

        return commonFriends;
    }


    private void checkUserFriendById(User user, long friendId, boolean param) {

        if (param) {

            if (user.getFriendsIds().contains(friendId)) {
                throw new ConflictException("У пользователя с id=>" + user.getId()
                        + " уже существует друг id=>" + friendId);
            }

        } else {

            if (!user.getFriendsIds().contains(friendId)) {
                throw new NotFoundException("У пользователя с id=>" + user.getId()
                        + " не существует друга c id=>" + friendId);
            }
        }
    }

    private void checkFriendsByUser(User user) {

        if (user.getFriendsIds().isEmpty()) {
            throw new ConflictException("У пользователя с id=>" + user.getId() + " еще нет друзей");
        }
    }

    public void checkUserById(long userId) {

        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Такой пользователь c id=>" + userId + " не существует");
        }
    }

    public void checkUserLogin(String newUserLogin) {

        final long existentId = userStorage.getIdOnLogin(newUserLogin);

        if (existentId != 0) {
            throw new ConflictException("Такой пользователь с login: " + newUserLogin
                    + " уже существует, для обновления используй PUT запрос");
        }
    }

    public void checkUserEmail(String newUserEmail) {

        final long existentId = userStorage.getIdOnEmail(newUserEmail);

        if (existentId != 0) {
            throw new ConflictException("Такой пользователь с email:" + newUserEmail
                    + " уже существует, по id=> " + existentId + " для обновления используй PUT запрос");
        }
    }

    public void checkUserIdOnLogin(String updateUserLogin, long updateUserId) {

        final long existentId = userStorage.getIdOnLogin(updateUserLogin);

        if (existentId != updateUserId & existentId != 0) {
            throw new ConflictException("Такой пользователь с login: "
                    + updateUserLogin + " уже существует, по id=>" + existentId);
        }

    }

    public void checkUserIdOnEmail(String updateUserEmail, long updateUserId) {

        final long existentId = userStorage.getIdOnEmail(updateUserEmail);

        if (existentId != updateUserId & existentId != 0) {
            throw new ConflictException("Такой пользователь с email: " + updateUserEmail
                    + " уже существует, по id=>" + existentId);
        }
    }
}