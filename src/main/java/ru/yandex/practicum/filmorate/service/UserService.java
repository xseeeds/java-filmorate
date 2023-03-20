package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toCollection;

@Service
@Slf4j
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private User userBuilder;
    private Integer globalId = 0;


    public ResponseEntity<Map<String, User>> addUser(User user) throws ResponseStatusException {

        if (user.getId() != 0) {

            log.error("POST request. Для обновления используй PUT запрос, user имеет id!!! => {}", user);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "POST request. Для обновления используй PUT запрос");
        }

        userStorage.checkUserLogin(user.getLogin());
        userStorage.checkUserEmail(user.getEmail());

        if (user.getName() == null) {
            userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .id(getNextId())
                    .build();
        }

        if (user.getId() == 0 & user.getName() != null) {
            userBuilder = user
                    .toBuilder()
                    .id(getNextId())
                    .build();
        }

        userStorage.userAddOrUpdate(userBuilder);

        log.info("newUser {}", userBuilder);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("Пользователь создан", userBuilder));
    }


    public ResponseEntity<Map<String, User>> updateUser(User newUser) throws ResponseStatusException {

        if (newUser.getId() == 0) {

            log.error("PUT request. Для обновления используй id!!! в теле запроса newUser => {}", newUser);

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "PUT request. Для обновления используй id в теле запроса");
        }

        userStorage.checkUserById(newUser.getId());
        userStorage.checkUserIdOnLogin(newUser.getLogin(), newUser.getId());
        userStorage.checkUserIdOnEmail(newUser.getEmail(), newUser.getId());

        final User oldUser = userStorage.getUserById(newUser.getId());

        userStorage.removeOldIdByEmail(oldUser.getEmail());
        userStorage.removeOldIdByLogin(oldUser.getLogin());

        if (newUser.getName() == null) {
            userBuilder = newUser
                    .toBuilder()
                    .name(newUser.getLogin())
                    .build();
        }

        if (newUser.getName() != null) {
            userBuilder = newUser
                    .toBuilder()
                    .build();
        }

        userStorage.userAddOrUpdate(userBuilder);

        log.info("Пользователь обновлен {}", userBuilder);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Пользователь обновлен", userBuilder));
    }

    public ResponseEntity<Map<String, Collection<User>>> getAllUser() {

        final Collection<User> allUser = userStorage.getAllUser();

        log.info("Текущее количество пользователей : {}", allUser.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("All users=>", allUser));
    }

    public ResponseEntity<Map<String, User>> getUserById(int userId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        log.info("Пользователь получен : {}", user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Запрос пользователя по id ", user));
    }

    public ResponseEntity<String> removeAllUser() {

        userStorage.removeAllUser();
        resetGlobalId();
        log.info("Все пользователи удалены.");

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body("Все пользователи удалены.");
    }

    public ResponseEntity<Map<String, User>> removeUserById(int userId) throws ResponseStatusException {

        User user = userStorage.removeUserById(userId);

        log.info("Пользователь с {} удален.", user);

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body(Map.of("Пользователь удален", user));
    }

    public ResponseEntity<Map<String, Map<User, User>>> addFriends(int userId, int friendId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        checkFriendById(user, friendId);
        checkFriendById(friend, userId);

        user.getFriendsIds().add(friendId);
        friend.getFriendsIds().add(userId);
//        user.addFriendId(friendId);
//        friend.addFriendId(userId);

        userStorage.userAddOrUpdate(user);
        userStorage.userAddOrUpdate(friend);

        log.info("Friend с id {} добавлен пользователю: {}", friendId, user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Friend с id=>" + friendId
                        + " добавлен пользователю с id=>" + userId
                        , Map.of(user, friend)));
    }

    public ResponseEntity<Map<String, Map<User, User>>> removeFriends(int userId, int friendId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        checkFriendById(user, friendId);
        checkFriendById(friend, userId);

        user.getFriendsIds().remove(friendId);
        friend.getFriendsIds().remove(userId);
//        user.removeFriendId(friendId);
//        friend.removeFriendId(userId);

        userStorage.userAddOrUpdate(user);
        userStorage.userAddOrUpdate(friend);

        log.info("Friend с id {} удален от пользователя: {}", friendId, user);

        return ResponseEntity
                .status(HttpStatus.RESET_CONTENT)
                .body(Map.of("Friend с id=>" + friendId
                                + " удален у пользователя с id=>" + userId
                        , Map.of(user, friend)));
    }

    public ResponseEntity<Map<String, Collection<User>>> getFriends(int userId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        final Collection<User> allFriends = user
                .getFriendsIds()
                .stream()
                .map(id -> userStorage.getUserById(id))
                .collect(toCollection(ArrayList::new));

        log.info("Текущее количество друзей пользователя с id=>{}; =>{}", userId, allFriends.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Друзья пользователя с id=>" + userId
                        + "; =>", allFriends));
    }

    public ResponseEntity<Map<String, Collection<User>>> getCommonFriends(int userId, int friendId) throws ResponseStatusException {

        final User user = userStorage.getUserById(userId);

        final User friend = userStorage.getUserById(friendId);

        Collection<User> commonFriends = user
                .getFriendsIds()
                .stream()
                .filter(friend.getFriendsIds()::contains)
                .map(id -> userStorage.getUserById(id))
                .collect(toCollection(ArrayList::new));

        log.info("Текущее количество общих друзей пользователя с id=>{} и пользователя с id=>{} => {}"
                , userId, friendId, commonFriends.size());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("Общие друзья пользователя с id=>" + userId
                        + " и пользователя с id=>" + friendId
                        + "; =>", commonFriends));
    }

    private void checkFriendById(User user, int friendId) {

        if (user.getFriendsIds().contains(friendId)) {

            log.error("У пользователя с id=>{} уже существует друг id=>{}", user.getId(), friendId);

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "У пользователя с id=>" + user.getId() + " уже существует друг id=>" + friendId);
        }
    }

    private Integer getNextId() {
        return ++globalId;
    }

    private void resetGlobalId() {
        globalId = 0;
    }
}
    /*Создайте UserService, который будет отвечать за такие операции с пользователями,
        как добавление в друзья, удаление из друзей, вывод списка общих друзей.
        Пока пользователям не надо одобрять заявки в друзья — добавляем сразу.
        То есть если Лена стала другом Саши, то это значит, что Саша теперь друг Лены.
        Создайте FilmService, который будет отвечать за операции с фильмами,
        — добавление и удаление лайка, вывод 10 наиболее популярных фильмов по количеству лайков.
        Пусть пока каждый пользователь может поставить лайк фильму только один раз.
        Добавьте к ним аннотацию @Service — тогда к ним можно будет получить доступ из контроллера.*/

    /*Дальше стоит заняться контроллерами и довести API до соответствия REST.
        С помощью аннотации @PathVariable добавьте возможность получать
        каждый фильм и данные о пользователях по их уникальному идентификатору: GET .../users/{id}.
        Добавьте методы, позволяющие пользователям добавлять друг друга в друзья,
        получать список общих друзей и лайкать фильмы. Проверьте, что все они работают корректно.

        PUT /users/{id}/friends/{friendId} — добавление в друзья.
        DELETE /users/{id}/friends/{friendId} — удаление из друзей.
        GET /users/{id}/friends — возвращаем список пользователей, являющихся его друзьями.
        GET /users/{id}/friends/common/{otherId} — список друзей, общих с другим пользователем.
        PUT /films/{id}/like/{userId} — пользователь ставит лайк фильму.
        DELETE /films/{id}/like/{userId} — пользователь удаляет лайк.
        GET /films/popular?count={count} — возвращает список из первых count фильмов по количеству лайков. Если значение параметра count не задано, верните первые 10.

        Убедитесь, что ваше приложение возвращает корректные HTTP-коды.
        400 — если ошибка валидации: ValidationException;
        404 — для всех ситуаций, если искомый объект не найден;
        500 — если возникло исключение.*/