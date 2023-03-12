package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmManager;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserManager;

public class Managers {
    private static InMemoryFilmManager filmManager;
    private static InMemoryUserManager userManager;

    public static InMemoryUserManager getDefaultUserManager() {
        if (userManager != null) {
            return userManager;
        }
        userManager = new InMemoryUserManager();
        return userManager;
    }

    public static InMemoryFilmManager getDefaultFilmManager() {
        if (filmManager != null) {
            return filmManager;
        }
        filmManager = new InMemoryFilmManager();
        return filmManager;
    }
}
