package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@Builder
public class UserFilmLike {
    private int userId;
    private int friendsId;
    private int estimation;

    public UserFilmLike makeUserFilmLike(ResultSet resultSet) throws SQLException {

        return UserFilmLike
                .builder()
                .userId(resultSet.getInt("user_id"))
                .friendsId(resultSet.getInt("friends_id"))
                .estimation(resultSet.getInt("estimation"))
                .build();
    }
}
