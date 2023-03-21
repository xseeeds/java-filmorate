package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private User userBuilder;
    private Integer globalId = 0;


    public ResponseEntity<User> addUser(User user) throws ResponseStatusException {

        if (user.getId() != 0) {

            log.error("POST request. Для обновления используй PUT запрос, user имеет id!!! => {}", user);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "POST request. Для обновления используй PUT запрос");
        }

        userStorage.checkUserLogin(user.getLogin());
        userStorage.checkUserEmail(user.getEmail());

        if (user.getName() == null || user.getName().isBlank()) {
            userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .id(getNextId())
                    .build();
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            userBuilder = user
                    .toBuilder()
                    .id(getNextId())
                    .build();
        }

        userStorage.userAddOrUpdate(userBuilder);

        log.info("newUser {}", userBuilder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userBuilder);
    }


    public ResponseEntity<User> updateUser(User newUser) throws ResponseStatusException {

        if (newUser.getId() == 0) {

            log.error("PUT request. Для обновления используй id!!! в теле запроса newUser => {}", newUser);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PUT request. Для обновления используй id в теле запроса");
        }

        userStorage.checkUserById(newUser.getId());
        userStorage.checkUserIdOnLogin(newUser.getLogin(), newUser.getId());
        userStorage.checkUserIdOnEmail(newUser.getEmail(), newUser.getId());

        final User oldUser = userStorage.getUserById(newUser.getId());

        userStorage.removeOldIdByEmail(oldUser.getEmail());
        userStorage.removeOldIdByLogin(oldUser.getLogin());

        if (newUser.getName() == null) {
            userBuilder = newUser
                    .toBuilder()
                    .name(newUser.getLogin())
                    .build();
        }

        if (newUser.getName() != null) {
            userBuilder = newUser
                    .toBuilder()
                    .build();
        }

        userStorage.userAddOrUpdate(userBuilder);

        log.info("Пользователь обновлен {}", userBuilder);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userBuilder);
    }

    public ResponseEntity<Collection<User>> getAllUser() {

        final Collection<User> allUser = userStorage.getAllUser();

        log.info("Текущее количество пользователей : {}", allUser.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(allUser);
    }

    public ResponseEntity<User> getUserById(int userId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        log.info("Пользователь получен : {}", user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    public ResponseEntity<String> removeAllUser() {

        userStorage.removeAllUser();
        resetGlobalId();
        log.info("Все пользователи удалены.");

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body("205 (RESET_CONTENT) Все пользователи удалены.");
    }

    public ResponseEntity<User> removeUserById(int userId) throws ResponseStatusException {

        User user = userStorage.removeUserById(userId);

        log.info("Пользователь с {} удален.", user);

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body(user);
    }

    public ResponseEntity<User> addFriends(int userId, int friendId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        checkUserFriendById(user, friendId, true);
        checkUserFriendById(friend, userId, true);

        user.getFriendsIds().add(friendId);
        friend.getFriendsIds().add(userId);

        userStorage.userAddOrUpdate(user);
        userStorage.userAddOrUpdate(friend);

        log.info("Friend с id {} добавлен пользователю: {}", friendId, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    public ResponseEntity<User> removeFriends(int userId, int friendId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        checkUserFriendById(user, friendId, false);
        checkUserFriendById(friend, userId, false);

        user.getFriendsIds().remove(friendId);
        friend.getFriendsIds().remove(userId);

        userStorage.userAddOrUpdate(user);
        userStorage.userAddOrUpdate(friend);

        log.info("Friend с id {} удален от пользователя: {}", friendId, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(user);
    }

    public ResponseEntity<Collection<User>> getFriends(int userId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        Collection<User> allFriends = new ArrayList<>();

        try {
            checkFriendsByUser(user);

        } catch (ResponseStatusException e) {

            log.info(e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(allFriends);
        }

        allFriends = user
                .getFriendsIds()
                .stream()
                .map(id -> {
                    User friend;
                    try {
                        friend = userStorage.getUserById(id);
                    } catch (ResponseStatusException e) {
                        return null;
                    }
                    return friend;
                })
                .collect(toList());

        log.info("Текущее количество друзей пользователя с id=>{}; =>{}", userId, allFriends.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(allFriends);
    }

    public ResponseEntity<Collection<User>> getCommonFriends(int userId, int friendId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        Collection<User> commonFriends = new ArrayList<>();

        try {
            checkFriendsByUser(user);
            checkFriendsByUser(friend);

        } catch (ResponseStatusException e) {

            log.info(e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(commonFriends);
        }

        commonFriends = user
                .getFriendsIds()
                .stream()
                .filter(friend.getFriendsIds()::contains)
                .map(id -> {
                    User commonFriend;
                    try {
                        commonFriend = userStorage.getUserById(id);
                    } catch (ResponseStatusException e) {
                        return null;
                    }
                    return commonFriend;
                })
                .collect(toList());

        log.info("Текущее количество общих друзей пользователя с id=>{} и пользователя с id=>{} => {}"
                , userId, friendId, commonFriends.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commonFriends);
    }

    private void checkUserFriendById(User user, int friendId, boolean param) {

        if (param) {

            if (user.getFriendsIds().contains(friendId)) {

                log.error("У пользователя с id=>{} уже существует друг id=>{}", user.getId(), friendId);

                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "У пользователя с id=>" + user.getId() + " уже существует друг id=>" + friendId);
            }

        } else {

            if (!user.getFriendsIds().contains(friendId)) {

                log.error("У пользователя с id=>{} не существует друга id=>{}", user.getId(), friendId);

                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "У пользователя с id=>" + user.getId() + " не существует друга id=>" + friendId);
            }
        }
    }

    private void checkFriendsByUser(User user) {

        if (user.getFriendsIds().isEmpty()) {

            log.error("У пользователя с id=>{} еще нет друзей", user.getId());

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "У пользователя с id=>" + user.getId() + " еще нет друзей");
        }
    }

    private Integer getNextId() {
        return ++globalId;
    }

    private void resetGlobalId() {
        globalId = 0;
    }
}