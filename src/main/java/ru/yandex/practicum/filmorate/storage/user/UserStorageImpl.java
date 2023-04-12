package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.TreeMap;


@Primary
@Component
public class UserStorageImpl implements UserStorage {
    protected static final TreeMap<Long, User> users = new TreeMap<>();
    protected static final TreeMap<String, Long> userEmails = new TreeMap<>();
    protected static final TreeMap<String, Long> userLogins = new TreeMap<>();


    @Override
    public User getUserById(long userId) {
        final User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Такой пользователь c id=>" + userId + " не существует");
        }
        return user;
    }

    @Override
    public Collection<User> getAllUser() {
        return users.values();
    }

    @Override
    public void removeAllUser() {
        users.clear();
        userLogins.clear();
        userEmails.clear();
    }

    @Override
    public void removeUserById(long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Такой user c id=>" + userId + " не существует");
        }
        final User user = users.remove(userId);
        userLogins.remove(user.getLogin());
        userEmails.remove(user.getEmail());
    }

    @Override
    public long getIdOnLogin(String updateUserLogin) {
        return userLogins.getOrDefault(updateUserLogin, 0L);
    }

    @Override
    public long getIdOnEmail(String updateUserEmail) {
        return userEmails.getOrDefault(updateUserEmail, 0L);
    }

}
