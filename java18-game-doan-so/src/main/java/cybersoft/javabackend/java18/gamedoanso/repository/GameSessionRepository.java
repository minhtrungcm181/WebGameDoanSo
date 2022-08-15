package cybersoft.javabackend.java18.gamedoanso.repository;

import cybersoft.javabackend.java18.gamedoanso.model.GameSession;
import cybersoft.javabackend.java18.gamedoanso.model.Guess;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class GameSessionRepository extends AbstractRepository<GameSession> {
    public void save(GameSession gameSession) {
        final String query = """
                insert into game_session
                (id, target, start_time, is_completed, is_active, username)
                values(?, ?, ?, ?, ?, ?)
                """;
        executeUpdate(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, gameSession.getId());
            statement.setInt(2, gameSession.getTargetNumber());
            statement.setTimestamp(3, Timestamp.from(
                    gameSession.getStartTime().toInstant(ZoneOffset.of("+07:00")))
            );
            statement.setInt(4, gameSession.getIsCompleted() ? 1 : 0);
            statement.setInt(5, gameSession.isActive() ? 1 : 0);
            statement.setString(6, gameSession.getUsername());

            return statement.executeUpdate();
        });
    }

    public List<GameSession> findByUsername(String username) {
        final String query = """
                    select id,  target, start_time, end_time, is_completed, is_active, username
                    from game_session
                    where username = ?
                """;
        return executeQuery(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();
            List<GameSession> sessions = new ArrayList<>();

            while (resultSet.next()) {
                sessions.add(new GameSession()
                        .id(resultSet.getString("id"))
                        .targetNumber(resultSet.getInt("target"))
                        .startTime(getDateTimeFromResultSet("start_time", resultSet))
                        .endTime(getDateTimeFromResultSet("end_time", resultSet))
                        .isCompleted(resultSet.getInt("is_completed") == 1)
                        .isActive(resultSet.getInt("is_active") == 1)
                        .username(resultSet.getString("username")));
            }

            return sessions;
        });
    }

    public GameSession findById(String id) {
        final String query = """
                    select id,  target, start_time, end_time, is_completed, is_active, username
                    from game_session
                    where id = ?
                """;
        return executeQuerySingle(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);

            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;

            return new GameSession()
                    .id(resultSet.getString("id"))
                    .targetNumber(resultSet.getInt("target"))
                    .startTime(getDateTimeFromResultSet("start_time", resultSet))
                    .endTime(getDateTimeFromResultSet("end_time", resultSet))
                    .isCompleted(resultSet.getInt("is_completed") == 1)
                    .isActive(resultSet.getInt("is_active") == 1)
                    .username(resultSet.getString("username"));
        });
    }

    public void deactivateAllGames(String username) {
        final String query = """
                update game_session
                set is_active = 0
                where username = ?
                """;
        executeUpdate(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            return statement.executeUpdate();
        });
    }

    public void completeGame(String sessionId) {
        GuessRepository guess = new GuessRepository();
        final String query = """
                update game_session
                set is_completed = 1, end_time = ?, duration = ?, count = ?
                where id = ?
                """;
        executeUpdate(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(4, sessionId);
            LocalDateTime end_time = LocalDateTime.now();
            statement.setTimestamp(1, Timestamp.from(end_time.toInstant(ZoneOffset.of("+07:00"))));
            LocalDateTime time2 = findById(sessionId).getStartTime();
            Duration duration = Duration.between(time2,end_time);
            long x = duration.getSeconds();
            int a = (int) x;
            statement.setInt(2, a);
            int b = guess.getCount(sessionId);
            statement.setInt(3, b);
            return statement.executeUpdate();
        });
    }
    public List<GameSession> rankingByDuration() {
        PlayerRepository player = new PlayerRepository();
        final String query = """
                    SELECT username, duration, count FROM game_session
                    WHERE duration < 1000 and is_completed = 1
                    ORDER BY duration ASC, count ASC
                """;
        return executeQuery(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            List<GameSession> sessions = new ArrayList<>();
            while (resultSet.next()) {
                sessions.add(new GameSession()
                        .count(resultSet.getLong("count"))
                        .duration(resultSet.getLong("duration"))
                        .username(player.returnName(resultSet.getString("username"))));
            }
            return sessions;
        });
    }
    private LocalDateTime getDateTimeFromResultSet(String columnName, ResultSet resultSet) {
        Timestamp time;

        try {
            time = resultSet.getTimestamp(columnName);
        } catch (SQLException e) {
            return null;
        }

        if (time == null)
            return null;

        return time.toLocalDateTime();
    }
}
