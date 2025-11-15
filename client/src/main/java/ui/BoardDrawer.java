package ui;

import chess.*;
import java.util.Scanner;
import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class BoardDrawer {
    private ChessGame game;
    private static final String LIGHT_SQUARE = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE = SET_BG_COLOR_MAGENTA;
    private ChessGame.TeamColor pov;
    private ChessPosition selected;
    private final Scanner scanner = new Scanner(System.in);


    public BoardDrawer(ChessGame game) {
        this.game = game;
    }

    public void run(ChessGame.TeamColor pov) {
        this.pov = pov;
        this.selected = null;
        boolean inGame = true;
        while (inGame) {
            printBoard(pov, selected);
            printHelp();
            String cmd = scanner.nextLine().trim().toLowerCase();

            switch (cmd) {
                case "exit" -> inGame = false;
                case "help" -> printHelp();
                default -> System.out.println("This function has not yet been implemented");
            }
        }
    }

    private void printHelp() {
        System.out.println("""
        Commands:
         move <pos> <pos>     - make a move
         select <pos>      - select a square
         resign         - resign the game
         exit           - return to menu
         help           - show this help
    """);
    }

    public void printBoard(ChessGame.TeamColor color, ChessPosition selectedPos) {
        var sb = new StringBuilder(SET_TEXT_BOLD);
        boolean reversed = (color == ChessGame.TeamColor.BLACK);
        sb.append(printHeaderRow(reversed));
        for (int r = 8; r >= 1; r--) {
            int row = reversed ? 9 - r : r;
            sb.append(printBoardRow(row, reversed, selectedPos));
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

    private String printBoardRow(int row, boolean reversed, ChessPosition selected ) {
        StringBuilder sb = new StringBuilder();
        sb.append(SET_BG_COLOR_BLACK).append(SET_TEXT_COLOR_BLUE).append(" %d ".formatted(row));

        for (int i = 1; i <= 8; i++) {
            int col = reversed ? 9 - i : i;
            sb.append(getSquareColor(row, col, selected))
                    .append(getPieceSymbol(row, col));
        }
        sb.append(SET_BG_COLOR_BLACK).append(SET_TEXT_COLOR_BLUE)
                .append(" %d ".formatted(row))
                .append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");

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

    private String getSquareColor(int row, int col, ChessPosition selected) {
        ChessPosition pos = new ChessPosition(row, col);
        if (pos.equals(selected)) {
            return SET_BG_COLOR_BLUE;
        }
        //Later add highlighted square logic here
        boolean isLight = ((row + col) % 2 == 0);
        return (isLight ? LIGHT_SQUARE : DARK_SQUARE);
    }
}

