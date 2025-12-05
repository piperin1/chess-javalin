package ui;

import chess.*;

import java.util.Set;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class BoardDrawer {
    private ChessGame game;
    private static final String LIGHT_SQUARE = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE = SET_BG_COLOR_MAGENTA;
    public static final String LIGHT_HIGHLIGHT = "\u001b[48;5;153m";
    public static final String DARK_HIGHLIGHT  = "\u001b[48;5;25m";

    public void draw(ChessGame game, ChessGame.TeamColor pov) {
        System.out.println("\n");
        printBoard(game, pov, null);
    }

    public void printBoard(ChessGame game,
                           ChessGame.TeamColor color,
                           Set<ChessPosition> highlights) {
        this.game = game;
        boolean reversed = (color == ChessGame.TeamColor.BLACK);
        var sb = new StringBuilder(SET_TEXT_BOLD);
        sb.append(printHeaderRow(reversed));
        for (int r = 8; r >= 1; r--) {
            int row = reversed ? 9 - r : r;
            sb.append(printBoardRow(row, reversed, highlights));
        }
        sb.append(printHeaderRow(reversed)).append("\n");
        sb.append(RESET_TEXT_BOLD_FAINT);
        out.println(sb);
    }


    private String printHeaderRow(boolean reversed) {
        StringBuilder sb = new StringBuilder();
        sb.append(SET_BG_COLOR_BLACK).append(SET_TEXT_COLOR_MAGENTA);
        if (!reversed) {
            sb.append("    a  b  c  d  e  f  g  h    ");
        }
        else {
            sb.append("    h  g  f  e  d  c  b  a    ");
        }
        sb.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");
        return sb.toString();
    }

    private String printBoardRow(int row,
                                 boolean reversed,
                                 Set<ChessPosition> highlights) {
        StringBuilder sb = new StringBuilder();
        sb.append(SET_BG_COLOR_BLACK)
                .append(SET_TEXT_COLOR_BLUE)
                .append(" %d ".formatted(row));
        for (int colIndex = 1; colIndex <= 8; colIndex++) {
            int col = reversed ? 9 - colIndex : colIndex;
            ChessPosition pos = new ChessPosition(row, col);
            boolean highlight = highlights != null && highlights.contains(pos);
            sb.append(getSquareColor(row, col, highlight)).append(getPieceSymbol(row, col));
        }
        sb.append(SET_BG_COLOR_BLACK)
                .append(SET_TEXT_COLOR_BLUE)
                .append(" %d ".formatted(row))
                .append(RESET_BG_COLOR)
                .append(RESET_TEXT_COLOR)
                .append("\n");
        return sb.toString();
    }


    private String getPieceSymbol(int row, int col) {
        ChessPiece piece = game.getBoard().getPiece(new ChessPosition(row, col));
        if (piece == null) {
            return "   ";
        }
        String colorCode = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
        String type = switch (piece.getPieceType()) {
            case KING -> " K ";
            case QUEEN -> " Q ";
            case ROOK -> " R ";
            case BISHOP -> " B ";
            case KNIGHT -> " N ";
            case PAWN -> " P ";
        };
        return colorCode + type;
    }

    private String getSquareColor(int row, int col, boolean highlighted) {
        boolean isLight = ((row + col) % 2 == 0);
        if (highlighted) {
            return isLight ? LIGHT_HIGHLIGHT : DARK_HIGHLIGHT;
        }
        return isLight ? LIGHT_SQUARE : DARK_SQUARE;
    }
}

