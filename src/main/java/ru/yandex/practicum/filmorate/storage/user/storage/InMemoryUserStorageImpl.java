package ru.yandex.practicum.filmorate.storage.user.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static java.util.stream.Collectors.toList;
import static ru.yandex.practicum.filmorate.model.Status.FRIENDSHIP;
import static ru.yandex.practicum.filmorate.model.Status.SUBSCRIPTION;


@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InMemoryUserStorageImpl implements UserStorage {
    private final TreeMap<Long, User> users = new TreeMap<>();
    private final TreeMap<String, Long> userEmails = new TreeMap<>();
    private final TreeMap<String, Long> userLogins = new TreeMap<>();
    private long globalId = 0;

    @Override
    public User createUser(User user) {

        final User userBuilder;

        if (user.getName() == null || user.getName().isBlank()) {
            userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .id(getNextId())
                    .build();
        } else {
            userBuilder = user
                    .toBuilder()
                    .id(getNextId())
                    .build();
        }

        users.put(userBuilder.getId(), userBuilder);
        userEmails.put(userBuilder.getEmail(), userBuilder.getId());
        userLogins.put(userBuilder.getLogin(), userBuilder.getId());

        return userBuilder;
    }

    @Override
    public void resetGlobalId() {
        globalId = 0;
    }

    @Override
    public User updateUser(User user) {

        final User oldUser = users.get(user.getId());
        userEmails.remove(oldUser.getEmail());
        userLogins.remove(oldUser.getLogin());

        if (user.getName() == null) {
            final User userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .build();
            users.put(userBuilder.getId(), userBuilder);
            userEmails.put(userBuilder.getEmail(), userBuilder.getId());
            userLogins.put(userBuilder.getLogin(), userBuilder.getId());

            return userBuilder;
        }

        users.put(user.getId(), user);
        userEmails.put(user.getEmail(), user.getId());
        userLogins.put(user.getLogin(), user.getId());

        return user;
    }

    @Override
    public User getUserById(long userId) throws NotFoundException {

        final User user = users.get(userId);

        if (user == null) {
            throw new NotFoundException("Такой пользователь c id => " + userId + " не существует");
        }

        return user;
    }

    @Override
    public List<User> getAllUser() {

        return new ArrayList<>(users.values());
    }

    @Override
    public void removeAllUser() {

        users.clear();
        userLogins.clear();
        userEmails.clear();
        resetGlobalId();

    }

    @Override
    public void removeUserById(long userId) {

        final User user = users.remove(userId);

        if (user == null) {
            throw new NotFoundException("Такой пользователь c id => " + userId + " не существует");
        }

        userLogins.remove(user.getLogin());
        userEmails.remove(user.getEmail());
    }

    @Override
    public void checkUserFriendById(long userId, long otherId, boolean addOrRemove) throws ConflictException, NotFoundException {

        final User user = users.get(userId);

        if (addOrRemove) {

            if (user.getFriendsIdsStatus().containsKey(otherId) && user.getFriendsIdsStatus().get(otherId) == FRIENDSHIP) {
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
    public void checkFriendByUserId(long userId) throws NotFoundException {

        final User user = users.get(userId);

        if (user != null && user.getFriendsIdsStatus().isEmpty()) {
            throw new NotFoundException("У пользователя с id => " + user.getId() + " нет друзей");
        }
    }

    @Override
    public void checkUserByFriendId(long otherId) throws NotFoundException {

        final User user = users.get(otherId);

        if (user != null && user.getFriendsIdsStatus().isEmpty()) {
            throw new NotFoundException("У пользователя с id => " + user.getId() + " нет друзей");
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
    public boolean checkStatusFriendship(long userId, long otherId, Status status) {

        final User user = users.get(userId);

        return user.getFriendsIdsStatus().containsKey(otherId)
                && user.getFriendsIdsStatus().get(otherId).equals(status);
    }

    @Override
    public boolean checkFriendship(long userId, long otherId) {
        return users.get(userId).getFriendsIdsStatus().containsKey(otherId);
    }

    @Override
    public void removeFriend(long userId, long otherId) {
        users.get(userId).getFriendsIdsStatus().remove(otherId);
    }

    @Override
    public List<User> getAllFriendsByUserId(long userId) {

        return users.get(userId)
                .getFriendsIdsStatus()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(FRIENDSHIP)
                        || entry.getValue().equals(SUBSCRIPTION))
                .map(entry -> users.get(entry.getKey()))
                .collect(toList());
    }

    @Override
    public List<User> getCommonFriendsByUser(long userId, long otherUserId) {

        return users.get(userId)
                .getFriendsIdsStatus()
                .entrySet()
                .stream()
                .filter(entry -> users.get(otherUserId).getFriendsIdsStatus().containsKey(entry.getKey())
                        && entry.getValue().equals(FRIENDSHIP)
                        || users.get(otherUserId).getFriendsIdsStatus().containsKey(entry.getKey())
                        && entry.getValue().equals(SUBSCRIPTION))
                .map(entry -> users.get(entry.getKey()))
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

    //TODO интересная задачка потренироваться getRecommendationsFilmsByUserId)))

    @Override
    public List<Film> getRecommendationsFilmsByUserId(long userId) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Метод /getRecommendationsFilmsByUserId не реализован.");
    }

    private long getNextId() {
        return ++globalId;
    }
}
