package game2048;

import ucb.util.CommandArgs;

import game2048.gui.Game;
import static game2048.Main.Side.*;

/** The main class for the 2048 game.
 *  @author David Au
 */
public class Main {

    /** Size of the board: number of rows and of columns. */
    static final int SIZE = 4;
    /** Number of squares on the board. */
    static final int SQUARES = SIZE * SIZE;
    /** MagicNumber. */
    static final int MAGICNUMBER = 2048;

    /** Symbolic names for the four sides of a board. */
    static enum Side { NORTH, EAST, SOUTH, WEST };

    /** The main program.  ARGS may contain the options --seed=NUM,
     *  (random seed); --log (record moves and random tiles
     *  selected.); --testing (take random tiles and moves from
     *  standard input); and --no-display. */
    public static void main(String... args) {
        CommandArgs options =
            new CommandArgs("--seed=(\\d+) --log --testing --no-display",
                            args);
        if (!options.ok()) {
            System.err.println("Usage: java game2048.Main [ --seed=NUM ] "
                               + "[ --log ] [ --testing ] [ --no-display ]");
            System.exit(1);
        }

        Main game = new Main(options);

        while (game.play()) {
            /* No action */
        }
        System.exit(0);
    }

    /** A new Main object using OPTIONS as options (as for main). */
    Main(CommandArgs options) {
        boolean log = options.contains("--log"),
            display = !options.contains("--no-display");
        long seed = !options.contains("--seed") ? 0 : options.getLong("--seed");
        _testing = options.contains("--testing");
        _game = new Game("2048", SIZE, seed, log, display, _testing);
    }

    /** Reset the score for the current game to 0 and clear the board. */
    void clear() {
        _score = 0;
        _count = 0;
        _game.clear();
        _game.setScore(_score, _maxScore);
        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                _board[r][c] = 0;
            }
        }
    }

    /** Play one game of 2048, updating the maximum score. Return true
     *  iff play should continue with another game, or false to exit. */
    boolean play() {
        clear();
        setRandomPiece();

        while (true) {
            setRandomPiece();

            if (gameOver()) {
                if (_score > _maxScore) {
                    _maxScore = _score;
                    _game.setScore(_score, _maxScore);
                }
                _game.endGame();
            }

        GetMove:
            while (true) {
                String key = _game.readKey();
                if (key.equals("↑")) {
                    key = "Up";
                } else if (key.equals("↓")) {
                    key = "Down";
                } else if (key.equals("←")) {
                    key = "Left";
                } else if (key.equals("→")) {
                    key = "Right";
                }

                switch (key) {
                case "Up": case "Down": case "Left": case "Right":
                    if (!gameOver() && tiltBoard(keyToSide(key))) {
                        break GetMove;
                    }
                    break;
                case "Quit":
                    return false;
                case "New Game":
                    return true;
                default:
                    break;
                }
            }
        }
    }

    /** Return true iff the current game is over (no more moves
     *  possible or MagicNumber 2048). */
    boolean gameOver() {
        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                int currVal = _board[r][c];
                if (currVal == MAGICNUMBER) {
                    return true;
                } else if (currVal == getBoardCell(r - 1, c)
                    || currVal == getBoardCell(r, c + 1)
                    || currVal == getBoardCell(r + 1, c)
                    || currVal == getBoardCell(r, c - 1)) {
                    return false;
                }
            }
        }
        return _count == SQUARES ? true : false;
    }
    /** Helper Function to return a tile with row R and col C. */
    int getBoardCell(int r, int c) {
        if ((0 <= r && r < SIZE) && (0 <= c && c < SIZE)) {
            return _board[r][c];
        } else {
            return -1;
        }
    }
    /** Add a tile to a random, empty position, choosing a value (2 or
     *  4) at random.  Has no effect if the board is currently full. */
    void setRandomPiece() {
        int[] t = _game.getRandomTile();
        if (_count == SQUARES) {
            return;
        }
        while (_board[t[1]][t[2]] != 0) {
            t = _game.getRandomTile();
        }
        _game.addTile(t[0], t[1], t[2]);
        _board[t[1]][t[2]] = t[0];
        _count += 1;
    }

    /** Perform the result of tilting the board toward SIDE.
     *  Returns true iff the tilt changes the board. **/
    boolean tiltBoard(Side side) {
        /* As a suggestion (see the project text), you might try copying
         * the board to a local array, turning it so that edge SIDE faces
         * north.  That way, you can re-use the same logic for all
         * directions.  (As usual, you don't have to). */
        int[][] board = new int[SIZE][SIZE];

        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                board[r][c] =
                    _board[tiltRow(side, r, c)][tiltCol(side, r, c)];
            }
        }
        boolean isChanged = upward(board, side);
        for (int r = 0; r < SIZE; r += 1) {
            for (int c = 0; c < SIZE; c += 1) {
                if (board[r][c] % 2 != 0) {
                    board[r][c] += 1;
                }
                _board[tiltRow(side, r, c)][tiltCol(side, r, c)]
                    = board[r][c];
            }
        }
        _game.displayMoves();
        return isChanged;

    }
    /** helper function that return merge and move using SIDE and BOARD. */
    boolean upward(int[][] board, Side side) {
        boolean isChanged = false;
        for (int c = 0; c < SIZE; c += 1) {
            boolean[] merge = new boolean[SIZE];
            for (int r = 1; r < SIZE; r += 1) {
                int x = r - 1;
                if (board[r][c] != 0) {
                    while (x >= 0 && board[x][c] == 0) {
                        x -= 1;
                    }
                    if (x >= 0 && board[r][c] == board[x][c]
                        && (!merge[x])) {
                        _game.mergeTile(board[x][c],
                                        board[x][c] * 2,
                                       tiltRow(side, r, c), tiltCol(side, r, c),
                                       tiltRow(side, x, c),
                                       tiltCol(side, x, c));
                        merge[x] = true;
                        int newval = board[r][c] * 2;
                        board[r][c] = 0;
                        board[x][c] = newval;
                        _score += board[x][c];
                        _game.setScore(_score, _maxScore);
                        _count -= 1;
                        isChanged = true;
                    } else {
                        _game.moveTile(board[r][c],
                                       tiltRow(side, r, c), tiltCol(side, r, c),
                                       tiltRow(side, x + 1, c),
                                       tiltCol(side, x + 1, c));
                        int newval2 = board[r][c];
                        board[r][c] = 0;
                        board[x + 1][c] = newval2;
                        if (r != x + 1) {
                            isChanged = true;
                        }
                    }
                }
            }
        }
        return isChanged;
    }

    /** Return the row number on a playing board that corresponds to row R
     *  and column C of a board turned so that row 0 is in direction SIDE (as
     *  specified by the definitions of NORTH, EAST, etc.).  So, if SIDE
     *  is NORTH, then tiltRow simply returns R (since in that case, the
     *  board is not turned).  If SIDE is WEST, then column 0 of the tilted
     *  board corresponds to row SIZE - 1 of the untilted board, and
     *  tiltRow returns SIZE - 1 - C. */
    int tiltRow(Side side, int r, int c) {
        switch (side) {
        case NORTH:
            return r;
        case EAST:
            return c;
        case SOUTH:
            return SIZE - 1 - r;
        case WEST:
            return SIZE - 1 - c;
        default:
            throw new IllegalArgumentException("Unknown direction");
        }
    }

    /** Return the column number on a playing board that corresponds to row
     *  R and column C of a board turned so that row 0 is in direction SIDE
     *  (as specified by the definitions of NORTH, EAST, etc.). So, if SIDE
     *  is NORTH, then tiltCol simply returns C (since in that case, the
     *  board is not turned).  If SIDE is WEST, then row 0 of the tilted
     *  board corresponds to column 0 of the untilted board, and tiltCol
     *  returns R. */
    int tiltCol(Side side, int r, int c) {
        switch (side) {
        case NORTH:
            return c;
        case EAST:
            return SIZE - 1 - r;
        case SOUTH:
            return SIZE - 1 - c;
        case WEST:
            return r;
        default:
            throw new IllegalArgumentException("Unknown direction");
        }
    }

    /** Return the side indicated by KEY ("Up", "Down", "Left",
     *  or "Right"). */
    Side keyToSide(String key) {
        switch (key) {
        case "Up":
            return NORTH;
        case "Down":
            return SOUTH;
        case "Left":
            return WEST;
        case "Right":
            return EAST;
        default:
            throw new IllegalArgumentException("unknown key designation");
        }
    }

    /** Represents the board: _board[r][c] is the tile value at row R,
     *  column C, or 0 if there is no tile there. */
    private final int[][] _board = new int[SIZE][SIZE];

    /** True iff --testing option selected. */
    private boolean _testing;
    /** THe current input source and output sink. */
    private Game _game;
    /** The score of the current game, and the maximum final score
     *  over all games in this session. */
    private int _score, _maxScore;
    /** Number of tiles on the board. */
    private int _count;
}
