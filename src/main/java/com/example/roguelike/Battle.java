package com.example.roguelike;

import java.util.Random;
import android.util.Log;

public class Battle {
    public enum Action { ATTACK, DEFEND, ITEM, RUN }
    public enum State { PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT, FLED }

    private Player player;
    private Enemy enemy;
    private State state;
    private String lastMessage;
    private boolean defending;
    private Random random;
    private final Object stateLock = new Object();

    public Battle(Player player, Enemy enemy) {
        if (player == null || enemy == null) {
            Log.e("Battle", "Battle constructor called with null player or enemy");
            throw new IllegalArgumentException("Player and Enemy cannot be null");
        }
        this.player = player;
        this.enemy = enemy;
        this.state = State.PLAYER_TURN;
        this.defending = false;
        this.lastMessage = "Battle started!";
        this.random = new Random(System.nanoTime());
    }

    public void playerAction(Action action) {
        if (action == null) {
            Log.w("Battle", "playerAction called with null action");
            return;
        }

        synchronized (stateLock) {
            if (state != State.PLAYER_TURN) {
                Log.w("Battle", "playerAction called when state is " + state);
                return;
            }
            defending = false;

            switch (action) {
                case ATTACK:
                    playerAttack();
                    break;
                case DEFEND:
                    defending = true;
                    lastMessage = "You brace for impact!";
                    break;
                case ITEM:
                    usePotion();
                    break;
                case RUN:
                    if (random.nextDouble() < 0.5) {
                        state = State.FLED;
                        lastMessage = "You escaped!";
                        return;
                    } else {
                        lastMessage = "Couldn't escape!";
                    }
                    break;
            }

            if (state != State.FLED && state != State.VICTORY) {
                state = State.ENEMY_TURN;
            }
        }
    }

    public void enemyTurn() {
        synchronized (stateLock) {
            if (state != State.ENEMY_TURN) {
                Log.w("Battle", "enemyTurn called when state is " + state);
                return;
            }

            enemyAttack();

            if (player.getHealth() <= 0) {
                state = State.DEFEAT;
                lastMessage = "You were defeated...";
            } else {
                defending = false;
                state = State.PLAYER_TURN;
            }
        }
    }

    private void playerAttack() {
        // Damage range: 8-12 (base 8, variance 0-4)
        int baseDamage = 8;
        int variance = random.nextInt(5);
        int damage = baseDamage + variance;
        enemy.takeDamage(damage);
        lastMessage = "You deal " + damage + " damage!";

        if (!enemy.isAlive()) {
            state = State.VICTORY;
            int goldReward = enemy.getGoldReward();
            player.addGold(goldReward);
            lastMessage = "Victory! You gained " + goldReward + " gold!";

            if (enemy.isBoss()) {
                int bossBonus = 50;
                player.addGold(bossBonus);
                lastMessage += " (Boss bonus: +" + bossBonus + " gold!)";
            }
        }
    }

    private void enemyAttack() {
        int enemyAttackPower = enemy.getAttack();
        int baseDamage = enemyAttackPower;
        int variance = random.nextInt(3);
        int damage = baseDamage + variance;

        if (defending) {
            damage = Math.max(1, damage / 2);
            lastMessage = "Enemy deals " + damage + " damage (reduced)!";
        } else {
            lastMessage = "Enemy deals " + damage + " damage!";
        }

        player.takeDamage(damage);
    }

    private void usePotion() {
        if (player.getHealthPotions() > 0) {
            int healAmount = 30;
            int oldHealth = player.getHealth();
            player.heal(healAmount);
            int actualHealed = player.getHealth() - oldHealth;
            player.usePotion();
            lastMessage = "You healed " + actualHealed + " HP!";
            // Transition to ENEMY_TURN after using potion
            state = State.ENEMY_TURN;
        } else {
            lastMessage = "No potions left!";
            // Stay in PLAYER_TURN; allow player to retry with ATTACK, DEFEND, or RUN
        }
    }

    public State getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public String getLastMessage() {
        synchronized (stateLock) {
            return lastMessage;
        }
    }

    public boolean isDefending() {
        synchronized (stateLock) {
            return defending;
        }
    }
}
