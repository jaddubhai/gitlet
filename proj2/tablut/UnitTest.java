package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test as a placeholder for real ones. */
    @Test
    public void inittest() {
        Board board = new Board();
        board.init();
        assert (board.get(4, 4) == Piece.KING);
        assert (board.turn() == Piece.BLACK);
        assert (board.pieceLocations(Piece.BLACK).size() == 16);
    }

    @Test
    public void movetest() {
        Board board = new Board();
        board.init();
        board.makeMove(Move.mv(Square.sq(3, 0 ), Square.sq(0, 0)));
        assert (board.pieceLocations(Piece.BLACK).contains(Square.sq(0, 0)));
    }

    @Test
    public void illegalmove() {
        Board board = new Board();
        board.init();
        board.makeMove(Move.mv(Square.sq(3, 0 ), Square.sq(3, 1)));
        assert (board.pieceLocations(Piece.BLACK).contains(Square.sq(3, 0)));
        assertFalse(board.pieceLocations(Piece.BLACK).contains(Square.sq(3, 1)));
    }
}


