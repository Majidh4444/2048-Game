package com.example.game_2048;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        ListView leaderboardList = findViewById(R.id.leaderboardList);
        ArrayList<Map<String, String>> leaderboardData = getLeaderboardData();

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                leaderboardData,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "score"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        leaderboardList.setAdapter(adapter);
    }

    private ArrayList<Map<String, String>> getLeaderboardData() {
        ArrayList<Map<String, String>> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("Leaderboard", MODE_PRIVATE);
        Map<String, ?> entries = prefs.getAll();

        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            HashMap<String, String> item = new HashMap<>();
            item.put("name", entry.getKey()); // Player name
            item.put("score", entry.getValue().toString()); // High score
            list.add(item);
        }

        // Sort by score (descending order)
        Collections.sort(list, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                try {
                    int score1 = Integer.parseInt(o1.get("score"));
                    int score2 = Integer.parseInt(o2.get("score"));
                    return Integer.compare(score2, score1); // Descending order
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });

        return list;
    }
}