package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;


public interface UserStorage {

    User createUser(User user);

    void resetGlobalId();

    User updateUser(User user);

    User getUserById(long userId) throws NotFoundException;

    List<User> getAllUser();

    void removeAllUser();

    void removeUserById(long userId);

    void checkUserFriendById(long userId, long otherId, boolean addOrRemove) throws ConflictException, NotFoundException;

    void checkFriendByUserId(long userId) throws NotFoundException;

    void checkUserByFriendId(long otherId) throws NotFoundException;

    void addFriend(long userId, long otherId, Status status);

    void updateStatusFriendShip(long userId, long otherId, Status status);

    boolean checkStatusFriendship(long userId, long otherId, Status status);

    boolean checkFriendship(long userId, long otherId);

    void removeFriend(long userId, long otherId);

    List<User> getAllFriendsByUserId(long userId);

    List<User> getCommonFriendsByUser(long userId, long otherId);

    void checkUserById(long userId) throws NotFoundException;

    void checkUserLogin(String newUserLogin) throws ConflictException;

    void checkUserEmail(String newUserEmail) throws ConflictException;

    void checkUserIdOnLogin(String updateUserLogin, long updateUserId) throws ConflictException;

    void checkUserIdOnEmail(String updateUserEmail, long updateUserId) throws ConflictException;

    List<Film> getRecommendationsFilmsByUserId(long userId);
}