package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;

import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaService {
    private final MpaStorage dbMpaStorageImpl;


    public List<Mpa> getMpaList() {
        List<Mpa> allMpa = dbMpaStorageImpl.getMpaList();

        log.info("Mpa получены (кол-во) => {}", allMpa.size());

        return allMpa;
    }

    public Mpa getMpaById(@Positive int id) {
        log.info("Mpa получен по id => {}", id);

        return dbMpaStorageImpl.getMpaById(id);
    }
}