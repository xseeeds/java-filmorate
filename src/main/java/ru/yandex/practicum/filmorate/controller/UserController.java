package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.model.Response;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collection;


@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> allUser() {
        return userService.getAllUser();
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public User getUserById(@PathVariable @Positive int userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.RESET_CONTENT)
    public Response removeAllUser() {
        return new Response(userService.removeAllUser());
        //Не понимаю почему здесь не приходит ответ с сервера(
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.RESET_CONTENT)
    public User removeUserById(@PathVariable @Positive int userId) {
        return userService.removeUserById(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public User addFriends(@PathVariable @Positive int userId,
                           @PathVariable @Positive int friendId) {
        return userService.addFriends(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public User removeFriends(@PathVariable @Positive int userId,
                              @PathVariable @Positive int friendId) {
        return userService.removeFriends(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> getFriends(@PathVariable @Positive int userId) {
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> getCommonFriends(@PathVariable @Positive int userId,
                                             @PathVariable @Positive int otherId) {
        return userService.getCommonFriends(userId, otherId);
    }
}
