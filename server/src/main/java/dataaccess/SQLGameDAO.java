package dataaccess;
import model.GameData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import chess.ChessGame;

public class SQLGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    public SQLGameDAO() {
        try {
            DatabaseManager.createDatabase();
            try (var conn = DatabaseManager.getConnection();
                 var table = conn.prepareStatement("""
                     CREATE TABLE IF NOT EXISTS games (
                         gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                         whiteUsername VARCHAR(255),
                         blackUsername VARCHAR(255),
                         gameName VARCHAR(255),
                         gameState TEXT
                     )
                     """)) {
                table.executeUpdate();
            }
        } catch (DataAccessException | SQLException e) {
            throw new RuntimeException("Failed to initialize SQLGameDAO", e);
        }
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        var sql = "INSERT INTO games (whiteUsername, blackUsername, gameName, gameState) VALUES (?, ?, ?, ?)";
        var chessGame = new ChessGame();
        var gameState = gson.toJson(chessGame);
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setNull(1, java.sql.Types.VARCHAR);
            stmt.setNull(2, java.sql.Types.VARCHAR);
            stmt.setString(3, gameName);
            stmt.setString(4, gameState);
            stmt.executeUpdate();
            try (var rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new DataAccessException("Failed to retrieve game ID");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        var sql = "SELECT * FROM games WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var white = rs.getString("whiteUsername");
                    var black = rs.getString("blackUsername");
                    var name = rs.getString("gameName");
                    var json = rs.getString("gameState");
                    var chessGame = gson.fromJson(json, ChessGame.class);
                    return new GameData(gameID, white, black, name, chessGame);
                } else {
                    throw new DataAccessException("Game not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game", e);
        }
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        var sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, gameState = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, game.gameName());
            stmt.setString(4, gson.toJson(game.game()));
            stmt.setInt(5, gameID);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game", e);
        }
    }

    @Override
    public Map<Integer,GameData> listGames() throws DataAccessException {
        var games = new HashMap<Integer, GameData>();
        var sql = "SELECT * FROM games";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql);
             var rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("gameID");
                var white = rs.getString("whiteUsername");
                var black = rs.getString("blackUsername");
                var name = rs.getString("gameName");
                var json = rs.getString("gameState");
                var chessGame = gson.fromJson(json, ChessGame.class);
                games.put(id, new GameData(id, white, black, name, chessGame));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games", e);
        }
        return games;
    }

    @Override
    public void clear() throws DataAccessException {
        var sql = "DELETE FROM games";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games table", e);
        }
    }
}
