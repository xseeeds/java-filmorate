package ru.yandex.practicum.filmorate.storage.user.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;


@Component
public class InMemoryUserStorageImplOnUpdate implements UserStorage.OnUpdate {

    @Override
    public User updateUser(User user) {

        final User oldUser = InMemoryUserStorageImpl.users.get(user.getId());
        InMemoryUserStorageImpl.userEmails.remove(oldUser.getEmail());
        InMemoryUserStorageImpl.userLogins.remove(oldUser.getLogin());

        if (user.getName() == null) {
            final User userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .build();
            InMemoryUserStorageImpl.users.put(userBuilder.getId(), userBuilder);
            InMemoryUserStorageImpl.userEmails.put(userBuilder.getEmail(), userBuilder.getId());
            InMemoryUserStorageImpl.userLogins.put(userBuilder.getLogin(), userBuilder.getId());

            return userBuilder;
        }

        InMemoryUserStorageImpl.users.put(user.getId(), user);
        InMemoryUserStorageImpl.userEmails.put(user.getEmail(), user.getId());
        InMemoryUserStorageImpl.userLogins.put(user.getLogin(), user.getId());

        return user;
    }
}
