package chess;
import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    private final ChessPosition pieceStart;
    private final ChessPosition pieceEnd;
    private final ChessPiece.PieceType piecePromo;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        pieceStart = startPosition;
        pieceEnd = endPosition;
        piecePromo = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() { return pieceStart; }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() { return pieceEnd; }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return piecePromo;
    }

    /**
     * Override functions
     */
    @Override
    public int hashCode() {
        return Objects.hash(pieceStart, pieceEnd, piecePromo);
    }

    @Override
    public String toString() {
        return "ChessMove{" + "startPosition=" + pieceStart + ", endPosition=" + pieceEnd + ", promotionPiece=" + piecePromo + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;};
        if (o == null || getClass() != o.getClass()) {return false;};
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(pieceStart, chessMove.pieceStart) && Objects.equals(pieceEnd, chessMove.pieceEnd) && piecePromo == chessMove.piecePromo;
    }
}
