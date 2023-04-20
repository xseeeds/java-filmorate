package ru.yandex.practicum.filmorate.storage.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbUserStorageImplOnUpdate implements UserStorage.OnUpdate {
    private final JdbcTemplate jdbcTemplate;
    private User userBuilder;

    @Override
    public User updateUser(User user) {

        if (user.getName() == null) {
            userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .build();

        } else {
            userBuilder = user.toBuilder().build();
        }

        final String sqlUpdateUser =
                "UPDATE public.users " +
                        "SET email = ?, login = ?, name = ?, birthday = ? " +
                        "WHERE id = ?";

        jdbcTemplate.update(sqlUpdateUser,
                userBuilder.getEmail(),
                userBuilder.getLogin(),
                userBuilder.getName(),
                userBuilder.getBirthday(),
                userBuilder.getId());
/*
        jdbcTemplate.update(
                "DELETE FROM public.friend " +
                        "WHERE user_id = ?",
                userBuilder.getId());

        if (!userBuilder.getFriendsIdsStatus().isEmpty()) {

            userBuilder.getFriendsIdsStatus().forEach(
                    (key, value) -> jdbcTemplate.update(
                            "INSERT INTO public.friend " +
                                    "(user_id, friend_id, status) " +
                                    "VALUES (?, ?, ?)",
                            userBuilder.getId(), key, value.toString()));
        }
*/
        return userBuilder;
    }
}
