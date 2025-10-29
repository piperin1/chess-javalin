package dataaccess;
import model.UserData;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {
    public SQLUserDAO () throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection();
            var table = conn.prepareStatement("""
                       CREATE TABLE IF NOT EXISTS users (
                           username VARCHAR(255) NOT NULL PRIMARY KEY,
                           password VARCHAR(255) NOT NULL,
                           email VARCHAR(255) NOT NULL
                   )"""
            )) {
            table.executeUpdate();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        var sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try(var conn = DatabaseManager.getConnection();
            var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,userData.username());
            stmt.setString(2,userData.password());
            stmt.setString(3,userData.email());
            stmt.executeUpdate();
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT username, password, email FROM users WHERE username = ?";
        try(var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching user", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var sql = "TRUNCATE TABLE users";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users table", e);
        }
    }
}
