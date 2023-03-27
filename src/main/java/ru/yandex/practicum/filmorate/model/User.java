package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class User {

    @Min(value = 0,groups = UserStorage.class, message = "должно быть больше 0")
    @Null(groups = UserStorage.OnCreate.class, message = "POST request. Для обновления используй PUT запрос, user имеет id!!!")
    @NotNull(groups = UserStorage.OnUpdate.class, message = "PUT request. Для обновления используй id!!! в теле запроса user")
    Long id;

    @Email(groups = UserStorage.class)
    String email;

    @NotNull(groups = UserStorage.class)
    @Pattern(regexp = "^\\S+$", message = "не должен быть пустым и содержать пробелы", groups = UserStorage.class)
    String login;

    String name;

    @Past(groups = UserStorage.class)
    LocalDate birthday;

    @Builder.Default
    Set<Long> friendsIds = new HashSet<>();
}