package service;
import static org.junit.jupiter.api.Assertions.*;
import model.*;
import dataaccess.*;
import org.junit.jupiter.api.*;


public class GameTests {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private GameService gameService;
    private UserService userService;
    private String authToken;

    @BeforeEach
    void setUp() throws DataAccessException, AlreadyTakenException, UnauthorizedException {
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);

        UserData user = new UserData("testUser", "password", "email");
        userService.register(user);
        authToken = userService.login(user).authToken();
    }

    @Test
    void listSuccess() throws UnauthorizedException, DataAccessException {
        var games = gameService.listGames(authToken);
        assertNotNull(games, "Game list shouldn't be null");
        assertEquals(0, games.size(), "Game list should be empty");

        int id = gameService.createGame(authToken, "Game");
        var updated = gameService.listGames(authToken);
        assertEquals(1, updated.size(), "Game list should have one game");
        assertEquals("Game", updated.get(1).gameName(), "Game name should match");
    }

    @Test
    void listFail() {
        assertThrows(UnauthorizedException.class, () -> {
            gameService.listGames("invalidToken");
        });
    }

    @Test
    void createSuccess() throws UnauthorizedException, DataAccessException{
        int id = gameService.createGame(authToken, "My New Game");
        assertTrue(id > 0, "Game ID should be pos");
        var games = gameService.listGames(authToken);
        assertEquals(1, games.size(), "Should contain one game");
    }

    @Test
    void createFail() {
        assertThrows(UnauthorizedException.class, () -> {
            gameService.createGame("invalidToken", "Game Fail");
        });
    }

    @Test
    void joinSuccess() throws DataAccessException, UnauthorizedException, AlreadyTakenException {
        int id = gameService.createGame(authToken, "JoinGame");
        gameService.joinGame(authToken, "WHITE", id);
        var games = gameService.listGames(authToken);
        assertEquals("testUser", games.get(1).whiteUsername(), "White username should be testUser");
        assertNull(games.get(1).blackUsername(), "Black slot should be empty");
    }

    @Test
    void joinFail() throws UnauthorizedException, DataAccessException {
        int id = gameService.createGame(authToken, "BadGame");
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.joinGame(authToken, "BLUE", id);
        });
        assertThrows(UnauthorizedException.class, () -> {
            gameService.joinGame("fakeAuth", "WHITE", id);
        });
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(authToken, "BLACK", 9999999);
        });
    }
}

