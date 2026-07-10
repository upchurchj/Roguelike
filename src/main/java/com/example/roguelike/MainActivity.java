package com.example.roguelike;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements GameView.GameListener {
    private static final String LOG_FILE = "/sdcard/roguelike_debug.log";
    private static final String TAG = "MainActivity";
    private static final int DUNGEON_WIDTH = 40;
    private static final int DUNGEON_HEIGHT = 16;
    private static final int INITIAL_ENEMIES = 5;
    private static final int BATTLE_REQUEST_CODE = 100;

    private Game game;
    private GameView gameView;
    private AlertDialog currentDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        writeLog("onCreate() START");
        writeLog("About to create Game");
        writeLog("MainActivity.onCreate() START");
        super.onCreate(savedInstanceState);
        writeLog("super.onCreate() OK");
        
        try {
            writeLog("About to call setContentView()");
            setContentView(R.layout.activity_main);
        Log.d("RoguelikeApp", "Setting content view");
            writeLog("setContentView() completed successfully");
        } catch (Exception e) {
            writeLog("CRASH during setContentView(): " + e.getClass().getName() + " - " + e.getMessage());
            Log.e(TAG, "CRASH during setContentView()", e);
            e.printStackTrace();
            finish();
            return;
        }

        Log.d(TAG, "Layout inflated");

        // Initialize game if not already created
        if (savedInstanceState == null) {
            Log.d(TAG, "Creating new Game with dimensions " + DUNGEON_WIDTH + "x" + DUNGEON_HEIGHT + ", enemies=" + INITIAL_ENEMIES);
            try {
                game = new Game(DUNGEON_WIDTH, DUNGEON_HEIGHT, INITIAL_ENEMIES);
                writeLog("Game created successfully");
                Log.d(TAG, "Game created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Exception during Game creation", e);
                writeLog("Exception during Game creation: " + e.getMessage());
                throw new RuntimeException("Failed to create Game", e);
            }
        } else {
            Log.d(TAG, "savedInstanceState is not null, game not recreated");
        }

        // Retrieve GameView from layout
        gameView = findViewById(R.id.game_view);
        writeLog("GameView findViewById done");
        if (gameView == null) {
            Log.e(TAG, "GameView not found in layout");
            writeLog("ERROR: GameView not found in layout");
            finish();
            return;
        }

        if (game == null) {
            Log.e(TAG, "Game is null after onCreate");
            finish();
            return;
        }

        Log.d(TAG, "Setting game on GameView and attaching listener");
        gameView.setGame(game);
        Log.d("MainActivity", "GameView.setGame() called");
        gameView.setGameListener(this);
        writeLog("onCreate() COMPLETE");
        Log.d(TAG, "onCreate completed successfully");
    }

    @Override
    public void onGameOver(Game.GameState state) {
        if (game == null) {
            Log.w(TAG, "Game is null in onGameOver");
            return;
        }

        String title, message;
        if (state == Game.GameState.WON) {
            title = "Victory!";
            message = "You've collected 100 gold and won the game!";
        } else {
            title = "Defeat";
            message = "You were defeated. Game Over.";
        }

        showGameOverDialog(title, message);
    }

    @Override
    public void onQuitRequested() {
        showQuitConfirmDialog();
    }

    private void showGameOverDialog(String title, String message) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        currentDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Play Again", (dialog, which) -> {
                    dialog.dismiss();
                    currentDialog = null;
                    restartGame();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    dialog.dismiss();
                    currentDialog = null;
                    finish();
                })
                .show();
    }

    private void showQuitConfirmDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        currentDialog = new AlertDialog.Builder(this)
                .setTitle("Quit Game?")
                .setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    currentDialog = null;
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    currentDialog = null;
                })
                .show();
    }

    private void restartGame() {
        game = new Game(DUNGEON_WIDTH, DUNGEON_HEIGHT, INITIAL_ENEMIES);
        writeLog("Game created successfully");
        if (gameView != null) {
            gameView.setGame(game);
            Log.d("MainActivity", "GameView.setGame() called");
            gameView.invalidate();
        }
        Log.d(TAG, "Game restarted");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    @Override
    protected void onDestroy() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }

        if (gameView != null) {
            gameView.setGameListener(null);
        }

        game = null;
        gameView = null;

        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState called");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState called");
    }

    private void writeLog(String msg) {
        try (java.io.FileWriter fw = new java.io.FileWriter(LOG_FILE, true)) {
            fw.write(System.currentTimeMillis() + " " + msg + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
