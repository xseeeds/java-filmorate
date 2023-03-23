package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    void userAddOrUpdate(User user);

    User getUserById(int userId);

    Collection<User> getAllUser();

    void removeAllUser();

    User removeUserById(int userId);

    int getIdOnLogin(String userLogin);

    int getIdOnEmail(String userEmail);

    void removeOldIdByLogin(String userLogin);

    void removeOldIdByEmail(String userEmail);

}