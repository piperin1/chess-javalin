package chess;
import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] boardLayout;

    public ChessBoard() {
       boardLayout = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.boardLayout[position.getColumn()-1][position.getRow()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.boardLayout[position.getColumn()-1][position.getRow()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        clearBoard();

        ChessPiece.PieceType[] outerRow = {
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        };

        // White pieces
        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(1, col),
                    new ChessPiece(ChessGame.TeamColor.WHITE, outerRow[col - 1]));
            addPiece(new ChessPosition(2, col),
                    new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }

        // Black pieces
        for (int col = 1; col <= 8; col++) {
            addPiece(new ChessPosition(7, col),
                    new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(8, col),
                    new ChessPiece(ChessGame.TeamColor.BLACK, outerRow[col - 1]));
        }
    }

    /**
     * Clears board to be totally empty
     */
    public void clearBoard() {
        for (int row = 0; row < 8; row++) {
            Arrays.fill(boardLayout[row], null);
        }
    }

    /**
     * Override functions
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;};
        if (o == null || getClass() != o.getClass()) {return false;};
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(boardLayout, that.boardLayout);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(boardLayout);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int y = 7; y >= 0; y--) {
            output.append("|");
            for (int x = 0; x < 8; x++) {
                output.append(boardLayout[x][y] != null ? boardLayout[x][y].toString() : " ");
                output.append("|");
            }
            output.append("\n");
        }
        return output.toString();
    }

}
