package service;
import dataaccess.*;
import model.*;
import java.util.Map;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public Map<Integer,GameData> listGames(String authToken) throws DataAccessException, UnauthorizedException{
        if (authDAO.getAuth(authToken)== null) {
            throw new UnauthorizedException("Invalid or expired auth token");
        }
        return gameDAO.listGames();
    }

    public int createGame(String authToken, String gameName) throws DataAccessException, UnauthorizedException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new UnauthorizedException("Invalid auth token");
        return gameDAO.createGame(gameName);
    }

    public void joinGame(String authToken, String color, int gameID) throws DataAccessException, UnauthorizedException, AlreadyTakenException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Invalid or expired auth token");
        }
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        String username = auth.username();
        switch (color.toUpperCase()) {
            case "BLACK":
                if (game.blackUsername() != null) throw new AlreadyTakenException("Team color already taken");
                gameDAO.updateGame(gameID, new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game()));
                break;
            case "WHITE":
                if (game.whiteUsername() != null) throw new AlreadyTakenException("Team color already taken");
                gameDAO.updateGame(gameID, new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game()));
                break;
            default:
                throw new IllegalArgumentException("Invalid color; must be WHITE or BLACK");
        }
    }

    public void clear() {
        gameDAO.clear();
    }


}
