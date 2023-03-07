package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NonNull;
import nonapi.io.github.classgraph.json.Id;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    @Id
    private int id;
    @Email
    private String email;
    @NotNull
    @NotBlank
    private String login;

    private String name;
    @Past
    private LocalDate birthday;
}