package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MPA {

    G("G", 1),

    PG("PG", 2),

    PG13("PG-13", 3),

    R("R", 4),

    NC17("NC-17", 5);

    private String mpa;
    private int id;
}
