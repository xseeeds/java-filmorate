package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
public class Genre {

    @Positive
    private Integer id;

    @NotNull
    @NotBlank
    private String name;

}
