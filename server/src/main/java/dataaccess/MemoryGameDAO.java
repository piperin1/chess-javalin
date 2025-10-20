package dataaccess;
import chess.ChessGame;
import model.GameData;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextID = 1;

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextID++;
        GameData newGame = new GameData(gameID,null, null, gameName, new ChessGame());
        games.put(gameID, newGame);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        if (games.containsKey(gameID)) {
            return games.get(gameID);
        }
        throw new DataAccessException("Game with id " + gameID + " does not exist");
    }

    @Override
    public void updateGame(int gameID, GameData game) throws DataAccessException {
        if (!games.containsKey(gameID)) {
            throw new DataAccessException("Game not found");
        }
        games.put(gameID, game);
    }

    @Override
    public Map<Integer,GameData> listGames() throws DataAccessException {
        return new HashMap<>(games);
    }

    @Override
    public void clear(){
        games.clear();
    }
}
