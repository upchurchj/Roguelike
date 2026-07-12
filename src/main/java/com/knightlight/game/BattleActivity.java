package com.knightlight.game;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class BattleActivity extends AppCompatActivity {
    private static final String EXTRA_PLAYER = "player";
    private static final String EXTRA_ENEMY = "enemy";
    private static final long TURN_DELAY_MS = 1000;
    private static final long RESULT_DELAY_MS = 2000;

    private Battle battle;
    private BattleView battleView;
    private Player player;
    private Enemy enemy;
    private Handler handler;
    private boolean isDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        battleView = findViewById(R.id.battle_view);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        handler.removeCallbacksAndMessages(null);
    }

    public void playerAction(Battle.Action action) {
        if (battle == null || isDestroyed) return;

        synchronized (battle) {
            battle.playerAction(action);
        }

        handler.postDelayed(() -> {
            if (!isDestroyed) {
                battleView.invalidate();

                synchronized (battle) {
                    if (battle.getState() == Battle.State.VICTORY || 
                        battle.getState() == Battle.State.DEFEAT ||
                        battle.getState() == Battle.State.FLED) {
                        handler.postDelayed(this::showBattleResult, RESULT_DELAY_MS);
                    } else {
                        handler.postDelayed(this::processTurn, TURN_DELAY_MS);
                    }
                }
            }
        }, TURN_DELAY_MS);
    }

    private void processTurn() {
        if (battle == null || isDestroyed) return;

        synchronized (battle) {
            battle.enemyTurn();
        }

        battleView.invalidate();

        synchronized (battle) {
            if (battle.getState() == Battle.State.VICTORY || 
                battle.getState() == Battle.State.DEFEAT ||
                battle.getState() == Battle.State.FLED) {
                handler.postDelayed(this::showBattleResult, RESULT_DELAY_MS);
            }
        }
    }

    private void showBattleResult() {
        if (isDestroyed) return;

        synchronized (battle) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("player", player);
            resultIntent.putExtra("battle_won", battle.getState() == Battle.State.VICTORY);
            setResult(RESULT_OK, resultIntent);
        }

        finish();
    }
}
