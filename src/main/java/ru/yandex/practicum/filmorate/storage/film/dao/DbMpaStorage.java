package ru.yandex.practicum.filmorate.storage.film.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DbMpaStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Mpa> getMpaList() {

        final String sql =
                "SELECT * " +
                        "FROM public.mpa";

        return jdbcTemplate.query(sql,
                this::makeMpa);
    }

    @Override
    public Mpa getMpaById(int id) {

        final String sql =
                "SELECT name " +
                        "FROM public.mpa " +
                        "WHERE id = ?";

        final SqlRowSet rows = jdbcTemplate.queryForRowSet(sql,
                id);

        if (rows.next()) {
            return Mpa
                    .builder()
                    .id(id)
                    .name(rows.getString("name"))
                    .build();
        } else {
            throw new NotFoundException("Рейтинг по id => " + id + " не существует");
        }
    }

    @Override
    public Mpa makeMpa(ResultSet resultSet, int rowNum) throws SQLException {

        return Mpa
                .builder()
                .id(resultSet.getInt("id"))
                .name(resultSet.getString("name"))
                .build();

    }
}
