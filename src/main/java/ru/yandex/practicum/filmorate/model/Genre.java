package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Genre {

    Comedy("Комедия", 1),

    Drama("Драма", 2),

    Cartoon("Мультфильм", 3),

    Thriller("Триллер", 4),

    Documentary("Документальный", 5),

    ActionMovie("Боевик", 6);

    private final String genre;
    private final int id;
}
