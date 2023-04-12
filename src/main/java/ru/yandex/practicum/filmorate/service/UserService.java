package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Status;
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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserStorage userStorage;
    private final UserStorage.OnCreate userStorageOnCreate;
    private final UserStorage.OnUpdate userStorageOnUpdate;

    private User user;
    private User friend;


    @Validated({UserStorage.OnCreate.class, UserStorage.class})
    public User createUser(@Valid User createdUser) throws ConflictException {

        checkUserLogin(createdUser.getLogin());
        checkUserEmail(createdUser.getEmail());

        user = userStorageOnCreate.createUser(createdUser);

        log.info("newUser {}", user);

        return user;
    }


    @Validated({UserStorage.OnUpdate.class, UserStorage.class})
    public User updateUser(@Valid User updatedUser) throws NotFoundException, ConflictException {

        checkUserById(updatedUser.getId());
        checkUserIdOnLogin(updatedUser.getLogin(), updatedUser.getId());
        checkUserIdOnEmail(updatedUser.getEmail(), updatedUser.getId());

        user = userStorageOnUpdate.updateUser(updatedUser);

        log.info("Пользователь обновлен {}", user);

        return user;
    }

    public Collection<User> getAllUser() {

        final Collection<User> allUser = userStorage.getAllUser();

        log.info("Текущее количество пользователей : {}", allUser.size());

        return allUser;
    }

    public User getUserById(@Positive long userId) throws NotFoundException {

        user = userStorage.getUserById(userId);

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

        //userStorage.getAllUser().forEach(user -> user.getFriendsIdsStatus().put(userId, Status.SUBSCRIPTION));

        //userStorage.getAllUser().forEach(u -> u.getFriendsIdsStatus().remove(userId));

        log.info("Пользователь с id=>{} удален"/*удалены все его подписки"*/, userId);

        return "Пользователь c id=>" + userId + "удален"/*удалены все его подписки"*/;
    }

    public User addFriends(@Positive long userId, @Min(-1) long friendId) throws NotFoundException, ConflictException {

        user = userStorage.getUserById(userId);

        friend = userStorage.getUserById(friendId);

        checkUserFriendById(user, friendId, true);
        checkUserFriendById(friend, userId, true);

        if (user.getFriendsIdsStatus().containsKey(friendId) && user.getFriendsIdsStatus().get(friendId) == Status.APPLICATION) {
            user.getFriendsIdsStatus().put(friendId, Status.FRIENDSHIP);
            friend.getFriendsIdsStatus().put(userId, Status.FRIENDSHIP);
            userStorageOnUpdate.updateUser(user);
            userStorageOnUpdate.updateUser(friend);
            log.info("User с id=>{} подтвердил дружбу c пользователем c id=>{}", userId, friendId);
            return user;
        }

        user.getFriendsIdsStatus().put(friendId, Status.SUBSCRIPTION);
        friend.getFriendsIdsStatus().put(userId, Status.APPLICATION);

        userStorageOnUpdate.updateUser(user);
        userStorageOnUpdate.updateUser(friend);

        log.info("Подписчик с id=>{} добавлен пользователю c id=>{} неподтвержденная дружба", friendId, userId);

        return user;
    }

    public User removeFriends(@Positive long userId, @Positive long friendId) throws NotFoundException, ConflictException {

        user = userStorage.getUserById(userId);

        try {
            friend = userStorage.getUserById(friendId);
            checkUserFriendById(user, friendId, false);

        } catch (NotFoundException e) {

            if (user.getFriendsIdsStatus().get(friendId) == Status.SUBSCRIPTION || user.getFriendsIdsStatus().get(friendId) == Status.APPLICATION) {
                user.getFriendsIdsStatus().remove(friendId);
                userStorageOnUpdate.updateUser(user);
                log.info(e.getMessage() + " => Подписчик с id=>{} удален от пользователя c id=>{}", friendId, user);
                return user;
            }
            throw new NotFoundException(e.getMessage());
        }

        if (user.getFriendsIdsStatus().get(friendId) == Status.SUBSCRIPTION || user.getFriendsIdsStatus().get(friendId) == Status.APPLICATION) {

            if (friend.getFriendsIdsStatus().get(userId) != null && friend.getFriendsIdsStatus().get(userId) == Status.APPLICATION) {
                friend.getFriendsIdsStatus().remove(userId);
                userStorageOnUpdate.updateUser(friend);
            }

            user.getFriendsIdsStatus().remove(friendId);
            userStorageOnUpdate.updateUser(user);
            log.info("Подписчик с id=>{} удален от пользователя c id=>{}", friendId, user);
            return user;
        }

        checkUserFriendById(friend, userId, false);

        user.getFriendsIdsStatus().remove(friendId);
        friend.getFriendsIdsStatus().put(userId, Status.SUBSCRIPTION);

        userStorageOnUpdate.updateUser(user);
        userStorageOnUpdate.updateUser(friend);

        log.info("Friend с id=>{} удален от пользователя c id=>{} и оставлен в подписках", friendId, user);

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
                .getFriendsIdsStatus()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(Status.FRIENDSHIP))
                .map(entry -> {
                    User friend;
                    try {
                        friend = userStorage.getUserById(entry.getKey());

                    } catch (NotFoundException e) {

                        return null;
                    }
                    return friend;
                })
                .collect(toList());

        log.info("Текущее количество друзей пользователя с id=>{}; =>{}", userId, allFriends.size());

        return allFriends;
    }

    public Collection<User> getCommonFriends(@Positive long userId, @Positive long otherId) throws NotFoundException, ConflictException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(otherId);

        Collection<User> commonFriends = new ArrayList<>();

        try {
            checkFriendsByUser(user);
            checkFriendsByUser(friend);

        } catch (ConflictException e) {

            log.info(e.getMessage());

            return commonFriends;
        }

        commonFriends = user
                .getFriendsIdsStatus()
                .entrySet()
                .stream()
                .filter(entry -> friend.getFriendsIdsStatus().containsKey(entry.getKey())
                        & entry.getValue().equals(Status.FRIENDSHIP))
                .map(entry -> {
                    User commonFriend;
                    try {

                        commonFriend = userStorage.getUserById(entry.getKey());

                    } catch (NotFoundException e) {

                        return null;
                    }
                    return commonFriend;
                })
                .collect(toList());

        log.info("Текущее количество общих друзей пользователя с id=>{} и пользователя с id=>{} ===> {}",
                userId, otherId, commonFriends.size());

        return commonFriends;
    }


    private void checkUserFriendById(User user, long friendId, boolean param) {

        if (param) {

            if (user.getFriendsIdsStatus().containsKey(friendId) && user.getFriendsIdsStatus().get(friendId) == Status.FRIENDSHIP) {
                throw new ConflictException("У пользователя с id=>" + user.getId()
                        + " уже существует дружба с id=>" + friendId);
            }

        } else {

            if (!user.getFriendsIdsStatus().containsKey(friendId)) {
                throw new NotFoundException("У пользователя с id=>" + user.getId()
                        + " не существует друга/заявки/подписки c id=>" + friendId);
            }
        }
    }

    private void checkFriendsByUser(User user) {

        if (user.getFriendsIdsStatus().isEmpty()) {
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