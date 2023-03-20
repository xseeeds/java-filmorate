package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    void userAddOrUpdate(User user);

    User getUserById(int userId);

    Collection<User> getAllUser();

    void removeAllUser();

    User removeUserById(int userId);

    void checkUserLogin(String userLogin);

    void checkUserEmail(String userEmail);

    void checkUserById(int userId);

    void checkUserIdOnLogin(String newUserLogin, int newUserId);

    void checkUserIdOnEmail(String newUserEmail, int newUserId);

    void removeOldIdByLogin(String userLogin);

    void removeOldIdByEmail(String userEmail);

}