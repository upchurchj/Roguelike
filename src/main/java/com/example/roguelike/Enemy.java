package com.example.roguelike;

import java.io.Serializable;
import android.util.Log;

public class Enemy implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int x;
    private int y;
    private int health;
    private int maxHealth;
    private int attackPower;
    private Dungeon dungeon;
    private boolean boss;

    public Enemy(int x, int y, int health, int attackPower, Dungeon dungeon) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.maxHealth = health;
        this.attackPower = attackPower;
        this.dungeon = dungeon;
        this.boss = false;
    }

    public synchronized int getX() { return x; }
    public synchronized int getY() { return y; }
    public synchronized int getHealth() { return health; }
    public synchronized int getMaxHealth() { return maxHealth; }
    public synchronized int getAttack() { return attackPower; }
    public int getGoldReward() { return boss ? 100 : 25; }
    public synchronized boolean isAlive() { return health > 0; }
    public boolean isBoss() { return boss; }

    public static Enemy createBoss(int x, int y, Dungeon dungeon) {
        Enemy boss = new Enemy(x, y, 50, 5, dungeon);
        boss.boss = true;
        return boss;
    }

    public synchronized void takeDamage(int damage) {
        health = Math.max(0, health - damage);
    }

    public synchronized void moveToward(Player player) {
        if (player == null || dungeon == null) {
            Log.w("Enemy", "moveToward called with null player or dungeon");
            return;
        }
        int playerX = player.getX();
        int playerY = player.getY();
        if (x < playerX && dungeon.isWalkable(x + 1, y)) x++;
        else if (x > playerX && dungeon.isWalkable(x - 1, y)) x--;
        else if (y < playerY && dungeon.isWalkable(x, y + 1)) y++;
        else if (y > playerY && dungeon.isWalkable(x, y - 1)) y--;
    }
}
