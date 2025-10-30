package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTests {
    private SQLUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new SQLUserDAO();
        userDAO.clear();
    }

    @Test
    @Order(1)
    public void createUserPositive() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        userDAO.createUser(user);

        UserData fetched = userDAO.getUser("alice");
        assertNotNull(fetched);
        assertEquals("alice", fetched.username());
        assertEquals("alice@example.com", fetched.email());
        assertNotNull(fetched.password());
        assertNotEquals("password123", fetched.password(), "Password not hashed");
    }

    @Test
    @Order(2)
    public void createUserNegative() throws DataAccessException {
        UserData user = new UserData("bob", "secret", "bob@example.com");
        userDAO.createUser(user);

        UserData duplicate = new UserData("bob", "otherpass", "bob2@example.com");
        assertThrows(DataAccessException.class, () -> userDAO.createUser(duplicate),
                "Creating a user with duplicate username should throw DataAccessException");
    }

    @Test
    @Order(3)
    public void getUserPositive() throws DataAccessException {
        UserData user = new UserData("charlie", "pass", "charlie@example.com");
        userDAO.createUser(user);

        UserData fetched = userDAO.getUser("charlie");
        assertNotNull(fetched);
        assertEquals("charlie", fetched.username());
        assertEquals("charlie@example.com", fetched.email());
    }

    @Test
    @Order(4)
    public void getUserNegative() throws DataAccessException {
        UserData result = userDAO.getUser("nonexistent");
        assertNull(result, "Fetching a non-existent user should return null");
    }

    @Test
    @Order(5)
    public void clearPositive() throws DataAccessException {
        UserData user1 = new UserData("dave", "pass1", "dave@example.com");
        UserData user2 = new UserData("eve", "pass2", "eve@example.com");
        userDAO.createUser(user1);
        userDAO.createUser(user2);

        userDAO.clear();

        assertNull(userDAO.getUser("dave"));
        assertNull(userDAO.getUser("eve"));
    }
}
