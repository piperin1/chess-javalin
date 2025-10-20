package dataaccess;
import model.GameData;
import java.util.HashMap;
import java.util.Map;

public interface GameDAO {
    int createGame(String gameName) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    Map<Integer,GameData> listGames() throws DataAccessException;
    void updateGame(int gameID, GameData gameData) throws DataAccessException;
    void clear();
}
