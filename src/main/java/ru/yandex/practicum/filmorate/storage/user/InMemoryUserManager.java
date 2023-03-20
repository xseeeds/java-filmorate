package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;


@Component
@Slf4j
public class InMemoryUserManager implements UserStorage {
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

            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Такой пользователь c id=>" + userId + " не существует");
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
            users.values().forEach(u -> u.getFriendsIds().remove(userId));

            return user;
        }
        log.error("Такой пользователь с id: {} не существует", userId);

        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Такой user c id=>" + userId + " не существует");
    }

    @Override
    public void checkUserLogin(String userLogin) {

        if (userLogins.containsKey(userLogin)) {

            log.error("Такой пользователь с login: {} уже существует, для обновления используй PUT запрос", userLogin);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с login:"
                            + userLogin
                            + " уже существует, для обновления используй PUT запрос");
        }
    }

    @Override
    public void checkUserEmail(String userEmail) {

        if (userEmails.containsKey(userEmail)) {

            log.error("Такой пользователь с email: {} уже существует, для обновления используй PUT запрос", userEmail);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с email:"
                            + userEmail
                            + " уже существует, для обновления используй PUT запрос");

        }
    }

    @Override
    public void checkUserById(int userId) {

        if (users.containsKey(userId)) {

            log.error("Такой пользователь c id=>{} не существует", userId);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Такой пользователь c id=>" + userId + " не существует");
        }
    }

    @Override
    public void checkUserIdOnLogin(String newUserLogin, int newUserId) {

        final int existentId = userLogins.getOrDefault(newUserLogin, 0);

        if (existentId != newUserId & existentId != 0) {

            log.error("Такой пользователь с login: {} уже существует, по id=>{}", newUserLogin, existentId);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с login: "
                            + newUserLogin
                            + " уже существует, по id=>" + existentId);
        }

    }

    @Override
    public void checkUserIdOnEmail(String newUserEmail, int newUserId) {

        int existentId = userEmails.getOrDefault(newUserEmail, 0);

        if (existentId != newUserId & existentId != 0) {

            log.error("Такой пользователь с email: {} уже существует, по id=>{}", newUserEmail, existentId);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с email:"
                            + newUserEmail
                            + " уже существует, по id=>" + existentId);
        }
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
