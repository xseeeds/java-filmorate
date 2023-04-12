package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserManager;

import javax.validation.Valid;
import java.util.Collection;


import static ru.yandex.practicum.filmorate.storage.Managers.getDefaultUserManager;


@RestController
public class UserController {
    private final InMemoryUserManager userManager = getDefaultUserManager();

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        return userManager.addUser(user);
    }

    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        return userManager.updateUser(user);
    }

    @GetMapping("/users")
    public Collection<User> allUser() {
        return userManager.getAllUser();
    }

    @DeleteMapping("/users")
    public ResponseEntity<String> removeAllUser() {
        return userManager.removeAllUser();
    }
}
