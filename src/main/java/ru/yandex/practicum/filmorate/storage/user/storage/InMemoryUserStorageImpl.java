package ru.yandex.practicum.filmorate.storage.user.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.TreeMap;

import static java.util.stream.Collectors.toList;


@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InMemoryUserStorageImpl implements UserStorage {
    private final UserStorage.OnCreate inMemoryUserStorageImplOnCreate;
    protected static final TreeMap<Long, User> users = new TreeMap<>();
    protected static final TreeMap<String, Long> userEmails = new TreeMap<>();
    protected static final TreeMap<String, Long> userLogins = new TreeMap<>();


    @Override
    public User getUserById(long userId) {

        final User user = users.get(userId);

        if (user == null) {
            throw new NotFoundException("Такой пользователь c id => " + userId + " не существует");
        }

        return user;
    }

    @Override
    public Collection<User> getAllUser() {

        return users.values();
    }

    @Override
    public void removeAllUser() {

        users.clear();
        userLogins.clear();
        userEmails.clear();
        inMemoryUserStorageImplOnCreate.resetGlobalId();

    }

    @Override
    public void removeUserById(long userId) {

        final User user = users.remove(userId);
        userLogins.remove(user.getLogin());
        userEmails.remove(user.getEmail());
    }

    @Override
    public void checkUserFriendById(long userId, long otherId, boolean addOrRemove) throws ConflictException, NotFoundException {

        final User user = users.get(userId);

        if (addOrRemove) {

            if (user.getFriendsIdsStatus().containsKey(otherId) && user.getFriendsIdsStatus().get(otherId) == Status.FRIENDSHIP) {
                throw new ConflictException("У пользователя с id => " + user.getId()
                        + " уже существует дружба с id => " + otherId);
            }

        } else {

            if (!user.getFriendsIdsStatus().containsKey(otherId)) {
                throw new NotFoundException("У пользователя с id => " + user.getId()
                        + " не существует друга/заявки/подписки c id => " + otherId);
            }
        }
    }

    @Override
    public void checkFriendByUserId(long userId) throws ConflictException {

        final User user = users.get(userId);

        if (user != null && user.getFriendsIdsStatus().isEmpty()) {
            throw new ConflictException("У пользователя с id => " + user.getId() + " нет друзей");
        }
    }

    @Override
    public void checkUserByFriendId(long otherId) throws ConflictException {

        final User user = users.get(otherId);

        if (user != null && user.getFriendsIdsStatus().isEmpty()) {
            throw new ConflictException("У пользователя с id => " + user.getId() + " нет друзей");
        }
    }

    @Override
    public void addFriend(long userId, long otherId, Status status) {
        users.get(userId).getFriendsIdsStatus().put(otherId, status);
    }

    @Override
    public void updateStatusFriendShip(long userId, long otherId, Status status) {
        users.get(userId).getFriendsIdsStatus().put(otherId, status);
    }

    @Override
    public void removeFriend(long userId, long otherId) {
        users.get(userId).getFriendsIdsStatus().remove(otherId);
    }

    @Override
    public Collection<User> getAllFriendsByUserId(long userId) {

        return users.get(userId)
                .getFriendsIdsStatus()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(Status.FRIENDSHIP))
                .map(entry -> users.get(
                        entry.getKey()))
                .collect(toList());
    }

    @Override
    public Collection<User> getCommonFriendsByUser(long userId, long otherUserId) {

        return users.get(userId)
                .getFriendsIdsStatus()
                .entrySet()
                .stream()
                .filter(entry -> users.get(otherUserId).getFriendsIdsStatus().containsKey(entry.getKey())
                        && entry.getValue().equals(Status.FRIENDSHIP))
                .map(entry -> users.get(
                        entry.getKey()))
                .collect(toList());
    }

    @Override
    public void checkUserById(long userId) throws NotFoundException {

        if (!users.containsKey(userId)) {
            throw new NotFoundException("Такой пользователь c id => " + userId + " не существует");
        }
    }

    @Override
    public void checkUserLogin(String newUserLogin) throws ConflictException {

        final Long existentId = userLogins.get(newUserLogin);

        if (existentId != null) {
            throw new ConflictException("Такой пользователь с login: " + newUserLogin
                    + " уже существует, по id => " + existentId + " для обновления используй PUT запрос");
        }
    }

    @Override
    public void checkUserEmail(String newUserEmail) throws ConflictException {

        final Long existentId = userEmails.get(newUserEmail);

        if (existentId != null) {
            throw new ConflictException("Такой пользователь с email:" + newUserEmail
                    + " уже существует, по id => " + existentId + " для обновления используй PUT запрос");
        }
    }

    @Override
    public void checkUserIdOnLogin(String updateUserLogin, long updateUserId) throws ConflictException {

        final Long existentId = userLogins.get(updateUserLogin);

        if (existentId != null && existentId != updateUserId) {
            throw new ConflictException("Такой пользователь с login: " + updateUserLogin
                    + " уже существует, по id => " + existentId);
        }

    }

    @Override
    public void checkUserIdOnEmail(String updateUserEmail, long updateUserId) throws ConflictException {

        final Long existentId = userEmails.get(updateUserEmail);

        if (existentId != null && existentId != updateUserId) {
            throw new ConflictException("Такой пользователь с email: " + updateUserEmail
                    + " уже существует, по id => " + existentId);
        }
    }
}
