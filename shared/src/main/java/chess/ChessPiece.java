package chess;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessPiece.PieceType pieceType;
    private final ChessGame.TeamColor pieceTeam;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        pieceType = type;
        pieceTeam = pieceColor;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceTeam;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceType type = board.getPiece(myPosition).getPieceType();
        ChessGame.TeamColor team = board.getPiece(myPosition).getTeamColor();
        Collection<ChessMove> moveList = new ArrayList<>();
        int direction = 1;
        if (team == ChessGame.TeamColor.BLACK) {
            direction = -1;
        }

        switch (type) {
            case PAWN:
                ChessPosition forwardOne = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
                if (onBoard(forwardOne) && board.getPiece(forwardOne) == null) {
                    if (isPromotionRank(forwardOne, team)) {
                        addPromotionMoves(moveList, myPosition, forwardOne);
                    } else {
                        moveList.add(new ChessMove(myPosition, forwardOne, null));
                    }
                }
                if ((team==ChessGame.TeamColor.WHITE && myPosition.getRow()==2) ||
                        (team==ChessGame.TeamColor.BLACK && myPosition.getRow()==7)) {
                    ChessPosition forwardTwo = new ChessPosition(myPosition.getRow() + (2 * direction), myPosition.getColumn());
                    if (onBoard(forwardTwo) && board.getPiece(forwardOne) == null && board.getPiece(forwardTwo) == null) {
                        ChessMove move = new ChessMove(myPosition, forwardTwo, null);
                        moveList.add(move);
                    }
                }
                for (int offset: new int[]{-1,1}) {
                    ChessPosition diag = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + offset );
                    if (onBoard(diag) && board.getPiece(diag)!=null && board.getPiece(diag).getTeamColor() != team) {
                            if (isPromotionRank(diag, team)) {
                                addPromotionMoves(moveList, myPosition, diag);
                            } else {
                                moveList.add(new ChessMove(myPosition, diag, null));
                            }
                    }
                }
                break;
            case ROOK:
                int[][] rookDirections= {
                        {1, 0}, //up
                        {-1,0}, //down
                        {0, 1}, //right
                        {0,-1}, //left
                };
                calculateLinearMoves(moveList, myPosition, board, team, rookDirections);
                break;
            case KNIGHT:
                int[][] knightDirections= {
                        {2, 1}, //upright
                        {-2,1}, //downright
                        {2, -1}, //upleft
                        {-2,-1}, //downleft
                        {1,2}, //rightup
                        {-1,2}, //rightdown
                        {1,-2}, //leftup
                        {-1,-2} //leftdown
                };
                calculateJumpMoves(moveList, myPosition, board, team, knightDirections);
                break;
            case BISHOP:
                int[][] bishopDirections= {
                        {1, 1}, //upright
                        {-1,1}, //downright
                        {1, -1}, //upleft
                        {-1,-1}, //downleft
                };
                calculateLinearMoves(moveList, myPosition, board, team, bishopDirections);
                break;
            case QUEEN:
                int[][] queenDirections= {
                        {1, 0}, //up
                        {-1,0}, //down
                        {0, 1}, //right
                        {0,-1}, //left
                        {1, 1}, //upright
                        {-1,1}, //downright
                        {1, -1}, //upleft
                        {-1,-1}, //downleft
                };
                calculateLinearMoves(moveList, myPosition, board, team, queenDirections);
                break;
            case KING:
                int[][] kingDirections= {
                        {1, 0}, //up
                        {-1,0}, //down
                        {0, 1}, //right
                        {0,-1}, //left
                        {1,1}, //upright
                        {1,-1}, //upleft
                        {-1,-1}, //downleft
                        {-1,1}, //downright
                };
                calculateJumpMoves(moveList, myPosition, board, team, kingDirections);
                break;
        }
        return moveList;
    }

    /**
     * Returns true when a position is in bounds
     */
    private boolean onBoard(ChessPosition myPosition){
        return ( 1 <= myPosition.getRow() && myPosition.getRow() <= 8 && 1<= myPosition.getColumn() && myPosition.getColumn() <= 8 );
    }

    /**
     * Helper for pawn promotion
     */
    private boolean isPromotionRank(ChessPosition pos, ChessGame.TeamColor team) {
        return (team == ChessGame.TeamColor.WHITE && pos.getRow() == 8)
                || (team == ChessGame.TeamColor.BLACK && pos.getRow() == 1);
    }

    /**
     * Helper 2 for pawn promotion
     */
    private void addPromotionMoves(Collection<ChessMove> moveList, ChessPosition start, ChessPosition end) {
        for (ChessPiece.PieceType promo : new ChessPiece.PieceType[]{
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT}) {
            moveList.add(new ChessMove(start, end, promo));
        }
    }

    /**
     * Calculates moves for pieces with linear, nonstop movement (Rook, Bishop, Queen)
     */
    private void calculateLinearMoves(Collection<ChessMove> moveList, ChessPosition myPosition,
                                      ChessBoard board, ChessGame.TeamColor team, int[][] directions){
        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while(true) {
                row += dRow;
                col += dCol;
                ChessPosition next = new ChessPosition(row, col);

                if (!onBoard(next)) {
                    break;
                }

                if (board.getPiece(next)!=null){
                    if (board.getPiece(next).getTeamColor()!=team){
                        ChessMove move = new ChessMove(myPosition, next,null);
                        moveList.add(move);
                    }
                    break;
                }

                ChessMove move = new ChessMove(myPosition, next,null);
                moveList.add(move);
            }
        }
    }

    /**
     * Calculates moves for pieces with singular movements (King, Knight)
     */
    private void calculateJumpMoves(Collection<ChessMove> moveList, ChessPosition myPosition,
                                    ChessBoard board, ChessGame.TeamColor team, int[][] directions ) {
        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            row += dRow;
            col += dCol;
            ChessPosition next = new ChessPosition(row, col);

            if (!onBoard(next)) {
                continue;
            }

            if (board.getPiece(next) != null) {
                if (board.getPiece(next).getTeamColor() != team) {
                    ChessMove move = new ChessMove(myPosition, next, null);
                    moveList.add(move);
                }
                continue;
            }

            ChessMove move = new ChessMove(myPosition, next,null);
            moveList.add(move);
        }
    }

    /**
     * Override functions
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; };
        if (o == null || getClass() != o.getClass()) {return false;} ;
        ChessPiece that = (ChessPiece) o;
        return pieceTeam == that.pieceTeam && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceTeam, pieceType);
    }


}
