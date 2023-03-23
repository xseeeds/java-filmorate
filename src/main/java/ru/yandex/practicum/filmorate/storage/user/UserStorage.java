package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    void userAddOrUpdate(User user);

    User getUserById(int userId);

    Collection<User> getAllUser();

    void removeAllUser();

    User removeUserById(int userId);

    void checkUserLogin(String newUserLogin);

    void checkUserEmail(String newUserEmail);

    void checkUserById(int userId);

    void checkUserIdOnLogin(String updateUserLogin, int updateUserId);

    void checkUserIdOnEmail(String updateUserEmail, int updateUserId);

    void removeOldIdByLogin(String userLogin);

    void removeOldIdByEmail(String userEmail);

}