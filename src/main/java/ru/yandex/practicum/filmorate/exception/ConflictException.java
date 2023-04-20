package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConflictException extends RuntimeException {

    private final String message;

}

