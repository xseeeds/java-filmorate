package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class UserFriends {
    private int id;
    private int friendsId;
    private Status status;
}