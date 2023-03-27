package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;


public interface UserStorage {

    interface OnCreate {
        User createUser(User user);
        void resetGlobalId();
    }

    interface OnUpdate {
        User updateUser(User user);
    }


    User getUserById(long userId);

    Collection<User> getAllUser();

    void removeAllUser();

    void removeUserById(long userId);

    long getIdOnLogin(String userLogin);

    long getIdOnEmail(String userEmail);

    void removeOldIdByLogin(String userLogin);

    void removeOldIdByEmail(String userEmail);

}