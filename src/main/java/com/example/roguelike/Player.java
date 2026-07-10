package com.example.roguelike;

import java.io.Serializable;
import android.util.Log;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private int x;
    private int y;
    private int maxHealth;
    private int currentHealth;
    private int goldCollected;
    private int healthPotions;
    private final Object stateLock = new Object();

    public Player(int startX, int startY) {
        if (startX < 0 || startY < 0) {
            Log.w("Player", "Negative starting position: (" + startX + ", " + startY + ")");
        }
        this.x = startX;
        this.y = startY;
        this.maxHealth = 30;
        
        if (this.maxHealth <= 0) {
            Log.w("Player", "Invalid maxHealth: " + this.maxHealth + "; defaulting to 30");
            this.maxHealth = 30;
        }
        
        this.currentHealth = this.maxHealth;
        this.goldCollected = 0;
        this.healthPotions = 3;
    }

    public synchronized void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public synchronized void setPosition(int newX, int newY) {
        if (newX < 0 || newY < 0) {
            Log.w("Player", "setPosition called with negative values: (" + newX + ", " + newY + ")");
        }
        x = newX;
        y = newY;
    }

    public synchronized int getX() {
        return x;
    }

    public synchronized int getY() {
        return y;
    }

    public synchronized int getHealth() {
        return currentHealth;
    }

    public synchronized int getCurrentHealth() {
        return currentHealth;
    }

    public synchronized int getMaxHealth() {
        return maxHealth;
    }

    public synchronized void takeDamage(int damage) {
        int oldHealth = currentHealth;
        currentHealth = Math.max(0, currentHealth - damage);
        
        if (currentHealth != oldHealth) {
            Log.d("Player", "Took " + damage + " damage: " + oldHealth + " -> " + currentHealth);
        }
    }

    public synchronized void heal(int amount) {
        int oldHealth = currentHealth;
        currentHealth = Math.min(currentHealth + amount, maxHealth);
        
        if (currentHealth != oldHealth) {
            Log.d("Player", "Healed " + (currentHealth - oldHealth) + " HP: " + oldHealth + " -> " + currentHealth);
        }
    }

    public synchronized void addGold(int amount) {
        if (amount < 0) {
            Log.w("Player", "addGold called with negative amount: " + amount);
            return;
        }
        int oldGold = goldCollected;
        goldCollected += amount;
        Log.d("Player", "Collected " + amount + " gold: " + oldGold + " -> " + goldCollected);
    }

    public synchronized int getGold() {
        return goldCollected;
    }

    public synchronized int getGoldCollected() {
        return goldCollected;
    }

    public synchronized int getHealthPotions() {
        return healthPotions;
    }

    public synchronized boolean usePotion() {
        if (healthPotions > 0) {
            healthPotions--;
            int healAmount = 15;
            int oldHealth = currentHealth;
            currentHealth = Math.min(currentHealth + healAmount, maxHealth);
            Log.d("Player", "Used potion: " + oldHealth + " -> " + currentHealth);
            return true;
        } else {
            Log.w("Player", "Attempted to use potion but none available");
            return false;
        }
    }

    public synchronized boolean isAlive() {
        return currentHealth > 0;
    }

    public synchronized void reset() {
        currentHealth = maxHealth;
        goldCollected = 0;
        healthPotions = 3;
        Log.d("Player", "Player reset to initial state");
    }
}
