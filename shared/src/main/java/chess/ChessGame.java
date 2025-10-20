package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        Collection<ChessMove> moveList = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : moveList) {
            ChessPiece tempPiece = board.getPiece(move.getEndPosition());
            board.addPiece(startPosition, null);
            board.addPiece(move.getEndPosition(), piece);
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            board.addPiece(move.getEndPosition(), tempPiece);
            board.addPiece(startPosition, piece);
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at starting square");
        }

        Collection<ChessMove> possibleMoves = validMoves(move.getStartPosition());
        boolean canMove = getTeamTurn() == board.getPiece(move.getStartPosition()).getTeamColor();
        boolean canAdd = possibleMoves.contains(move);
        if (possibleMoves == null || !canAdd || !canMove) {
            throw new InvalidMoveException("Invalid move");
        }
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(),move.getPromotionPiece());
        }
        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = findKing(teamColor);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition curPos = new ChessPosition(i+1, j+1);
                ChessPiece curPiece = board.getPiece(curPos);
                if (curPiece != null && curPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> enemyMoves = curPiece.pieceMoves(board, curPos);
                    if (checkEnemyMoves(enemyMoves, kingPos)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean checkEnemyMoves(Collection<ChessMove> enemyMoves, ChessPosition kingPos){
        for (ChessMove move : enemyMoves) {
            if (move.getEndPosition().equals(kingPos)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param color which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor color) {
        if (!isInCheck(color)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition start = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(start);

                if (piece != null && piece.getTeamColor() == color) {
                    if (canEscapeCheck(piece, start, color)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private boolean canEscapeCheck(ChessPiece piece, ChessPosition start, TeamColor color) {
        for (ChessMove move : piece.pieceMoves(board, start)) {
            ChessPiece captured = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(), piece);
            board.addPiece(start, null);
            boolean stillInCheck = isInCheck(color);
            board.addPiece(start, piece);
            board.addPiece(move.getEndPosition(), captured);
            if (!stillInCheck) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                Collection<ChessMove> moveList;
                ChessPosition position = new ChessPosition(y, x);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && teamColor == piece.getTeamColor()) {
                    moveList = validMoves(position);
                    if (moveList != null && !moveList.isEmpty()) {
                        return false;
                    }}
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    /**
     * Locates and returns the position of the King for selected team
     */
    public ChessPosition findKing(TeamColor team) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPosition curPos = new ChessPosition(i+1,j+1);
                ChessPiece curPiece = board.getPiece(curPos);
                if (curPiece!=null && curPiece.getPieceType() == ChessPiece.PieceType.KING && curPiece.getTeamColor() == team){
                    return curPos;
                }
            }
        }
        return null;
    }

    /**
     * Override functions
     */
    @Override
    public String toString() {
        return "ChessGame{" + "teamTurn=" + teamTurn + ", board=" + board + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;};
        if (o == null || getClass() != o.getClass()) {return false;} ;
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
