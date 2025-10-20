package service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import model.*;
import dataaccess.*;

public class UserTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerSuccess() throws Exception {
        UserData user = new UserData("test","password","test@example.com");
        AuthData auth = userService.register(user);
        assertNotNull(auth);
        assertEquals("test", auth.username());
        assertNotNull(userDAO.getUser("test"));
    }

    @Test
    void registerFail() throws Exception {
        UserData user = new UserData("test","password","test@example.com");
        userService.register(user);
        assertThrows(AlreadyTakenException.class, () -> userService.register(user));
    }

    @Test
    void loginSuccess() throws DataAccessException, AlreadyTakenException, UnauthorizedException {
        UserData user = new UserData("test","password","test@example.com");
        userService.register(user);
        AuthData auth = userService.login(user);
        AuthData fromDAO = authDAO.getAuth(auth.authToken());
        assertNotNull(fromDAO);
        assertEquals(fromDAO.username(), auth.username());
        assertEquals(fromDAO.authToken(), auth.authToken());
    }

    @Test
    void loginFail() throws Exception {
        UserData user = new UserData("test","password","test@example.com");
        userService.register(user);
        assertThrows(UnauthorizedException.class, () ->  userService.login(new UserData("test","incorrect","test@example.com")));
    }

    @Test
    void logoutSuccess() throws DataAccessException, AlreadyTakenException, UnauthorizedException {
        UserData user = new UserData("test","password","test@example.com");
        userService.register(user);
        AuthData auth = userService.login(user);
        userService.logout(auth.authToken());
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void logoutFail() throws DataAccessException, AlreadyTakenException, UnauthorizedException {
        UserData user = new UserData("test","password","test@example.com");
        userService.register(user);
        AuthData auth = userService.login(user);
        userService.logout(auth.authToken());
        assertThrows(UnauthorizedException.class, () ->  userService.logout(auth.authToken()));
    }

    @Test
    void clearSuccess() throws DataAccessException, AlreadyTakenException {
        UserData user = new UserData("test","password","test@example.com");
        AuthData auth = userService.register(user);
        userService.clear();
        assertNull(userDAO.getUser("test"));
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void clearFail() {
        assertDoesNotThrow(() -> userService.clear());
        assertDoesNotThrow(() -> userService.clear());
    }

}
