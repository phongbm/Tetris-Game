// Board.java
package oop.asg04;

import java.util.Arrays;

/**
 * CS108 Tetris Board. Represents a Tetris board -- essentially a 2-d grid of
 * booleans. Supports tetris pieces and row clearing. Has an "undo" feature that
 * allows clients to add and remove pieces efficiently. Does not do any drawing
 * or have any idea of pixels. Instead, just represents the abstract 2-d board.
 */
public class Board {
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean DEBUG = true;
	boolean committed;

	// MY CODE

	private int maxHeight;
	private int[] heights;
	private int[] widths;

	private boolean[][] gridBackup;
	private int[] heightsBackup;
	private int[] widthsBackup;

	// Here a few trivial methods are provided:

	/**
	 * Creates an empty board of the given width and height measured in blocks.
	 */
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		committed = true;

		// YOUR CODE HERE

		maxHeight = 0;
		heights = new int[width];
		widths = new int[height];

		gridBackup = new boolean[width][height];
		heightsBackup = new int[width];
		widthsBackup = new int[height];
	}

	/**
	 * Returns the width of the board in blocks.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns the height of the board in blocks.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the max column height present in the board. For an empty board
	 * this is 0.
	 */
	public int getMaxHeight() {
		return maxHeight; // YOUR CODE HERE
	}

	/**
	 * Checks the board for internal consistency -- used for debugging.
	 */
	public void sanityCheck() {
		if (DEBUG) {
			// YOUR CODE HERE

			if (!checkHeights())
				throw new RuntimeException("Check heights is failed!");
			if (!checkWidths())
				throw new RuntimeException("Check widths is failed!");
			if (!checkMaxHeight())
				throw new RuntimeException("Check maxHeight is failed!");
		}
	}

	/**
	 * Given a piece and an x, returns the y value where the piece would come to
	 * rest if it were dropped straight down at that x.
	 * 
	 * <p>
	 * Implementation: use the skirt and the col heights to compute this fast --
	 * O(skirt length).
	 */
	public int dropHeight(Piece piece, int x) {
		int y = 0, i = 0;
		int[] skirt = piece.getSkirt();
		while (i < skirt.length) {
			int heightsXI = heights[x + i];
			if (y < -(skirt[i] - heightsXI)) {
				y = -(skirt[i] - heightsXI);
			}
			i++;
		}
		return y; // YOUR CODE HERE
	}

	/**
	 * Returns the height of the given column -- i.e. the y value of the highest
	 * block + 1. The height is 0 if the column contains no blocks.
	 */
	public int getColumnHeight(int x) {
		return heights[x]; // YOUR CODE HERE
	}

	/**
	 * Returns the number of filled blocks in the given row.
	 */
	public int getRowWidth(int y) {
		return widths[y]; // YOUR CODE HERE
	}

	/**
	 * Returns true if the given block is filled in the board. Blocks outside of
	 * the valid width/height area always return true.
	 */
	public boolean getGrid(int x, int y) {
		if (x < 0 || y < 0)
			return false;
		if (x >= width || y >= height)
			return false;
		if (!grid[x][y])
			return false;
		return true; // YOUR CODE HERE
	}

	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 * Attempts to add the body of a piece to the board. Copies the piece blocks
	 * into the board grid. Returns PLACE_OK for a regular placement, or
	 * PLACE_ROW_FILLED for a regular placement that causes at least one row to
	 * be filled.
	 * 
	 * <p>
	 * Error cases: A placement may fail in two ways. First, if part of the
	 * piece may falls out of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 * Or the placement may collide with existing blocks in the grid in which
	 * case PLACE_BAD is returned. In both error cases, the board may be left in
	 * an invalid state. The client can use undo(), to recover the valid,
	 * pre-place state.
	 */
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed)
			throw new RuntimeException("place commit problem");

		int result = PLACE_OK;

		// YOUR CODE HERE

		backup();
		if (checkOutBounds(piece, x, y))
			return PLACE_OUT_BOUNDS;
		if (checkBad(piece, x, y))
			return PLACE_BAD;
		for (int i = 0; i < piece.getBody().length; i++) {
			int gridX = x + piece.getBody()[i].x;
			int gridY = y + piece.getBody()[i].y;
			grid[gridX][gridY] = true;
			widths[gridY]++;
			heights[gridX] = Math.max(heights[gridX], gridY + 1);
			if (widths[gridY] == width)
				result = PLACE_ROW_FILLED;
		}
		computeMaxHeight();
		sanityCheck();
		committed = false;

		return result;
	}

	/**
	 * Deletes rows that are filled all the way across, moving things above
	 * down. Returns the number of rows cleared.
	 */
	public int clearRows() {
		int rowsCleared = 0;
		// YOUR CODE HERE
		if (committed)
			backup();
		int topRow = 0;
		for (int j = 0; j < maxHeight; j++) {
			if (widths[j] == width) {
				rowsCleared++;
			} else {
				for (int i = 0; i < width; i++) {
					if (topRow != j)
						grid[i][topRow] = grid[i][j];
				}
				topRow++;
			}
		}
		for (int j = topRow; j < maxHeight; j++) {
			for (int i = 0; i < width; i++) {
				grid[i][j] = false;
			}
		}
		computeWidthsAndHeights();
		computeMaxHeight();
		committed = false;

		sanityCheck();
		return rowsCleared;
	}

	/**
	 * Reverts the board to its state before up to one place and one
	 * clearRows(); If the conditions for undo() are not met, such as calling
	 * undo() twice in a row, then the second undo() does nothing. See the
	 * overview docs.
	 */
	public void undo() {
		// YOUR CODE HERE

		if (!committed) {
			int[] tempWidths = widths;
			widths = widthsBackup;
			widthsBackup = tempWidths;

			int[] tempHeights = heights;
			heights = heightsBackup;
			heightsBackup = tempHeights;

			boolean[][] tempGrid = grid;
			grid = gridBackup;
			gridBackup = tempGrid;

			computeMaxHeight();
		}
		commit();
		sanityCheck();
	}

	/**
	 * Puts the board in the committed state.
	 */
	public void commit() {
		committed = true;
	}

	/*
	 * Renders the board state as a big String, suitable for printing. This is
	 * the sort of print-obj-state utility that can help see complex state
	 * change over time. (provided debugging utility)
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height - 1; y >= 0; y--) {
			buff.append('|');
			for (int x = 0; x < width; x++) {
				if (getGrid(x, y))
					buff.append('+');
				else
					buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x = 0; x < width + 2; x++)
			buff.append('-');
		return (buff.toString());
	}

	/*
	 * MY CODE Private functions to support the main function in the file
	 */

	private void computeMaxHeight() {
		maxHeight = 0;
		for (int i = 0; i < heights.length; i++)
			maxHeight = Math.max(maxHeight, heights[i]);
	}

	private boolean checkHeights() {
		for (int i = 0; i < width; i++) {
			int heightsCheck = 0;
			for (int j = 0; j < height; j++) {
				if (grid[i][j]) {
					heightsCheck = j + 1;
				}
			}
			if (heightsCheck != heights[i])
				return false;
		}
		return true;
	}

	private boolean checkWidths() {
		int[] widthsCheck = new int[height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (grid[i][j]) {
					widthsCheck[j]++;
				}
			}
		}
		if (!Arrays.equals(widthsCheck, widths))
			return false;
		else
			return true;
	}

	private boolean checkMaxHeight() {
		int checkMaxHeight = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (grid[i][j]) {
					checkMaxHeight = Math.max(checkMaxHeight, j + 1);
				}
			}
		}
		if (checkMaxHeight != maxHeight)
			return false;
		return true;
	}

	private void backup() {
		System.arraycopy(heights, 0, heightsBackup, 0, width);
		System.arraycopy(widths, 0, widthsBackup, 0, height);
		for (int i = 0; i < grid.length; i++)
			System.arraycopy(grid[i], 0, gridBackup[i], 0, grid[i].length);
	}

	private boolean checkOutBounds(Piece piece, int x, int y) {
		if (x < 0 || y < 0 || x + piece.getWidth() > width
				|| y + piece.getHeight() > height)
			return true;
		return false;
	}

	private boolean checkBad(Piece piece, int x, int y) {
		for (int i = 0; i < piece.getBody().length; i++) {
			int gridX = x + piece.getBody()[i].x;
			int gridY = y + piece.getBody()[i].y;
			if (grid[gridX][gridY])
				return true;
		}
		return false;
	}

	private void computeWidthsAndHeights() {
		heights = new int[width];
		widths = new int[height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (grid[i][j]) {
					heights[i] = j + 1;
					widths[j]++;
				}
			}
		}
	}

}