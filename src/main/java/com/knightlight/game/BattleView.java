package com.knightlight.game;

import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class BattleView extends View {
    private Battle battle;
    private Paint healthBarBackPaint, healthBarFillPaint, textPaint;

    public BattleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("RoguelikeApp", "BattleView constructor called");
        initPaints();
    }

    public BattleView(Context context, Battle battle) {
        super(context);
        this.battle = battle;
        initPaints();
    }

    public void setBattle(Battle battle) {
        this.battle = battle;
        invalidate();
    }

    private void initPaints() {
        healthBarBackPaint = new Paint();
        healthBarBackPaint.setColor(Color.parseColor("#333333"));
        healthBarBackPaint.setStyle(Paint.Style.FILL);

        healthBarFillPaint = new Paint();
        healthBarFillPaint.setColor(Color.parseColor("#00FF00"));
        healthBarFillPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (battle == null) {
            Log.w("BattleView", "Battle is null in onDraw");
            return;
        }

        int height = getHeight();
        int statusAreaHeight = (int) (height * 0.75);

        drawHealthBars(canvas, statusAreaHeight);
        drawMessage(canvas, statusAreaHeight / 2);
    }

    private void drawHealthBars(Canvas canvas, int statusAreaHeight) {
        Player player = battle.getPlayer();
        Enemy enemy = battle.getEnemy();

        if (player == null || enemy == null) {
            Log.w("BattleView", "Player or Enemy is null in drawHealthBars");
            return;
        }

        int barWidth = getWidth() / 2 - 20;
        int barHeight = 30;
        int barX = 10;

        // Player health bar (always draw)
        int playerMaxHealth = player.getMaxHealth();
        int playerCurrentHealth = Math.min(player.getCurrentHealth(), playerMaxHealth);
        playerCurrentHealth = Math.max(0, playerCurrentHealth);

        int playerBarY = 10;
        canvas.drawRect(barX, playerBarY, barX + barWidth, playerBarY + barHeight, healthBarBackPaint);
        float playerHealthRatio = (float) playerCurrentHealth / playerMaxHealth;
        canvas.drawRect(barX, playerBarY, barX + (int) (barWidth * playerHealthRatio), playerBarY + barHeight, healthBarFillPaint);
        canvas.drawText("Player: " + playerCurrentHealth + "/" + playerMaxHealth, barX + 5, playerBarY + 22, textPaint);

        // Enemy health bar (only draw if alive)
        int enemyHealth = enemy.getHealth();
        if (enemyHealth > 0) {
            int enemyMaxHealth = enemy.getMaxHealth();
            int enemyCurrentHealth = Math.min(enemyHealth, enemyMaxHealth);
            enemyCurrentHealth = Math.max(0, enemyCurrentHealth);

            int enemyBarX = barX + barWidth + 20;
            int enemyBarY = 10;
            canvas.drawRect(enemyBarX, enemyBarY, enemyBarX + barWidth, enemyBarY + barHeight, healthBarBackPaint);
            float enemyHealthRatio = (float) enemyCurrentHealth / enemyMaxHealth;
            canvas.drawRect(enemyBarX, enemyBarY, enemyBarX + (int) (barWidth * enemyHealthRatio), enemyBarY + barHeight, healthBarFillPaint);
            canvas.drawText("Enemy: " + enemyCurrentHealth + "/" + enemyMaxHealth, enemyBarX + 5, enemyBarY + 22, textPaint);
        }
    }

    private void drawMessage(Canvas canvas, int y) {
        if (battle == null) {
            Log.w("BattleView", "Battle is null in drawMessage");
            return;
        }

        String message = battle.getLastMessage();
        if (message == null) {
            message = "Battle in progress...";
        }

        canvas.drawText(message, 10, y, textPaint);
    }
}
