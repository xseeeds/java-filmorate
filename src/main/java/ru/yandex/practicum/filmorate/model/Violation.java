package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class Violation {
    private final LocalDateTime timestamp = LocalDateTime.now();

    private final String fieldName;

    private final String message;

}