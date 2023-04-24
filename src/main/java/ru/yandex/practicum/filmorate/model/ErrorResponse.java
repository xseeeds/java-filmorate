package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final LocalDateTime timestamp = LocalDateTime.now();

    private final String status;

    private final String error;

    private final String description;
}