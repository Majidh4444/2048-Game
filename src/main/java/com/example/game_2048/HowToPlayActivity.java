package com.example.game_2048;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class HowToPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        // Enable the back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("How to Play");
        }

        // Find the container for dynamic content
        LinearLayout contentContainer = findViewById(R.id.howToPlayContent);

        // Add sections dynamically
        addSection(contentContainer, "Game Objective",
                "Combine tiles with the same numbers to create a tile with the number 2048! " +
                        "Keep combining tiles to achieve higher scores.");

        addSection(contentContainer, "How to Move",
                "Swipe in any direction (up, down, left, or right) to move all tiles. " +
                        "Tiles with the same number will merge when they touch. " +
                        "After each move, a new tile with a value of 2 or 4 will appear.");

        addSection(contentContainer, "Scoring",
                "• When two tiles merge, their values are added to your score\n" +
                        "• Try to achieve the highest score possible\n" +
                        "• Your best score is saved automatically");

        addSection(contentContainer, "Game Over",
                "The game ends when:\n" +
                        "• You create a 2048 tile (You win!)\n" +
                        "• No more moves are possible (board is full and no merges are possible)");

        addSection(contentContainer, "Tips",
                "1. Keep your highest value tiles in a corner\n" +
                        "2. Focus on maintaining a clear strategy for merging\n" +
                        "3. Don't scatter high-value tiles across the board\n" +
                        "4. Try to keep a path open for merging tiles");

        addSection(contentContainer, "Controls",
                "• Swipe Up: Move tiles upward\n" +
                        "• Swipe Down: Move tiles downward\n" +
                        "• Swipe Left: Move tiles to the left\n" +
                        "• Swipe Right: Move tiles to the right\n" +
                        "• Menu Button: Access New Game and Leaderboard");
    }

    private void addSection(LinearLayout container, String title, String content) {
        // Create and style the title
        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(20);
        titleView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        titleView.setPadding(0, 32, 0, 16);
        titleView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);

        // Create and style the content
        TextView contentView = new TextView(this);
        contentView.setText(content);
        contentView.setTextSize(16);
        contentView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        contentView.setPadding(0, 0, 0, 16);
        contentView.setLineSpacing(8, 1);
        contentView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);

        // Add views to container
        container.addView(titleView);
        container.addView(contentView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}