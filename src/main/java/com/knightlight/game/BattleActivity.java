package com.knightlight.game;

import android.widget.Toast;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class BattleActivity extends AppCompatActivity {
    private static final String EXTRA_PLAYER = "player";
    private static final String EXTRA_ENEMY = "enemy";
    private static final long TURN_DELAY_MS = 1000;
    private static final long RESULT_DELAY_MS = 2000;
    private static final String TAG = "BattleActivity";

    private Battle battle;
    private BattleView battleView;
    private Player player;
    private Enemy enemy;
    private Handler handler;
    private boolean isDestroyed = false;

    private Button btnAttack, btnDefend, btnFlee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        battleView = findViewById(R.id.battle_view);
        if (battleView == null) {
            Log.e(TAG, "BattleView not found in layout");
            finish();
            return;
        }

        handler = new Handler(Looper.getMainLooper());

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            player = (Player) extras.getSerializable(EXTRA_PLAYER);
            enemy = (Enemy) extras.getSerializable(EXTRA_ENEMY);
        }

        if (player != null && enemy != null) {
            battle = new Battle(player, enemy);
            battleView.setBattle(battle);
        }

        // Wire up buttons
        btnAttack = findViewById(R.id.btnAttack);
        btnDefend = findViewById(R.id.btnDefend);
        btnFlee = findViewById(R.id.btnFlee);

        if (btnAttack != null) {
            btnAttack.setOnClickListener(v -> playerAction(Battle.Action.ATTACK));
        }
        if (btnDefend != null) {
            btnDefend.setOnClickListener(v -> playerAction(Battle.Action.DEFEND));
        }
        if (btnFlee != null) {
            btnFlee.setOnClickListener(v -> playerAction(Battle.Action.RUN));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        handler.removeCallbacksAndMessages(null);
    }

    public void playerAction(Battle.Action action) {
        if (battle == null || isDestroyed) {
            Log.w(TAG, "playerAction: battle is null or activity destroyed");
            return;
        }

        Log.d(TAG, "Player action: " + action);
        battle.playerAction(action);
        battleView.invalidate();

        // Schedule enemy turn after player animation
        handler.postDelayed(this::processTurn, TURN_DELAY_MS);
    }

    private void processTurn() {
        if (battle == null || isDestroyed) {
            return;
        }

        Log.d(TAG, "Processing enemy turn");
        battle.enemyTurn();
        battleView.invalidate();

        Log.d(TAG, "After enemy turn - Battle state: " + battle.getState());
        if (isBattleOver()) {
            Log.d(TAG, "Battle over after enemy action");
            handler.postDelayed(this::showBattleResult, RESULT_DELAY_MS);
        }
    }

    private boolean isBattleOver() {
        if (battle == null) return false;
        Battle.State state = battle.getState();
        return state == Battle.State.VICTORY ||
               state == Battle.State.DEFEAT ||
               state == Battle.State.FLED;
    }

    private void showBattleResult() {
        Log.d(TAG, "Showing battle result");
        if (isDestroyed) return;

        Intent resultIntent = new Intent();
        resultIntent.putExtra("player", player);
        resultIntent.putExtra("battle_won", battle.getState() == Battle.State.VICTORY);
        setResult(RESULT_OK, resultIntent);

        finish();
    }
}
