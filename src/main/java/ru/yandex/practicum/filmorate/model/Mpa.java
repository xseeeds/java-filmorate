package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Positive;

@Getter
@Builder
public class Mpa {

    @Positive
    private final Long id;

    private final String name;

}
