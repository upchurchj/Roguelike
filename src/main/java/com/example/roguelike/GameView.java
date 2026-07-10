package com.example.roguelike;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;

public class GameView extends View {
    private Game game;
    private Paint tileWallPaint, tileFloorPaint, tileGoldPaint, playerPaint, enemyPaint, textPaint, buttonPaint, buttonPressPaint;
    private float tileSize;
    private GameListener listener;
    private Rect btnUp, btnDown, btnLeft, btnRight, btnQuit;
    private Paint overlayPaint, gameOverTextPaint, buttonLabelPaint;
    
    // Synchronized access to button press state
    private final Object buttonLock = new Object();
    private boolean btnUpPressed = false, btnDownPressed = false, btnLeftPressed = false, btnRightPressed = false;

    public interface GameListener {
        void onGameOver(Game.GameState state);
        void onQuitRequested();
    }

    public GameView(Context context) {
        super(context);
        initPaints();
        initButtons();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.d("RoguelikeApp", "GameView constructor called");
        initPaints();
        initButtons();
    }
    private void initPaints() {
        tileWallPaint = new Paint();
        tileWallPaint.setColor(Color.parseColor("#1a1a1a"));
        tileWallPaint.setStyle(Paint.Style.FILL);
        
        tileFloorPaint = new Paint();
        tileFloorPaint.setColor(Color.parseColor("#333333"));
        tileFloorPaint.setStyle(Paint.Style.FILL);
        
        tileGoldPaint = new Paint();
        tileGoldPaint.setColor(Color.parseColor("#FFD700"));
        tileGoldPaint.setStyle(Paint.Style.FILL);
        
        playerPaint = new Paint();
        playerPaint.setColor(Color.parseColor("#00FF00"));
        playerPaint.setStyle(Paint.Style.FILL);
        
        enemyPaint = new Paint();
        enemyPaint.setColor(Color.parseColor("#FF0000"));
        enemyPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36);
        textPaint.setTextAlign(Paint.Align.LEFT);
        
        buttonPaint = new Paint();
        buttonPaint.setColor(Color.parseColor("#00FF00"));
        buttonPaint.setStyle(Paint.Style.FILL);
        
        buttonPressPaint = new Paint();
        buttonPressPaint.setColor(Color.parseColor("#00AA00"));
        buttonPressPaint.setStyle(Paint.Style.FILL);
        
        overlayPaint = new Paint();
        overlayPaint.setColor(Color.argb(200, 0, 0, 0));
        
        gameOverTextPaint = new Paint();
        gameOverTextPaint.setColor(Color.WHITE);
        gameOverTextPaint.setTextSize(64);
        gameOverTextPaint.setTextAlign(Paint.Align.CENTER);
        
        buttonLabelPaint = new Paint();
        buttonLabelPaint.setColor(Color.BLACK);
        buttonLabelPaint.setTextSize(20);
        buttonLabelPaint.setTextAlign(Paint.Align.CENTER);
    }

    // Pre-allocate button Rects to avoid per-frame allocation
    private void initButtons() {
        btnUp = new Rect();
        btnDown = new Rect();
        btnLeft = new Rect();
        btnRight = new Rect();
        btnQuit = new Rect();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (game == null) {
            Log.w("GameView", "Game is null in onDraw");
            return;
        }
        
        int width = getWidth(), height = getHeight();
        int gameAreaHeight = (int) (height * 0.75);
        int buttonAreaHeight = height - gameAreaHeight;

        Dungeon dungeon = game.getDungeon();
        if (dungeon == null) {
            Log.w("GameView", "Dungeon is null in onDraw");
            return;
        }

        tileSize = (float) width / dungeon.getWidth();
        drawDungeon(canvas, dungeon);
        drawPlayer(canvas);
        drawEnemies(canvas);
        drawStatus(canvas, gameAreaHeight);
        drawButtons(canvas, gameAreaHeight, buttonAreaHeight);

        Game.GameState state = game.getState();
        if (state != Game.GameState.PLAYING) {
            drawGameOverOverlay(canvas, width, gameAreaHeight);
        }
    }

    private void drawDungeon(Canvas canvas, Dungeon dungeon) {
        for (int y = 0; y < dungeon.getHeight(); y++) {
            for (int x = 0; x < dungeon.getWidth(); x++) {
                float left = x * tileSize, top = y * tileSize, right = left + tileSize, bottom = top + tileSize;
                
                // Cache getTile result to avoid redundant calls
                int tile = dungeon.getTile(x, y);
                Paint tilePaint;
                if (tile == Dungeon.TILE_WALL) {
                    tilePaint = tileWallPaint;
                } else if (tile == Dungeon.TILE_GOLD) {
                    tilePaint = tileGoldPaint;
                } else {
                    tilePaint = tileFloorPaint;
                }
                canvas.drawRect(left, top, right, bottom, tilePaint);
            }
        }
    }

    private void drawPlayer(Canvas canvas) {
        Player player = game.getPlayer();
        if (player == null) {
            Log.w("GameView", "Player is null in drawPlayer");
            return;
        }
        
        float x = player.getX() * tileSize + tileSize / 2;
        float y = player.getY() * tileSize + tileSize / 2;
        canvas.drawCircle(x, y, tileSize * 0.35f, playerPaint);
    }

    private void drawEnemies(Canvas canvas) {
        List<Enemy> enemies = game.getEnemies();
        if (enemies == null) {
            Log.w("GameView", "Enemies list is null in drawEnemies");
            return;
        }
        
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                float x = enemy.getX() * tileSize + tileSize / 2;
                float y = enemy.getY() * tileSize + tileSize / 2;
                canvas.drawCircle(x, y, tileSize * 0.35f, enemyPaint);
            }
        }
    }

    private void drawStatus(Canvas canvas, int gameAreaHeight) {
        Player player = game.getPlayer();
        if (player == null) {
            Log.w("GameView", "Player is null in drawStatus");
            return;
        }
        
        String status = String.format("HP: %d/%d | Gold: %d | Turn: %d",
                player.getCurrentHealth(),
                player.getMaxHealth(),
                player.getGoldCollected(),
                game.getTurnCount());
        canvas.drawText(status, 10, gameAreaHeight - 10, textPaint);
    }

    private void drawButtons(Canvas canvas, int gameAreaHeight, int buttonAreaHeight) {
        int buttonWidth = getWidth() / 5;
        int buttonHeight = buttonAreaHeight / 2;
        int startY = gameAreaHeight;

        // Update pre-allocated Rect objects instead of creating new ones
        btnUp.set(0, startY, buttonWidth, startY + buttonHeight);
        btnDown.set(buttonWidth, startY, 2 * buttonWidth, startY + buttonHeight);
        btnLeft.set(2 * buttonWidth, startY, 3 * buttonWidth, startY + buttonHeight);
        btnRight.set(3 * buttonWidth, startY, 4 * buttonWidth, startY + buttonHeight);
        btnQuit.set(4 * buttonWidth, startY, 5 * buttonWidth, startY + buttonHeight);

        // Read button state under lock for thread safety
        synchronized (buttonLock) {
            drawButton(canvas, btnUp, "UP", btnUpPressed);
            drawButton(canvas, btnDown, "DOWN", btnDownPressed);
            drawButton(canvas, btnLeft, "LEFT", btnLeftPressed);
            drawButton(canvas, btnRight, "RIGHT", btnRightPressed);
        }
        drawButton(canvas, btnQuit, "QUIT", false);
    }

    private void drawButton(Canvas canvas, Rect rect, String label, boolean pressed) {
        Paint paint = pressed ? buttonPressPaint : buttonPaint;
        canvas.drawRect(rect, paint);
        canvas.drawText(label, rect.centerX(), rect.centerY() + 10, buttonLabelPaint);
    }

    private void drawGameOverOverlay(Canvas canvas, int width, int gameAreaHeight) {
        canvas.drawRect(0, 0, width, gameAreaHeight, overlayPaint);
        String message = game.getState() == Game.GameState.WON ? "YOU WIN!" : "GAME OVER";
        canvas.drawText(message, width / 2, gameAreaHeight / 2, gameOverTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX(), y = event.getY();
        
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
        if (game == null) {
            Log.w("GameView", "Game is null in handleButtonPress");
            return;
        }
        if (btnUp != null && btnUp.contains((int) x, (int) y)) {
            synchronized (buttonLock) {
                btnUpPressed = true;
            }
            game.playerMove(0, -1);
        } else if (btnDown != null && btnDown.contains((int) x, (int) y)) {
            synchronized (buttonLock) {
                btnDownPressed = true;
            }
            game.playerMove(0, 1);
        } else if (btnLeft != null && btnLeft.contains((int) x, (int) y)) {
            synchronized (buttonLock) {
                btnLeftPressed = true;
            }
            game.playerMove(-1, 0);
        } else if (btnRight != null && btnRight.contains((int) x, (int) y)) {
            synchronized (buttonLock) {
                btnRightPressed = true;
            }
            game.playerMove(1, 0);
        } else if (btnQuit != null && btnQuit.contains((int) x, (int) y)) {
            if (listener != null) {
                listener.onQuitRequested();
            }
        }

        Game.GameState state = game.getState();
        if (state != Game.GameState.PLAYING && listener != null) {
            listener.onGameOver(state);
        }
    }

    private void handleButtonRelease() {
        synchronized (buttonLock) {
            btnUpPressed = false;
            btnDownPressed = false;
            btnLeftPressed = false;
            btnRightPressed = false;
        }
    }
}
