package com.example.game_2048;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import android.media.MediaPlayer;

public class MainActivity extends AppCompatActivity implements
        GestureDetector.OnGestureListener {

    private GameBoard gameBoard;
    private GestureDetectorCompat gestureDetector;
    private TextView scoreTextView;
    private TextView bestScoreTextView;
    private boolean gameOverChecked = false;
    private SharedPreferences prefs;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        mediaPlayer.setLooping(true); // Set looping to true if you want continuous play
        mediaPlayer.start(); // Start playing the music

        gameBoard = findViewById(R.id.gameBoard);
        scoreTextView = findViewById(R.id.score);
        bestScoreTextView = findViewById(R.id.bestScore);
        gestureDetector = new GestureDetectorCompat(this, this);
        prefs = getSharedPreferences("GameState", MODE_PRIVATE);

        // Check if we need to ask for name (first launch)
//        if (!prefs.contains("playerName")) {
            showNameInputDialog();


        updateScore();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the music when the activity goes into the background
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the music when the activity comes to the foreground
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the MediaPlayer resources when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void showNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Your Name");

        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setMessage("Please enter your name:");
        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playerName = input.getText().toString().trim();
                if (playerName.isEmpty()) {
                    playerName = "Player"; // Default name
                }
                prefs.edit().putString("playerName", playerName).apply();
                Toast.makeText(MainActivity.this, "Welcome " + playerName + "!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }


    private void saveScore() {
        String playerName = prefs.getString("playerName", "Player");
        int currentScore = gameBoard.getScore();

        // Get the leaderboard preferences
        SharedPreferences leaderboard = getSharedPreferences("Leaderboard", MODE_PRIVATE);

        // Get player's current high score
        int previousHighScore = leaderboard.getInt(playerName, 0);

        // Only update if current score is higher than previous high score
        if (currentScore > previousHighScore) {
            leaderboard.edit().putInt(playerName, currentScore).apply();
            Toast.makeText(this, "New High Score: " + currentScore, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGameState() {
        if (!gameOverChecked) {
            if (gameBoard.isGameOver()) {
                gameOverChecked = true;
                saveScore();
                showGameOverDialog();
            } else if (gameBoard.hasWon()) {
                gameOverChecked = true;
                saveScore();
                showWinDialog();
            }
        }
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over!")
                .setMessage("Your score: " + gameBoard.getScore())
                .setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .setNegativeButton("Main Menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showWinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Congratulations!")
                .setMessage("You've reached 2048!\nDo you want to continue playing?")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gameOverChecked = false;
                    }
                })
                .setNegativeButton("New Game", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void restartGame() {
        gameBoard.restart();
        gameOverChecked = false;
        // Ask for name when starting a new game
        showNameInputDialog();
        updateScore();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new_game) {
            restartGame();
            return true;
        } else if (id == R.id.action_leaderboard) {
            startActivity(new Intent(this, LeaderboardActivity.class));
            return true;
        } else if (id == R.id.action_change_name) {
            showNameInputDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateScore() {
        scoreTextView.setText(String.valueOf(gameBoard.getScore()));
        bestScoreTextView.setText(String.valueOf(gameBoard.getBestScore()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2,
                           float velocityX, float velocityY) {
        if (e1 == null || e2 == null) return false;

        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                if (diffX > 0) {
                    gameBoard.move(GameBoard.Direction.RIGHT);
                } else {
                    gameBoard.move(GameBoard.Direction.LEFT);
                }
            }
        } else {
            if (Math.abs(diffY) > 100 && Math.abs(velocityY) > 100) {
                if (diffY > 0) {
                    gameBoard.move(GameBoard.Direction.DOWN);
                } else {
                    gameBoard.move(GameBoard.Direction.UP);
                }
            }
        }

        updateScore();
        checkGameState();
        return true;
    }

    // Required gesture detector methods
    @Override public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
    @Override public void onLongPress(MotionEvent e) {}
    @Override public void onShowPress(MotionEvent e) {}
    @Override public boolean onSingleTapUp(MotionEvent e) { return false; }
}