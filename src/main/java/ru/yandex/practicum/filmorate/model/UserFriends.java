package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;

@Data
@Builder
public class UserFriends {
    private long id;
    private long friendsId;
    private Status status;

    public UserFriends makeUserFriend(ResultSet resultSet) throws SQLException {

        return UserFriends
                .builder()
                .id(resultSet.getLong("id"))
                .friendsId(resultSet.getLong("friendsId"))
                .status(Status.valueOf(resultSet.getString("status")))
                .build();
    }
}