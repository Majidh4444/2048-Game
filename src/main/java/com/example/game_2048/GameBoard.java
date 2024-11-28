package com.example.game_2048;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

public class GameBoard extends View {

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    private static final int GRID_SIZE = 4;
    private Tile[][] tiles;
    private int score;
    private int bestScore;
    private final Paint gridPaint;
    private final Paint textPaint;
    private final Random random;
    private SharedPreferences prefs;

    // Constructor for creating view programmatically
    public GameBoard(Context context) {
        this(context, null);
    }

    // Constructor for XML inflation
    public GameBoard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Constructor for XML inflation with style
    public GameBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        gridPaint = new Paint();
        gridPaint.setStyle(Paint.Style.FILL);
        gridPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        random = new Random();

        prefs = context.getSharedPreferences("GameState", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("bestScore", 0);

        initGame();
    }

    private void initGame() {
        tiles = new Tile[GRID_SIZE][GRID_SIZE];
        score = 0;

        // Add initial tiles
        addRandomTile();
        addRandomTile();

        invalidate();
    }

    private void addRandomTile() {
        ArrayList<int[]> emptyCells = new ArrayList<>();

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (tiles[i][j] == null) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            tiles[cell[0]][cell[1]] = new Tile(random.nextFloat() < 0.9 ? 2 : 4);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cellSize = getWidth() / GRID_SIZE;
        float padding = cellSize * 0.1f;

        // Draw background
        gridPaint.setColor(0xFFBBADA0);
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), padding, padding, gridPaint);

        // Draw grid cells
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                RectF rect = new RectF(
                        j * cellSize + padding,
                        i * cellSize + padding,
                        (j + 1) * cellSize - padding,
                        (i + 1) * cellSize - padding
                );

                if (tiles[i][j] != null) {
                    // Draw tile
                    gridPaint.setColor(getTileColor(tiles[i][j].getValue()));
                    canvas.drawRoundRect(rect, padding, padding, gridPaint);

                    // Draw number
                    String value = String.valueOf(tiles[i][j].getValue());
                    textPaint.setColor(tiles[i][j].getValue() <= 4 ? 0xFF776E65 : 0xFFF9F6F2);
                    textPaint.setTextSize(cellSize * (value.length() <= 2 ? 0.4f : 0.3f));
                    canvas.drawText(value,
                            rect.centerX(),
                            rect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2),
                            textPaint
                    );
                } else {
                    // Draw empty cell
                    gridPaint.setColor(0xFFCDC1B4);
                    canvas.drawRoundRect(rect, padding, padding, gridPaint);
                }
            }
        }
    }

    private int getTileColor(int value) {
        switch (value) {
            case 2: return 0xFFEEE4DA;
            case 4: return 0xFFEDE0C8;
            case 8: return 0xFFF2B179;
            case 16: return 0xFFF59563;
            case 32: return 0xFFF67C5F;
            case 64: return 0xFFF65E3B;
            case 128: return 0xFFEDCF72;
            case 256: return 0xFFEDCC61;
            case 512: return 0xFFEDC850;
            case 1024: return 0xFFEDC53F;
            case 2048: return 0xFFEDC22E;
            default: return 0xFFCDC1B4;
        }
    }

    public void move(Direction direction) {
        boolean moved = false;
        boolean[][] merged = new boolean[GRID_SIZE][GRID_SIZE];

        switch (direction) {
            case UP:
                for (int j = 0; j < GRID_SIZE; j++) {
                    for (int i = 1; i < GRID_SIZE; i++) {
                        if (tiles[i][j] != null) {
                            moved |= moveTile(i, j, -1, 0, merged);
                        }
                    }
                }
                break;

            case DOWN:
                for (int j = 0; j < GRID_SIZE; j++) {
                    for (int i = GRID_SIZE - 2; i >= 0; i--) {
                        if (tiles[i][j] != null) {
                            moved |= moveTile(i, j, 1, 0, merged);
                        }
                    }
                }
                break;

            case LEFT:
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 1; j < GRID_SIZE; j++) {
                        if (tiles[i][j] != null) {
                            moved |= moveTile(i, j, 0, -1, merged);
                        }
                    }
                }
                break;

            case RIGHT:
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = GRID_SIZE - 2; j >= 0; j--) {
                        if (tiles[i][j] != null) {
                            moved |= moveTile(i, j, 0, 1, merged);
                        }
                    }
                }
                break;
        }

        if (moved) {
            addRandomTile();
            if (score > bestScore) {
                bestScore = score;
                prefs.edit().putInt("bestScore", bestScore).apply();
            }
            invalidate();
        }
    }

    private boolean moveTile(int row, int col, int rowDelta, int colDelta, boolean[][] merged) {
        boolean moved = false;
        int newRow = row;
        int newCol = col;

        while (true) {
            int nextRow = newRow + rowDelta;
            int nextCol = newCol + colDelta;

            if (nextRow < 0 || nextRow >= GRID_SIZE ||
                    nextCol < 0 || nextCol >= GRID_SIZE) {
                break;
            }

            if (tiles[nextRow][nextCol] == null) {
                tiles[nextRow][nextCol] = tiles[newRow][newCol];
                tiles[newRow][newCol] = null;
                newRow = nextRow;
                newCol = nextCol;
                moved = true;
            } else if (!merged[nextRow][nextCol] &&
                    tiles[nextRow][nextCol].getValue() == tiles[newRow][newCol].getValue()) {
                int mergedValue = tiles[newRow][newCol].getValue() * 2;
                tiles[nextRow][nextCol].setValue(mergedValue);
                tiles[newRow][newCol] = null;
                merged[nextRow][nextCol] = true;
                score += mergedValue;
                moved = true;
                break;
            } else {
                break;
            }
        }

        return moved;
    }

    public boolean isGameOver() {
        // Check for empty cells
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (tiles[i][j] == null) {
                    return false;
                }
            }
        }

        // Check for possible merges
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int value = tiles[i][j].getValue();

                // Check right neighbor
                if (j < GRID_SIZE - 1 && tiles[i][j + 1].getValue() == value) {
                    return false;
                }

                // Check bottom neighbor
                if (i < GRID_SIZE - 1 && tiles[i + 1][j].getValue() == value) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean hasWon() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (tiles[i][j] != null && tiles[i][j].getValue() == 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    public void restart() {
        initGame();
    }

    public int getScore() {
        return score;
    }

    public int getBestScore() {
        return bestScore;
    }

    private static class Tile {
        private int value;

        Tile(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }

        void setValue(int value) {
            this.value = value;
        }
    }
}