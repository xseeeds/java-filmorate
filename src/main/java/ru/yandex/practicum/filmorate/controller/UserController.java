package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @GetMapping
    public ResponseEntity<Collection<User>> allUser() {
        return userService.getAllUser();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable @Positive int userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<User> addFriends(@PathVariable @Positive int userId,
                                           @PathVariable @Positive int friendId) {
        return userService.addFriends(userId, friendId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<User> removeUserById(@PathVariable @Positive int userId) {
        return userService.removeUserById(userId);
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllUser() {
        return userService.removeAllUser();
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<User> removeFriends(@PathVariable @Positive int userId,
                                              @PathVariable @Positive int friendId) {
        return userService.removeFriends(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<Collection<User>> getFriends(@PathVariable @Positive int userId) {
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> getCommonFriends(@PathVariable @Positive int userId,
                                                             @PathVariable @Positive int otherId) {
        return userService.getCommonFriends(userId, otherId);
    }
}
