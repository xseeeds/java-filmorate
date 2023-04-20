package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class User {

    @Min(value = 0,groups = UserStorage.class, message = "должно быть больше 0")
    @Null(groups = UserStorage.OnCreate.class, message = "POST request. Для обновления используй PUT запрос, user имеет id!!!")
    @NotNull(groups = UserStorage.OnUpdate.class, message = "PUT request. Для обновления используй id!!! в теле запроса user")
    Long id;

    //@Email(regexp = "[A-Za-z0-9._%-+]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}", message = "Пожалуйста укажите действительный адрес")
    @Email(groups = UserStorage.class)
    String email;

    @NotNull(groups = UserStorage.class)
    //@Pattern(regexp = "[^' ']*", message = "Invalid login")
    @Pattern(regexp = "^\\S+$", message = "не должен быть пустым и содержать пробелы", groups = UserStorage.class)
    String login;

    String name;

    @Past(groups = UserStorage.class)
    LocalDate birthday;

    @Builder.Default
    Map<Long, Status> friendsIdsStatus = new TreeMap<>();

}