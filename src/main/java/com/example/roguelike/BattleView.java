package com.example.roguelike;
import android.util.AttributeSet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class BattleView extends View {
    private Battle battle;
    private Paint healthBarBackPaint, healthBarFillPaint, textPaint, buttonPaint, buttonPressPaint, buttonLabelPaint;
    private Rect btnAttack, btnDefend, btnItem, btnRun;
    private final Object buttonLock = new Object();
    private boolean btnAttackPressed = false, btnDefendPressed = false, btnItemPressed = false, btnRunPressed = false;

    public BattleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("RoguelikeApp", "BattleView constructor called");
        initPaints();
        initButtons();
    }
    public BattleView(Context context, Battle battle) {
        super(context);
        this.battle = battle;
        initPaints();
        initButtons();
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

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.parseColor("#0066FF"));
        buttonPaint.setStyle(Paint.Style.FILL);

        buttonPressPaint = new Paint();
        buttonPressPaint.setColor(Color.parseColor("#0044AA"));
        buttonPressPaint.setStyle(Paint.Style.FILL);

        buttonLabelPaint = new Paint();
        buttonLabelPaint.setColor(Color.WHITE);
        buttonLabelPaint.setTextSize(20);
        buttonLabelPaint.setTextAlign(Paint.Align.CENTER);
    }

    // Pre-allocate button Rects to avoid per-frame allocation
    private void initButtons() {
        btnAttack = new Rect();
        btnDefend = new Rect();
        btnItem = new Rect();
        btnRun = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (battle == null) {
            Log.w("BattleView", "Battle is null in onDraw");
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int buttonAreaHeight = (int) (height * 0.25);
        int statusAreaHeight = height - buttonAreaHeight;

        // Draw battle status and health bars
        drawHealthBars(canvas, statusAreaHeight);
        drawMessage(canvas, statusAreaHeight / 2);
        drawButtons(canvas, statusAreaHeight, buttonAreaHeight);
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

        // Player health bar (top-left)
        int playerMaxHealth = player.getMaxHealth();
        int playerCurrentHealth = Math.min(player.getCurrentHealth(), playerMaxHealth);
        playerCurrentHealth = Math.max(0, playerCurrentHealth);

        int playerBarY = 10;
        canvas.drawRect(barX, playerBarY, barX + barWidth, playerBarY + barHeight, healthBarBackPaint);
        float playerHealthRatio = (float) playerCurrentHealth / playerMaxHealth;
        canvas.drawRect(barX, playerBarY, barX + (int) (barWidth * playerHealthRatio), playerBarY + barHeight, healthBarFillPaint);
        canvas.drawText("Player: " + playerCurrentHealth + "/" + playerMaxHealth, barX + 5, playerBarY + 22, textPaint);

        // Enemy health bar (top-right)
        int enemyMaxHealth = enemy.getMaxHealth();
        int enemyCurrentHealth = Math.min(enemy.getHealth(), enemyMaxHealth);
        enemyCurrentHealth = Math.max(0, enemyCurrentHealth);

        int enemyBarX = barX + barWidth + 20;
        int enemyBarY = 10;
        canvas.drawRect(enemyBarX, enemyBarY, enemyBarX + barWidth, enemyBarY + barHeight, healthBarBackPaint);
        float enemyHealthRatio = (float) enemyCurrentHealth / enemyMaxHealth;
        canvas.drawRect(enemyBarX, enemyBarY, enemyBarX + (int) (barWidth * enemyHealthRatio), enemyBarY + barHeight, healthBarFillPaint);
        canvas.drawText("Enemy: " + enemyCurrentHealth + "/" + enemyMaxHealth, enemyBarX + 5, enemyBarY + 22, textPaint);
    }

    private void drawMessage(Canvas canvas, int y) {
        if (battle == null) {
            Log.w("BattleView", "Battle is null in drawMessage");
            return;
        }

        // Cache message to avoid repeated calls
        String message = battle.getLastMessage();
        if (message == null) {
            message = "Battle in progress...";
        }

        canvas.drawText(message, 10, y, textPaint);
    }

    private void drawButtons(Canvas canvas, int statusAreaHeight, int buttonAreaHeight) {
        int buttonWidth = getWidth() / 4;
        int buttonHeight = buttonAreaHeight / 2;
        int startY = statusAreaHeight;

        // Update pre-allocated Rect objects
        btnAttack.set(0, startY, buttonWidth, startY + buttonHeight);
        btnDefend.set(buttonWidth, startY, 2 * buttonWidth, startY + buttonHeight);
        btnItem.set(2 * buttonWidth, startY, 3 * buttonWidth, startY + buttonHeight);
        btnRun.set(3 * buttonWidth, startY, 4 * buttonWidth, startY + buttonHeight);

        // Read button state under lock for thread safety
        synchronized (buttonLock) {
            drawButton(canvas, btnAttack, "ATTACK", btnAttackPressed);
            drawButton(canvas, btnDefend, "DEFEND", btnDefendPressed);
            drawButton(canvas, btnItem, "ITEM", btnItemPressed);
            drawButton(canvas, btnRun, "RUN", btnRunPressed);
        }
    }

    private void drawButton(Canvas canvas, Rect rect, String label, boolean pressed) {
        Paint paint = pressed ? buttonPressPaint : buttonPaint;
        canvas.drawRect(rect, paint);
        canvas.drawText(label, rect.centerX(), rect.centerY() + 10, buttonLabelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                handleButtonPress(x, y);
                break;
            case MotionEvent.ACTION_UP:
                handleButtonRelease();
                break;
        }

        invalidate();
        return true;
    }

    private void handleButtonPress(float x, float y) {
        if (battle == null) {
            Log.w("BattleView", "Battle is null in handleButtonPress");
            return;
        }

        int ix = (int) x;
        int iy = (int) y;

        if (btnAttack != null && btnAttack.contains(ix, iy)) {
            synchronized (buttonLock) {
                btnAttackPressed = true;
            }
            battle.playerAction(Battle.Action.ATTACK);
        } else if (btnDefend != null && btnDefend.contains(ix, iy)) {
            synchronized (buttonLock) {
                btnDefendPressed = true;
            }
            battle.playerAction(Battle.Action.DEFEND);
        } else if (btnItem != null && btnItem.contains(ix, iy)) {
            synchronized (buttonLock) {
                btnItemPressed = true;
            }
            battle.playerAction(Battle.Action.ITEM);
        } else if (btnRun != null && btnRun.contains(ix, iy)) {
            synchronized (buttonLock) {
                btnRunPressed = true;
            }
            battle.playerAction(Battle.Action.RUN);
        }
    }

    private void handleButtonRelease() {
        synchronized (buttonLock) {
            btnAttackPressed = false;
            btnDefendPressed = false;
            btnItemPressed = false;
            btnRunPressed = false;
        }
    }
}
