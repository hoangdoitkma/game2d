package com.example.templerunclone;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.templerunclone.managers.HighScoreManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HighScoreActivity extends Activity {
    private HighScoreManager highScoreManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        highScoreManager = new HighScoreManager(this);
        
        setupUI();
    }
    
    private void setupUI() {
        // Create main layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.BLACK);
        mainLayout.setPadding(40, 40, 40, 40);
        
        // Title
        TextView titleView = new TextView(this);
        titleView.setText("HIGH SCORES");
        titleView.setTextColor(Color.rgb(255, 215, 0)); // Gold color
        titleView.setTextSize(36);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setGravity(android.view.Gravity.CENTER);
        titleView.setPadding(0, 0, 0, 40);
        mainLayout.addView(titleView);
        
        // High scores list
        List<HighScoreManager.HighScore> scores = highScoreManager.getHighScores();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        for (int i = 0; i < Math.max(8, scores.size()); i++) {
            LinearLayout scoreLayout = new LinearLayout(this);
            scoreLayout.setOrientation(LinearLayout.HORIZONTAL);
            scoreLayout.setPadding(0, 10, 0, 10);
            
            // Rank
            TextView rankView = new TextView(this);
            rankView.setText(String.valueOf(i + 1) + ".");
            rankView.setTextColor(Color.WHITE);
            rankView.setTextSize(24);
            rankView.setMinWidth(80);
            rankView.setTypeface(Typeface.DEFAULT_BOLD);
            scoreLayout.addView(rankView);
            
            if (i < scores.size()) {
                HighScoreManager.HighScore score = scores.get(i);
                
                // Score
                TextView scoreView = new TextView(this);
                scoreView.setText(String.valueOf(score.score));
                scoreView.setTextColor(i < 3 ? Color.rgb(255, 215, 0) : Color.WHITE); // Gold for top 3
                scoreView.setTextSize(24);
                scoreView.setTypeface(i < 3 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                scoreView.setMinWidth(200);
                scoreLayout.addView(scoreView);
                
                // Level
                TextView levelView = new TextView(this);
                levelView.setText("Level " + score.level);
                levelView.setTextColor(Color.CYAN);
                levelView.setTextSize(20);
                levelView.setMinWidth(150);
                scoreLayout.addView(levelView);
                
                // Date
                TextView dateView = new TextView(this);
                dateView.setText(dateFormat.format(new Date(score.timestamp)));
                dateView.setTextColor(Color.GRAY);
                dateView.setTextSize(16);
                scoreLayout.addView(dateView);
            } else {
                // Empty slot
                TextView emptyView = new TextView(this);
                emptyView.setText("---");
                emptyView.setTextColor(Color.GRAY);
                emptyView.setTextSize(24);
                scoreLayout.addView(emptyView);
            }
            
            mainLayout.addView(scoreLayout);
        }
        
        // Back button
        Button backButton = new Button(this);
        backButton.setText("BACK TO MENU");
        backButton.setBackgroundColor(Color.BLUE);
        backButton.setTextColor(Color.WHITE);
        backButton.setTextSize(20);
        backButton.setTypeface(Typeface.DEFAULT_BOLD);
        backButton.setPadding(40, 20, 40, 20);
        
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.topMargin = 40;
        backButton.setLayoutParams(buttonParams);
        
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        mainLayout.addView(backButton);
        
        // Clear scores button (for testing)
        Button clearButton = new Button(this);
        clearButton.setText("CLEAR SCORES");
        clearButton.setBackgroundColor(Color.RED);
        clearButton.setTextColor(Color.WHITE);
        clearButton.setTextSize(16);
        clearButton.setPadding(40, 20, 40, 20);
        
        LinearLayout.LayoutParams clearButtonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        clearButtonParams.topMargin = 20;
        clearButton.setLayoutParams(clearButtonParams);
        
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highScoreManager.clearHighScores();
                setupUI(); // Refresh the UI
            }
        });
        
        mainLayout.addView(clearButton);
        
        setContentView(mainLayout);
    }
}