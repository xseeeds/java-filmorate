package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class User {

    @Min(0)
    int id;

    @Email
    String email;

    @NotNull
    @NotBlank
    String login;

    String name;

    @Past
    LocalDate birthday;

    @Builder.Default
    Set<Integer> friendsIds = new HashSet<>();
}