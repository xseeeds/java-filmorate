package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;


@Component
public class UserStorageImplOnCreateUser implements UserStorage.OnCreate {

    private long globalId = 0;
    private User userBuilder;

    @Override
    public User createUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            userBuilder = user
                    .toBuilder()
                    .name(user.getLogin())
                    .id(getNextId())
                    .build();
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            userBuilder = user
                    .toBuilder()
                    .id(getNextId())
                    .build();
        }
        UserStorageImpl.users.put(userBuilder.getId(), userBuilder);
        UserStorageImpl.userEmails.put(userBuilder.getEmail(), userBuilder.getId());
        UserStorageImpl.userLogins.put(userBuilder.getLogin(), userBuilder.getId());

        return userBuilder;
    }


    @Override
    public void resetGlobalId() {
        globalId = 0;
    }

    private long getNextId() {
        return ++globalId;
    }

}
