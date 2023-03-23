package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
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

        log.info("Пользователь добавлен/обновлен =>{}", user);
    }

    @Override
    public User getUserById(int userId) {

        final User user = users.get(userId);

        if (user == null) {
            log.error("Такой пользователь с id: {} не существует", userId);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Такой пользователь c id=>" + userId + " не существует");
        }
        log.info("Пользователь получен =>{}", user);

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

        log.info("Все пользователи удалены");
    }

    @Override
    public User removeUserById(int userId) {

        if (users.containsKey(userId)) {

            final User user = users.remove(userId);

            userLogins.remove(user.getLogin());
            userEmails.remove(user.getEmail());

            users.values().forEach(u -> u.getFriendsIds().remove(userId));

            log.info("Пользователь удален/удален у всех друзей =>{}", user);

            return user;
        }
        log.error("Такой пользователь с id: {} не существует", userId);

        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Такой user c id=>" + userId + " не существует");
    }

    @Override
    public void checkUserLogin(String newUserLogin) {

        if (userLogins.containsKey(newUserLogin)) {

            log.error("Такой пользователь с login: {} уже существует, для обновления используй PUT запрос", newUserLogin);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с login:"
                            + newUserLogin
                            + " уже существует, для обновления используй PUT запрос");
        }
    }

    @Override
    public void checkUserEmail(String newUserEmail) {

        if (userEmails.containsKey(newUserEmail)) {

            log.error("Такой пользователь с email: {} уже существует, по id=>{}," +
                            " для обновления используй PUT запрос", newUserEmail, userEmails.get(newUserEmail));

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с email:"
                            + newUserEmail
                            + " уже существует, по id=> "
                            + userEmails.get(newUserEmail)
                            + " для обновления используй PUT запрос");

        }
    }

    @Override
    public void checkUserById(int userId) {

        if (!users.containsKey(userId)) {

            log.error("Такой пользователь c id=>{} не существует", userId);

            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Такой пользователь c id=>" + userId + " не существует");
        }
    }

    @Override
    public void checkUserIdOnLogin(String updateUserLogin, int updateUserId) {

        final Integer existentId = userLogins.getOrDefault(updateUserLogin, 0);

        if (existentId != updateUserId & existentId != 0) {

            log.error("Такой пользователь с login: {} уже существует, по id=>{}", updateUserLogin, existentId);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с login: "
                            + updateUserLogin
                            + " уже существует, по id=>" + existentId);
        }

    }

    @Override
    public void checkUserIdOnEmail(String updateUserEmail, int updateUserId) {

        final int existentId = userEmails.getOrDefault(updateUserEmail, 0);

        if (existentId != updateUserId & existentId != 0) {

            log.error("Такой пользователь с email: {} уже существует, по id=>{}", updateUserEmail, existentId);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с email:"
                            + updateUserEmail
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
