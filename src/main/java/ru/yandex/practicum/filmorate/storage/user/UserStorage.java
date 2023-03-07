package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    ResponseEntity<User> addUser(User user);
    ResponseEntity<User> updateUser(User user);
    Collection<User> getAllUser();
    ResponseEntity<String> removeAllUser();
}