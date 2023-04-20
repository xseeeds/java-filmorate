package ru.yandex.practicum.filmorate.storage.user.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;



@Component
public class InMemoryUserStorageImplOnCreate implements UserStorage.OnCreate {

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
        } else {
            userBuilder = user
                    .toBuilder()
                    .id(getNextId())
                    .build();
        }
        InMemoryUserStorageImpl.users.put(userBuilder.getId(), userBuilder);
        InMemoryUserStorageImpl.userEmails.put(userBuilder.getEmail(), userBuilder.getId());
        InMemoryUserStorageImpl.userLogins.put(userBuilder.getLogin(), userBuilder.getId());

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
