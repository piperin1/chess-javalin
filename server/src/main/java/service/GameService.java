package service;
import chess.*;
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
        if (auth == null) {
            throw new UnauthorizedException("Invalid auth token");
        }
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
                if (game.blackUsername() != null && !game.blackUsername().trim().equalsIgnoreCase(username.trim())) {
                    throw new AlreadyTakenException("Team color already taken");
                }
                gameDAO.updateGame(gameID, new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game()));
                break;
            case "WHITE":
                if (game.whiteUsername() != null && !game.whiteUsername().trim().equalsIgnoreCase(username.trim())) {
                    throw new AlreadyTakenException("Team color already taken");
                }
                gameDAO.updateGame(gameID, new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game()));
                break;
            case "EMPTY":
                break;
            default:
                throw new IllegalArgumentException("Invalid color; must be WHITE or BLACK");
        }
    }

    public GameData getGame(String authToken, int gameID)
            throws DataAccessException, UnauthorizedException {

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Invalid or expired auth token");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }

        return game;
    }

    public void makeMove(String authToken, int gameID, ChessMove move)
            throws DataAccessException, UnauthorizedException, InvalidMoveException {

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Invalid or expired auth token");
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Game not found");
        }

        String username = auth.username();
        ChessGame game = gameData.game();
        boolean isWhite = username.equals(gameData.whiteUsername());
        boolean isBlack = username.equals(gameData.blackUsername());

        if (!isWhite && !isBlack) {
            throw new UnauthorizedException("Observers may not make moves");
        }
        if (game.getTeamTurn() == ChessGame.TeamColor.WHITE && !isWhite) {
            throw new UnauthorizedException("Not your turn");
        }
        if (game.getTeamTurn() == ChessGame.TeamColor.BLACK && !isBlack) {
            throw new UnauthorizedException("Not your turn");
        }
        if (gameData.game().isGameOver()) {
            throw new InvalidMoveException("Game is already over");
        }

        game.makeMove(move);

        gameDAO.updateGame(
                gameID,
                new GameData(
                        gameID,
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        game
                )
        );
    }

    public void resignGame(String authToken, int gameID)
            throws DataAccessException, UnauthorizedException {

        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Invalid or expired auth token");
        }
        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("Game not found");
        }

        String username = auth.username();
        ChessGame game = gameData.game();
        boolean isWhite = username.equals(gameData.whiteUsername());
        boolean isBlack = username.equals(gameData.blackUsername());

        if (!isWhite && !isBlack) {
            throw new UnauthorizedException("Observers may not resign");
        }

        if (game.isGameOver()) {
            throw new IllegalStateException("Game already ended");
        }

        game.setGameOver(true);

        gameDAO.updateGame(
                gameID,
                new GameData(
                        gameID,
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        game
                )
        );
    }

    public void clear() throws DataAccessException {
        gameDAO.clear();
    }

    public void updateGameWithoutAuth(int gameID, GameData game) throws DataAccessException {
        gameDAO.updateGame(gameID, game);
    }

    public GameData getGameWithoutAuth(int gameID) throws DataAccessException {
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        return game;
    }


}
