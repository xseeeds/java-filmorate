package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.NoParameterException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collection;
import java.util.Optional;


@RestController
@RequestMapping("/users")
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
    public ResponseEntity<User> getUserById(@PathVariable @Valid @Positive Optional<Integer> userId) {

        if (userId.orElseThrow(() -> new NoParameterException("userId")) <= 0) {
            throw new NoParameterException("userId");
        }
        return userService.getUserById(userId.get());
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<User> addFriends(@PathVariable @Valid @Positive Optional<Integer> userId,
                                           @PathVariable @Valid @Positive Optional<Integer> friendId) {

        if (userId.orElseThrow(() -> new NoParameterException("userId")) <= 0) {
            throw new NoParameterException("userId");
        }
        if (friendId.orElseThrow(() -> new NoParameterException("friendId")) <= 0) {
            throw new NoParameterException("friendId");
        }
        return userService.addFriends(userId.get(), friendId.get());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<User> removeUserById(@PathVariable @Valid @Positive Optional<Integer> userId) {

        if (userId.orElseThrow(() -> new NoParameterException("userId")) <= 0) {
            throw new NoParameterException("userId");
        }
        return userService.removeUserById(userId.get());
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllUser() {
        return userService.removeAllUser();
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<User> removeFriends(@PathVariable @Valid @Positive Optional<Integer> userId,
                                              @PathVariable @Valid @Positive Optional<Integer> friendId) {

        if (userId.orElseThrow(() -> new NoParameterException("userId")) <= 0) {
            throw new NoParameterException("userId");
        }
        if (friendId.orElseThrow(() -> new NoParameterException("friendId")) <= 0) {
            throw new NoParameterException("friendId");
        }
        return userService.removeFriends(userId.get(), friendId.get());
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<Collection<User>> getFriends(@PathVariable @Valid @Positive Optional<Integer> userId) {

        if (userId.orElseThrow(() -> new NoParameterException("userId")) <= 0) {
            throw new NoParameterException("userId");
        }
        return userService.getFriends(userId.get());
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public ResponseEntity<Collection<User>> getCommonFriends(@PathVariable @Valid @Positive Optional<Integer> userId,
                                                             @PathVariable @Valid @Positive Optional<Integer> otherId) {

        if (userId.orElseThrow(() -> new NoParameterException("userId")) <= 0) {
            throw new NoParameterException("userId");
        }
        if (otherId.orElseThrow(() -> new NoParameterException("otherId")) <= 0) {
            throw new NoParameterException("otherId");
        }
        return userService.getCommonFriends(userId.get(), otherId.get());
    }
}
