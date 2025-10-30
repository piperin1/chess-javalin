package dataaccess;
import model.AuthData;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO{

    public SQLAuthDAO() {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection();
                 var table = conn.prepareStatement("""
                     CREATE TABLE IF NOT EXISTS auth (
                         username VARCHAR(255) NOT NULL,
                         authToken VARCHAR(255) NOT NULL PRIMARY KEY
                     )
                     """)) {
                table.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException("Failed to initialize SQLAuthDAO", e);
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        var sql = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        try(var conn = DatabaseManager.getConnection();
            var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authData.username());
            stmt.setString(2, authData.authToken());
            stmt.executeUpdate();
        } catch(DataAccessException | SQLException e) {
            throw new DataAccessException("Error inserting auth data", e);
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        var sql = "SELECT username, authToken FROM auth WHERE authToken = ?";
        try(var conn = DatabaseManager.getConnection();
            var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(
                            rs.getString("authToken"),
                            rs.getString("username")
                    );
                }
                return null;
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Error fetching authToken", e);
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        var sql = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.executeUpdate();
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("Error deleting auth token", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var sql = "TRUNCATE auth";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth table", e);
        }
    }
}
