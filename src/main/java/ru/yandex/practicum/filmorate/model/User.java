package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;
import nonapi.io.github.classgraph.json.Id;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Set;

@Value
@Builder(toBuilder = true)
public class User {

    @Id
    int id;

    @Email
    String email;

    @NotNull
    @NotBlank
    String login;

    String name;

    @Past
    LocalDate birthday;

    Set<Integer> friendsIds;

//    public void addFriendId(int friendId) {
//        friendsIds.add(friendId);
//    }
//
//    public void removeFriendId(int friendId) {
//        friendsIds.remove(friendId);
//    }
}