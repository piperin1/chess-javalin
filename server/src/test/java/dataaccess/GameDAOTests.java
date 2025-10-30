package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GameDAOTests {
    private SQLGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        gameDAO.clear();
    }

    @Test
    @Order(1)
    public void createGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("Test Game");
        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game);
        assertEquals("Test Game", game.gameName());
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
        assertNotNull(game.game());
    }

    @Test
    @Order(2)
    public void createGameNegative() {
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(null);
        });
    }

    @Test
    @Order(3)
    public void getGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("GetGameTest");
        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game);
        assertEquals("GetGameTest", game.gameName());
    }

    @Test
    @Order(4)
    public void getGameNegative() {
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(9999),
                "Fetching non-existent game should throw DataAccessException");
    }

    @Test
    @Order(5)
    public void updateGamePositive() throws DataAccessException {
        int gameID = gameDAO.createGame("UpdateTest");
        GameData original = gameDAO.getGame(gameID);
        GameData updated = new GameData(gameID, "Alice", "Bob", "UpdatedGame", original.game());
        gameDAO.updateGame(gameID, updated);

        GameData fetched = gameDAO.getGame(gameID);
        assertEquals("Alice", fetched.whiteUsername());
        assertEquals("Bob", fetched.blackUsername());
        assertEquals("UpdatedGame", fetched.gameName());
    }

    @Test
    @Order(6)
    public void updateGameNegative() {
        GameData dummy = new GameData(9999, "A", "B", "NonExistent", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(9999, dummy),
                "Updating a non-existent game should throw DataAccessException");
    }

    @Test
    @Order(7)
    public void listGamesPositive() throws DataAccessException {
        int id1 = gameDAO.createGame("Game1");
        int id2 = gameDAO.createGame("Game2");

        Map<Integer, GameData> games = gameDAO.listGames();
        assertEquals(2, games.size());
        assertTrue(games.containsKey(id1));
        assertTrue(games.containsKey(id2));
    }

    @Test
    @Order(8)
    public void listGamesNegative() throws DataAccessException {
        Map<Integer, GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "Listing games in empty DB should return empty map");
    }

    @Test
    @Order(9)
    public void clearPositive() throws DataAccessException {
        gameDAO.createGame("GameToClear");
        gameDAO.clear();
        Map<Integer, GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty(), "After clear, games table should be empty");
    }
}
