package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthDAOTests {

    private SQLAuthDAO authDAO;
    private AuthData testAuth;

    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        authDAO.clear();
        testAuth = new AuthData(UUID.randomUUID().toString(), "testuser");
    }

    @Test
    @Order(1)
    public void createAuthPositive() throws DataAccessException {
        authDAO.createAuth(testAuth);
        AuthData fetched = authDAO.getAuth(testAuth.authToken());
        assertNotNull(fetched);
        assertEquals(testAuth.username(), fetched.username());
        assertEquals(testAuth.authToken(), fetched.authToken());
    }

    @Test
    @Order(2)
    public void createAuthNegative() throws DataAccessException {
        authDAO.createAuth(testAuth);
        AuthData duplicate = new AuthData(testAuth.authToken(), "anotheruser");
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(duplicate),
                "Creating auth with duplicated token should fail");
    }

    @Test
    @Order(3)
    public void getAuthPositive() throws DataAccessException {
        authDAO.createAuth(testAuth);
        AuthData fetched = authDAO.getAuth(testAuth.authToken());
        assertNotNull(fetched);
        assertEquals(testAuth.username(), fetched.username());
    }

    @Test
    @Order(4)
    public void getAuthNegative() throws DataAccessException {
        AuthData fetched = authDAO.getAuth(UUID.randomUUID().toString());
        assertNull(fetched, "Fetching a non-existent token should return null");
    }

    @Test
    @Order(5)
    public void deleteAuthPositive() throws DataAccessException {
        authDAO.createAuth(testAuth);
        authDAO.deleteAuth(testAuth.authToken());
        AuthData fetched = authDAO.getAuth(testAuth.authToken());
        assertNull(fetched, "Auth token should be deleted");
    }

    @Test
    @Order(6)
    public void deleteAuthNegative() {
        assertDoesNotThrow(() -> authDAO.deleteAuth(UUID.randomUUID().toString()),
                "Deleting a non-existent token should not throw");
    }

    @Test
    @Order(7)
    public void clearPositive() throws DataAccessException {
        authDAO.createAuth(testAuth);
        authDAO.clear();
        AuthData fetched = authDAO.getAuth(testAuth.authToken());
        assertNull(fetched, "Auth token should not exist");
    }
}
