package ru.yandex.practicum.filmorate.storage.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbUserStorageImpl implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User getUserById(long userId) {

        checkUserById(userId);

        final String sql =
                "SELECT * " +
                        "FROM public.users " +
                        "WHERE id = ?";

        return jdbcTemplate.queryForObject(sql,
        this::makeUser,
                userId);
    }

    @Override
    public Collection<User> getAllUser() {

        final String sql =
                "SELECT * " +
                        "FROM public.users";

        return jdbcTemplate.query(sql,
                this::makeUser);
    }

    @Override
    public void removeAllUser() {

        jdbcTemplate.update(
                "DELETE FROM public.\"LIKE\"");

        jdbcTemplate.update(
                "DELETE FROM public.friend");

        jdbcTemplate.update(
                "DELETE FROM public.users");
        jdbcTemplate.update(
                "ALTER TABLE public.users " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 1");
    }

    @Override
    public void removeUserById(long userId) {

        checkUserById(userId);

        jdbcTemplate.update(
                "DELETE FROM public.users " +
                        "WHERE id = ?",
                userId);
    }

    @Override
    public void checkUserFriendById(long userId, long otherId, boolean addOrRemove) throws ConflictException, NotFoundException {

        final String sql =
                "SELECT status " +
                        "FROM public.friend " +
                        "WHERE user_id = ?" +
                        "AND friend_id =?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                userId, otherId);

        if (addOrRemove) {

            if (rows.next() && Status.valueOf(rows.getString("status")) == Status.FRIENDSHIP) {
                throw new ConflictException("У пользователя с id => " + userId
                        + " уже существует дружба с id => " + otherId);
            }
        } else {
            if (!rows.next()) {
                throw new NotFoundException("У пользователя с id => " + userId
                        + " не существует друга/заявки/подписки c id => " + otherId);
            }
        }
    }

    @Override
    public void checkFriendByUserId(long userId) throws ConflictException {

        final String sql =
                "SELECT friend_id " +
                        "FROM public.friend " +
                        "WHERE user_id = ?" +
                        "LIMIT 1";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                userId);

        if (!rows.next()) {
            throw new ConflictException("У пользователя с id => " + userId + " нет друзей");
        }
    }

    @Override
    public void checkUserByFriendId(long otherId) throws ConflictException {

        final String sql =
                "SELECT user_id " +
                        "FROM public.friend " +
                        "WHERE friend_id = ?" +
                        "LIMIT 1";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                otherId);

        if (!rows.next()) {
            throw new ConflictException("У пользователя с id => " + otherId + " нет друзей");
        }
    }

    @Override
    public void addFriend(long userId, long otherId, Status status) {

        jdbcTemplate.update(
                "INSERT INTO public.friend " +
                        "(user_id, friend_id, status) " +
                        "VALUES (?, ?, ?)",
                userId, otherId, status.toString());
    }

    @Override
    public void updateStatusFriendShip(long userId, long otherId, Status status) {
        jdbcTemplate.update(
                "UPDATE public.friend " +
                        "SET status = ? " +
                        "WHERE user_id = ? " +
                        "AND friend_id = ? " +
                        "AND user_id <> friend_id",
                status.toString(), userId, otherId);
    }

    @Override
    public void removeFriend(long userId, long otherId) {
        jdbcTemplate.update(
                "DELETE FROM public.friend " +
                        "WHERE user_id = ? " +
                        "AND friend_id = ?",
                userId, otherId);
    }

    @Override
    public Collection<User> getAllFriendsByUserId(long userId) {

        final String sql =
                "SELECT public.users.id, public.users.email, public.users.login, public.users.name, public.users.birthday " +
                        "FROM public.users " +
                        "JOIN public.friend ON public.users.id = public.friend.friend_id " +
                        "WHERE public.friend.user_id = ?" +
                        "AND status LIKE 'FRIENDSHIP'";

        return jdbcTemplate.query(sql,
                this::makeUser,
                userId);
    }

    @Override
    public Collection<User> getCommonFriendsByUser(long userId, long otherId) {

        final String sql =
                "SELECT public.users.id, public.users.email, public.users.login, public.users.name, public.users.birthday " +
                        "FROM public.users " +
                        "JOIN public.friend AS f1 ON f1.friend_id = public.users.id " +
                        "JOIN public.friend AS f2 ON f1.friend_id = f2.friend_id " +
                        "WHERE f1.user_id = ? and f2.user_id = ?";

        return jdbcTemplate.query(sql,
                this::makeUser,
                userId, otherId);
    }

    @Override
    public void checkUserById(long userId) throws NotFoundException {

        final String sql =
                "SELECT id " +
                        "FROM public.users " +
                        "WHERE id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                userId);

        if (!rows.next()) {
            throw new NotFoundException("Такой пользователь c id => " + userId + " не существует");
        }
    }

    @Override
    public void checkUserLogin(String newUserLogin) throws ConflictException {

        final String sql =
                "SELECT id " +
                        "FROM public.users " +
                        "WHERE login = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                newUserLogin);

        if (rows.next()) {
            throw new ConflictException("Такой пользователь с login: " + newUserLogin
                    + " уже существует, по id => " + rows.getLong("id") + " для обновления используй PUT запрос");
        }
    }

    @Override
    public void checkUserEmail(String newUserEmail) throws ConflictException {

        final String sql =
                "SELECT id " +
                        "FROM public.users " +
                        "WHERE email = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                newUserEmail);

        if (rows.next()) {
            throw new ConflictException("Такой пользователь с email:" + newUserEmail
                    + " уже существует, по id => " + rows.getLong("id") + " для обновления используй PUT запрос");
        }
    }

    @Override
    public void checkUserIdOnLogin(String updateUserLogin, long updateUserId) throws ConflictException {

        final String sql =
                "SELECT id " +
                        "FROM public.users " +
                        "WHERE login = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                updateUserLogin);

        if (rows.next()) {
            final long existentId = rows.getLong("id");

            if (existentId != updateUserId) {
                throw new ConflictException("Такой пользователь с login: "
                        + updateUserLogin + " уже существует, по id => " + existentId);
            }
        }
    }

    @Override
    public void checkUserIdOnEmail(String updateUserEmail, long updateUserId) throws ConflictException {

        final String sql =
                "SELECT id " +
                        "FROM public.users " +
                        "WHERE email = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                updateUserEmail);

        if (rows.next()) {
            final long existentId = rows.getLong("id");

            if (existentId != updateUserId) {
                throw new ConflictException("Такой пользователь с email: " + updateUserEmail
                        + " уже существует, по id => " + existentId);
            }
        }
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {

        final String sql =
                "SELECT friend_id, status " +
                        "FROM public.friend " +
                        "WHERE public.friend.user_id = ?";

        final User userBuilder = User
                .builder()
                .id(resultSet.getLong("id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();

        userBuilder
                .getFriendsIdsStatus()
                .putAll(
                        (Map<? extends Long, ? extends Status>) jdbcTemplate.query(sql,
                                        this::makeFriend,
                                        userBuilder.getId()
                                )
                                .stream()
                                .flatMap(
                                        map -> map.entrySet()
                                                .stream()
                                )
                                .collect(
                                        Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (oldValue, newValue) -> newValue,
                                                HashMap::new)));

        return userBuilder;
    }

    private Map<Long, Status> makeFriend(ResultSet rs, int rowNum) throws SQLException {
        return Map.of(rs.getLong("friend_id"), Status.valueOf(rs.getString("status")));
    }
}