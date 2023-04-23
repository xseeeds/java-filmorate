package ru.yandex.practicum.filmorate.storage.user.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
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

@Repository("userStorage")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbUserStorageImpl implements UserStorage {
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
                "ALTER TABLE users " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 1");
    }

    @Override
    public User updateUser(User user) {

        final User userBuilder;

        if (user.getName() == null) {
            userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .build();

        } else {
            userBuilder = user.toBuilder().build();
        }

        final String sqlUpdateUser =
                "UPDATE users " +
                        "SET email = ?, login = ?, name = ?, birthday = ? " +
                        "WHERE id = ?";

        jdbcTemplate.update(sqlUpdateUser,
                userBuilder.getEmail(),
                userBuilder.getLogin(),
                userBuilder.getName(),
                userBuilder.getBirthday(),
                userBuilder.getId());

        if (!userBuilder.getFriendsIdsStatus().isEmpty()) {

            jdbcTemplate.update(
                    "DELETE FROM friendship " +
                            "WHERE user_id = ?",
                    userBuilder.getId());

            userBuilder.getFriendsIdsStatus().forEach(
                    (friend_id, status) -> jdbcTemplate.update(
                            "INSERT INTO friendship " +
                                    "(user_id, friend_id, status) " +
                                    "VALUES (?, ?, ?)",
                            userBuilder.getId(), friend_id, status.toString()));
        }

        return userBuilder;
    }

    @Override
    public User getUserById(long userId) {

        checkUserById(userId);

        final String sql =
                "SELECT * " +
                        "FROM users " +
                        "WHERE id = ?";

        return jdbcTemplate.queryForObject(sql,
                this::makeUser,
                userId);
    }

    @Override
    public Collection<User> getAllUser() {

        final String sql =
                "SELECT * " +
                        "FROM users";

        return jdbcTemplate.query(sql,
                this::makeUser);
    }

    @Override
    public void removeAllUser() {

        jdbcTemplate.update(
                "DELETE FROM user_film_like");

        jdbcTemplate.update(
                "DELETE FROM friendship");

        jdbcTemplate.update(
                "DELETE FROM users");
        jdbcTemplate.update(
                "ALTER TABLE users " +
                        "ALTER COLUMN id " +
                        "RESTART WITH 1");
    }

    @Override
    public void removeUserById(long userId) {

        checkUserById(userId);

        jdbcTemplate.update(
                "DELETE FROM users " +
                        "WHERE id = ?",
                userId);
    }

    @Override
    public void checkUserFriendById(long userId, long otherId, boolean addOrRemove) throws ConflictException, NotFoundException {

        final String sql =
                "SELECT status " +
                        "FROM friendship " +
                        "WHERE user_id = ? " +
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
    public void checkFriendByUserId(long userId) throws NotFoundException {

        final String sql =
                "SELECT friend_id " +
                        "FROM friendship " +
                        "WHERE user_id = ? " +
                        "LIMIT 1";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                userId);

        if (!rows.next()) {
            throw new NotFoundException("У пользователя с id => " + userId + " нет друзей");
        }
    }

    @Override
    public void checkUserByFriendId(long otherId) throws NotFoundException {

        final String sql =
                "SELECT user_id " +
                        "FROM friendship " +
                        "WHERE friend_id = ? " +
                        "LIMIT 1";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                otherId);

        if (!rows.next()) {
            throw new NotFoundException("У пользователя с id => " + otherId + " нет друзей");
        }
    }

    @Override
    public void addFriend(long userId, long otherId, Status status) {

        jdbcTemplate.update(
                "INSERT INTO friendship " +
                        "(user_id, friend_id, status) " +
                        "VALUES (?, ?, ?)",
                userId, otherId, status.toString());
    }

    @Override
    public void updateStatusFriendShip(long userId, long otherId, Status status) {
        jdbcTemplate.update(
                "UPDATE friendship " +
                        "SET status = ? " +
                        "WHERE user_id = ? " +
                        "AND friend_id = ? " +
                        "AND user_id <> friend_id",
                status.toString(), userId, otherId);
    }

    @Override
    public boolean checkStatusFriendship(long userId, long otherId, Status status) {

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT user_id " +
                        "FROM friendship " +
                        "WHERE user_id = ? " +
                        "AND friend_id = ? " +
                        "AND status = ?",
                userId, otherId, status.toString());

        return rows.next();
    }

    @Override
    public boolean checkFriendship(long userId, long otherId) {

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(
                "SELECT user_id " +
                        "FROM friendship " +
                        "WHERE user_id = ? " +
                        "AND friend_id = ?",
                userId, otherId);

        return rows.next();
    }

    @Override
    public void removeFriend(long userId, long otherId) {
        jdbcTemplate.update(
                "DELETE FROM friendship " +
                        "WHERE user_id = ? " +
                        "AND friend_id = ?",
                userId, otherId);
    }

    @Override
    public Collection<User> getAllFriendsByUserId(long userId) {

        final String sql =
                "SELECT users.id, users.email, users.login, users.name, users.birthday " +
                        "FROM users " +
                        "JOIN friendship ON users.id = friendship.friend_id " +
                        "WHERE friendship.user_id = ? " +
                        "AND (status LIKE 'FRIENDSHIP'" +
                        "OR status LIKE 'SUBSCRIPTION')";

        return jdbcTemplate.query(sql,
                this::makeUser,
                userId);
    }

    @Override
    public Collection<User> getCommonFriendsByUser(long userId, long otherId) {

        final String sql =
                "SELECT users.id, users.email, users.login, users.name, users.birthday " +
                        "FROM users " +
                        "JOIN friendship AS fs1 ON fs1.friend_id = users.id " +
                        "JOIN friendship AS fs2 ON fs1.friend_id = fs2.friend_id " +
                        "WHERE fs1.user_id = ? AND fs2.user_id = ? " +
                        "AND (fs2.status LIKE 'FRIENDSHIP' " +
                        "OR fs2.status LIKE 'SUBSCRIPTION')";

        return jdbcTemplate.query(sql,
                this::makeUser,
                userId, otherId);
    }

    @Override
    public void checkUserById(long userId) throws NotFoundException {

        final String sql =
                "SELECT id " +
                        "FROM users " +
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
                        "FROM users " +
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
                        "FROM users " +
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
                        "FROM users " +
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
                        "FROM users " +
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
                        "FROM friendship " +
                        "WHERE friendship.user_id = ?";

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