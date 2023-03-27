package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;


@Component
public class UserStorageImplOnUpdateUser implements UserStorage.OnUpdate {
    private User userBuilder;

    @Override
    public User updateUser(User user) {

        final User oldUser = UserStorageImpl.users.get(user.getId());
        UserStorageImpl.userEmails.remove(oldUser.getEmail());
        UserStorageImpl.userLogins.remove(oldUser.getLogin());

        if (user.getName() == null) {
            userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .build();
            UserStorageImpl.users.put(userBuilder.getId(), userBuilder);
            UserStorageImpl.userEmails.put(userBuilder.getEmail(), userBuilder.getId());
            UserStorageImpl.userLogins.put(userBuilder.getLogin(), userBuilder.getId());

            return userBuilder;
        }

        UserStorageImpl.users.put(user.getId(), user);
        UserStorageImpl.userEmails.put(user.getEmail(), user.getId());
        UserStorageImpl.userLogins.put(user.getLogin(), user.getId());

        return user;
    }
}
