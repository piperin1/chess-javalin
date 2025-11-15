package client;

import model.*;
import network.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;


public class ServerFacadeTests {

    private static Server server;
    private static int port;

    private ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void cleanup() throws Exception {
        facade = new ServerFacade("http://localhost:" + port);
        facade.clear();
    }

    @Test
    @Order(1)
    public void registerSuccess() throws Exception {
        AuthData result = facade.register("alice", "password", "a@b.com");
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("alice", result.username());
    }

    @Test
    @Order(2)
    public void registerFailureDuplicateUser() throws Exception {
        facade.register("alice", "password", "a@b.com");
        Assertions.assertThrows(Exception.class, () ->
                facade.register("alice", "password", "a@b.com"));
    }

    @Test
    @Order(3)
    public void loginSuccess() throws Exception {
        facade.register("bob", "pass", "b@c.com");
        AuthData result = facade.login("bob", "pass");
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("bob", result.username());
    }

    @Test
    @Order(4)
    public void loginFailureBadPassword() throws Exception {
        facade.register("bob", "pass", "b@c.com");
        Assertions.assertThrows(Exception.class, () ->
                facade.login("bob", "wrong"));
    }

    @Test
    @Order(5)
    public void logoutSuccess() throws Exception {
        facade.register("carol", "pass", "c@d.com");
        var auth = facade.login("carol", "pass");

        Assertions.assertDoesNotThrow(() ->
                facade.logout(auth.authToken()));
    }

    @Test
    @Order(6)
    public void logoutFailureInvalidToken() {
        Assertions.assertThrows(Exception.class, () ->
                facade.logout("invalid_token"));
    }

    @Test
    @Order(7)
    public void createGameSuccess() throws Exception {
        var auth = facade.register("dave", "pass", "d@e.com");
        int gameID = facade.createGame(auth.authToken(), "My Game");
        Assertions.assertTrue(gameID > 0);
    }

    @Test
    @Order(8)
    public void createGameFailureNoAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.createGame("bad_token", "Game"));
    }

    @Test
    @Order(9)
    public void listGamesSuccess() throws Exception {
        var auth = facade.register("erin", "pass", "e@f.com");
        facade.createGame(auth.authToken(), "Chess 1");
        facade.createGame(auth.authToken(), "Chess 2");

        List<GameData> games = facade.listGames(auth.authToken());
        Assertions.assertEquals(2, games.size());
    }

    @Test
    @Order(10)
    public void listGamesFailureInvalidAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.listGames("bad_token"));
    }

    @Test
    @Order(11)
    public void joinGameSuccess() throws Exception {
        var auth = facade.register("frank", "pass", "f@g.com");
        int gameID = facade.createGame(auth.authToken(), "Cool Game");

        Assertions.assertDoesNotThrow(() ->
                facade.joinGame(auth.authToken(), gameID, "WHITE"));
    }

    @Test
    @Order(12)
    public void joinGameFailureBadGameID() throws Exception {
        var auth = facade.register("harry", "pass", "h@i.com");

        Assertions.assertThrows(Exception.class, () ->
                facade.joinGame(auth.authToken(), 99999, "BLACK"));
    }

    @Test
    @Order(13)
    public void joinGameFailureInvalidAuth() {
        Assertions.assertThrows(Exception.class, () ->
                facade.joinGame("invalid", 1, "WHITE"));
    }

}
