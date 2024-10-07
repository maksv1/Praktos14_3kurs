// MainActivity.java
package com.example.tictactoegame;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    private Button[] buttons = new Button[9];
    private int[] board = new int[9]; // 0 - пусто, 1 - X, 2 - O
    private boolean playerTurn = true; // true - X, false - O
    private boolean gameActive = true;
    private TextView statusTextView;
    private TextView statisticsTextView;

    private SharedPreferences preferences;
    private SharedPreferences themePreferences;
    private int wins = 0;
    private int losses = 0;
    private int draws = 0;

    private Switch themeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Загружаем тему перед вызовом super.onCreate
        themePreferences = getSharedPreferences("theme_preferences", MODE_PRIVATE);
        if (themePreferences.getBoolean("dark_theme", false)) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("game_statistics", MODE_PRIVATE);
        loadStatistics();

        statusTextView = findViewById(R.id.statusTextView);
        statisticsTextView = findViewById(R.id.statisticsTextView);
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        themeSwitch = findViewById(R.id.themeSwitch);

        // Устанавливаем состояние переключателя темы
        themeSwitch.setChecked(themePreferences.getBoolean("dark_theme", false));

        for (int i = 0; i < 9; i++) {
            String buttonID = "button" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            buttons[i] = findViewById(resID);
            final int index = i;
            buttons[i].setOnClickListener(v -> onButtonClicked(index));
        }

        findViewById(R.id.resetButton).setOnClickListener(v -> resetGame());

        updateStatistics();

        // Обработка переключателя темы
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = themePreferences.edit();
            editor.putBoolean("dark_theme", isChecked);
            editor.apply();
            recreate(); // Перезагрузка активности для применения темы
        });
    }

    private void onButtonClicked(int index) {
        if (!gameActive || board[index] != 0) {
            return;
        }

        board[index] = playerTurn ? 1 : 2;
        buttons[index].setText(playerTurn ? "X" : "O");

        if (checkWinner()) {
            gameActive = false;
            if (playerTurn) {
                statusTextView.setText("Игрок X победил!");
                wins++;
            } else {
                statusTextView.setText("Игрок O победил!");
                losses++;
            }
            saveStatistics();
        } else if (isBoardFull()) {
            statusTextView.setText("Ничья!");
            draws++;
            gameActive = false;
            saveStatistics();
        } else {
            playerTurn = !playerTurn;
            statusTextView.setText(playerTurn ? "Игрок X ходит" : "Игрок O ходит");
        }
    }

    private boolean checkWinner() {
        int[][] winPositions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Ряды
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Колонки
                {0, 4, 8}, {2, 4, 6}  // Диагонали
        };

        for (int[] pos : winPositions) {
            if (board[pos[0]] == board[pos[1]] && board[pos[1]] == board[pos[2]] && board[pos[0]] != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int value : board) {
            if (value == 0) {
                return false;
            }
        }
        return true;
    }

    private void resetGame() {
        gameActive = true;
        playerTurn = true;
        statusTextView.setText("Игрок X ходит");

        for (int i = 0; i < 9; i++) {
            board[i] = 0;
            buttons[i].setText("");
        }
    }

    private void loadStatistics() {
        wins = preferences.getInt("wins", 0);
        losses = preferences.getInt("losses", 0);
        draws = preferences.getInt("draws", 0);
    }

    private void saveStatistics() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("wins", wins);
        editor.putInt("losses", losses);
        editor.putInt("draws", draws);
        editor.apply();
        updateStatistics();
    }

    private void updateStatistics() {
        statisticsTextView.setText("Победы: " + wins + ", Поражения: " + losses + ", Ничьи: " + draws);
    }
}
