package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.service.UserService;


import java.util.Collection;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody User user) {
        if (user.getId() != null) {
            throw new BadRequestException("POST request. Для обновления используй PUT запрос, user имеет id!!!");
        }
        return userService.createUser(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@RequestBody User user) {
        if (user.getId() == null) {
            throw new BadRequestException("PUT request. Для обновления используй id!!! в теле запроса user");
        }
        return userService.updateUser(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> allUser() {
        return userService.getAllUser();
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public User getUserById(@PathVariable long userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping
    public ResponseEntity<String> removeAllUser() {
        return ResponseEntity.ok(userService.removeAllUser());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> removeUserById(@PathVariable long userId) {
        return ResponseEntity.ok(userService.removeUserById(userId));
    }

    @PutMapping("/{userId}/friends/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public void addFriend(@PathVariable long userId, @PathVariable long otherId) {
        userService.addFriend(userId, otherId);
    }

    @DeleteMapping("/{userId}/friends/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFriend(@PathVariable long userId, @PathVariable long otherId) {
        userService.removeFriend(userId, otherId);
    }

    @GetMapping("/{userId}/friends")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> getFriends(@PathVariable long userId) {
        return userService.getAllFriendsByUser(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> getCommonFriends(@PathVariable long userId, @PathVariable long otherId) {
        return userService.getCommonFriendsByUser(userId, otherId);
    }
}