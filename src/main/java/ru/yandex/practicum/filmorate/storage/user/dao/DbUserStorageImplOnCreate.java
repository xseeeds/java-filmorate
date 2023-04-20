package ru.yandex.practicum.filmorate.storage.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashMap;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbUserStorageImplOnCreate implements UserStorage.OnCreate {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User createUser(User user) {
        User userBuilder;
        if (user.getName() == null || user.getName().isBlank()) {
            userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .build();
        } else {
            userBuilder = user
                   .toBuilder()
                   .build();
        }

        final HashMap<String, Object> userToMap = new HashMap<>();

        userToMap.put("email", userBuilder.getEmail());
        userToMap.put("login", userBuilder.getLogin());
        userToMap.put("name", userBuilder.getName());
        userToMap.put("birthday", userBuilder.getBirthday());

        final SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        final long id = simpleJdbcInsert.executeAndReturnKey(userToMap).longValue();

        return userBuilder
                .toBuilder()
                .id(id)
                .build();
    }

    @Override
    public void resetGlobalId() {
        jdbcTemplate.update(
                "ALTER TABLE public.users " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 1");
    }
}
