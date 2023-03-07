package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
public class InMemoryUserManager implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private final HashSet<String> userEmails = new HashSet<>();
    private final HashSet<String> userLogins = new HashSet<>();

    @Override
    public ResponseEntity<User> addUser(User user) {

        if (user.getId() != 0) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "POST request. Для обновления используй PUT запрос");
        }

        if (userLogins.contains(user.getLogin())) {

            log.error("Такой пользователь с login: {} уже существует, для обновления используй PUT запрос", user.getLogin());

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с login:"
                            + user.getLogin()
                            + " уже существует, для обновления используй PUT запрос");
        }

        if (userEmails.contains(user.getEmail())) {

            log.error("Такой пользователь с email: {} уже существует, для обновления используй PUT запрос", user.getEmail());

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Такой пользователь с email:"
                            + user.getEmail()
                            + " уже существует, для обновления используй PUT запрос");
        }

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        user.setId(users.size() + 1);

        users.put(user.getId(), user);
        userEmails.add(user.getEmail());
        userLogins.add(user.getLogin());

        log.info("user {}", user);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Override
    public ResponseEntity<User> updateUser(User newUser) {

        if (newUser.getId() == 0) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PUT request. Для обновления используй id в теле запроса");
        }

        if (users.containsKey(newUser.getId())) {

            if (newUser.getName() == null) {
                newUser.setName(newUser.getLogin());
            }
            final User oldUser = users.get(newUser.getId());

            userEmails.remove(oldUser.getEmail());
            userLogins.remove(oldUser.getLogin());

            users.put(newUser.getId(), newUser);
            userEmails.add(newUser.getEmail());
            userLogins.add(newUser.getLogin());

            log.info("user {}", newUser);

            return ResponseEntity.status(HttpStatus.OK).body(newUser);

        }
        log.error("Такой пользователь: {} не существует", newUser);

        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Такой пользователь: " + newUser + " не существует");
    }

    @Override
    public Collection<User> getAllUser() {

        log.info("Текущее количество пользователей : {}", users.size());

        return users.values();
    }


    @Override
    public ResponseEntity<String> removeAllUser() {
        users.clear();
        userLogins.clear();
        userEmails.clear();

        log.info("Все пользователи удалены.");

        return ResponseEntity.status(HttpStatus.RESET_CONTENT).body("Все пользователи удалены.");
    }
}
