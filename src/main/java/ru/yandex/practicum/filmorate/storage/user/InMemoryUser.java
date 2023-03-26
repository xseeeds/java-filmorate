package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.TreeMap;


@Component
@Slf4j
public class InMemoryUser implements UserStorage {
    private final TreeMap<Integer, User> users = new TreeMap<>();
    private final TreeMap<String, Integer> userEmails = new TreeMap<>();
    private final TreeMap<String, Integer> userLogins = new TreeMap<>();


    @Override
    public void userAddOrUpdate(User user) {
        users.put(user.getId(), user);
        userEmails.put(user.getEmail(), user.getId());
        userLogins.put(user.getLogin(), user.getId());
    }

    @Override
    public User getUserById(int userId) {
        final User user = users.get(userId);
        if (user == null) {
            log.error("Такой пользователь с id: {} не существует", userId);
            throw new NotFoundException("Такой пользователь c id=>" + userId + " не существует");
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
    }

    @Override
    public User removeUserById(int userId) {
        if (users.containsKey(userId)) {
            final User user = users.remove(userId);
            userLogins.remove(user.getLogin());
            userEmails.remove(user.getEmail());
            return user;
        }
        log.error("Такой пользователь с id: {} не существует", userId);
        throw new NotFoundException("Такой user c id=>" + userId + " не существует");
    }

    @Override
    public int getIdOnLogin(String updateUserLogin) {
        return userLogins.getOrDefault(updateUserLogin, 0);
    }

    @Override
    public int getIdOnEmail(String updateUserEmail) {
        return userEmails.getOrDefault(updateUserEmail, 0);
    }

    @Override
    public void removeOldIdByLogin(String userLogin) {
        userLogins.remove(userLogin);
    }

    @Override
    public void removeOldIdByEmail(String userEmail) {
        userEmails.remove(userEmail);
    }
}
