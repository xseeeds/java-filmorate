package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.TreeMap;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class User {

    @Min(value = 0, message = "должно быть больше 0")
    Long id;

    @Email
    String email;

    @NotNull

    @Pattern(regexp = "^\\S+$", message = "не должен быть пустым и содержать пробелы")
    String login;

    String name;

    @Past
    LocalDate birthday;

    @Builder.Default
    TreeMap<Long, Status> friendsIdsStatus = new TreeMap<>();

}