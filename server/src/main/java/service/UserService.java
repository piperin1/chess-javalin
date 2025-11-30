package service;
import dataaccess.*;
import model.UserData;
import model.AuthData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;


public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws AlreadyTakenException, DataAccessException {
            if (userDAO.getUser(user.username()) != null) {
                throw new AlreadyTakenException("User already exists");
            }
            userDAO.createUser(user);
            String authToken = UUID.randomUUID().toString();
            AuthData auth = new AuthData(authToken,user.username());
            authDAO.createAuth(auth);
            return auth;
    }

    public AuthData login(UserData user) throws DataAccessException, UnauthorizedException {
        var existingUser = userDAO.getUser(user.username());
        if (existingUser == null) {
            throw new UnauthorizedException("User does not exist");
        }
        if (!BCrypt.checkpw(user.password(), existingUser.password())) {
            throw new UnauthorizedException("Invalid password");
        }
        String authToken = UUID.randomUUID().toString();
        AuthData auth = new AuthData(authToken, user.username());
        authDAO.createAuth(auth);
        return auth;
    }

    public void logout(String authToken) throws DataAccessException, UnauthorizedException {
            AuthData auth = authDAO.getAuth(authToken);
            if (auth == null) {
                throw new UnauthorizedException("Invalid or expired auth token");
            }
            authDAO.deleteAuth(authToken);
    }

    public AuthData authenticate(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Invalid or expired auth token");
        }
        return auth;
    }


    public void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
    }
}
