package com.knightlight.game;

import com.knightlight.game.Player;
import com.knightlight.game.Enemy;
import com.knightlight.game.Equipment;
import com.knightlight.game.ExperienceSystem;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe combat system for turn-based combat between Player and Enemy.
 * Handles attack resolution, damage calculation, hit chance, and combat flow.
 */
public class CombatSystem {
    private final Random random;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Player player;
    private Enemy enemy;
    private List<String> combatLog;
    private int round;
    private boolean isActive;
    private CombatResult result;

    /**
     * Private constructor enforces Builder pattern.
     */
    private CombatSystem(Builder builder) {
        this.player = builder.player;
        this.enemy = builder.enemy;
        this.random = new Random();
        this.combatLog = new ArrayList<>();
        this.round = 0;
        this.isActive = false;
        this.result = null;
    }

    /**
     * Start a new combat encounter.
     * @return true if combat successfully initialized
     */
    public boolean startCombat() {
        lock.writeLock().lock();
        try {
            if (player == null || enemy == null) {
                combatLog.add("ERROR: Missing player or enemy");
                return false;
            }
            if (player.getHealth() <= 0 || enemy.getHealth() <= 0) {
                combatLog.add("ERROR: Combat participants already dead");
                return false;
            }
            isActive = true;
            round = 0;
            combatLog.clear();
            combatLog.add("=== COMBAT START: " + player.getName() + " vs " + enemy.getName() + " ===");
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Execute one round of combat (player acts first, then enemy).
     * @return true if combat is still active, false if combat ended
     */
    public boolean executeRound() {
        lock.writeLock().lock();
        try {
            if (!isActive) {
                return false;
            }

            round++;
            combatLog.add("\n--- Round " + round + " ---");

            // Player attacks first
            playerAttack();

            if (!isActive) {
                return false; // Combat ended after player action
            }

            // Enemy attacks in response
            enemyAttack();

            // Check for combat end
            if (player.getHealth() <= 0) {
                endCombat(CombatResult.DEFEAT);
                return false;
            }
            if (enemy.getHealth() <= 0) {
                endCombat(CombatResult.VICTORY);
                return false;
            }

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Player attacks enemy.
     */
    private void playerAttack() {
        int playerATK = calculatePlayerAttack();
        int enemyDEF = enemy.getDefense();

        // Calculate hit chance: base 70%, modified by stats
        double hitChance = 0.70 + (playerATK - enemyDEF) * 0.02;
        hitChance = Math.max(0.2, Math.min(1.0, hitChance)); // Clamp 20-100%

        if (random.nextDouble() > hitChance) {
            combatLog.add(player.getName() + " misses!");
            return;
        }

        // Calculate damage: ATK - DEF/2 + variance
        int baseDamage = Math.max(1, playerATK - (enemyDEF / 2));
        int variance = random.nextInt(baseDamage / 2 + 1); // ±25% variance
        int damage = baseDamage + (random.nextBoolean() ? variance : -variance);
        damage = Math.max(1, damage);

        enemy.takeDamage(damage);
        combatLog.add(player.getName() + " attacks for " + damage + " damage! " +
                "(Enemy HP: " + Math.max(0, enemy.getHealth()) + ")");
    }

    /**
     * Enemy attacks player.
     */
    private void enemyAttack() {
        int enemyATK = enemy.getAttack();
        int playerDEF = calculatePlayerDefense();

        // Calculate hit chance: base 65%, modified by stats
        double hitChance = 0.65 + (enemyATK - playerDEF) * 0.01;
        hitChance = Math.max(0.15, Math.min(0.95, hitChance)); // Clamp 15-95%

        if (random.nextDouble() > hitChance) {
            combatLog.add(enemy.getName() + " misses!");
            return;
        }

        // Calculate damage: ATK - DEF/2 + variance
        int baseDamage = Math.max(1, enemyATK - (playerDEF / 2));
        int variance = random.nextInt(baseDamage / 2 + 1);
        int damage = baseDamage + (random.nextBoolean() ? variance : -variance);
        damage = Math.max(1, damage);

        player.takeDamage(damage);
        combatLog.add(enemy.getName() + " attacks for " + damage + " damage! " +
                "(Player HP: " + Math.max(0, player.getHealth()) + ")");
    }

    /**
     * Calculate player's effective attack including equipment bonuses.
     */
    private int calculatePlayerAttack() {
        int baseATK = player.getAttack();
        int equipmentBonus = player.getInventory().getTotalAttackBonus();
        return baseATK + equipmentBonus;
    }

    /**
     * Calculate player's effective defense including equipment bonuses.
     */
    private int calculatePlayerDefense() {
        int baseDEF = player.getDefense();
        int equipmentBonus = player.getInventory().getTotalDefenseBonus();
        return baseDEF + equipmentBonus;
    }

    /**
     * End combat and resolve rewards/penalties.
     */
    private void endCombat(CombatResult result) {
        this.result = result;
        isActive = false;

        if (result == CombatResult.VICTORY) {
            combatLog.add("\n=== VICTORY ===");
            combatLog.add(player.getName() + " defeated " + enemy.getName() + "!");

            // Award experience
            int xpReward = enemy.getExperienceReward();
            player.addExperience(xpReward);
            combatLog.add("Gained " + xpReward + " experience!");
        } else if (result == CombatResult.DEFEAT) {
            combatLog.add("\n=== DEFEAT ===");
            combatLog.add(player.getName() + " was defeated by " + enemy.getName() + "!");
        } else if (result == CombatResult.FLED) {
            combatLog.add("\n=== FLED ===");
            combatLog.add(player.getName() + " successfully fled!");
        }
    }

    /**
     * Attempt to flee from combat.
     * Success chance: 40% base, modified by player level vs enemy level.
     * @return true if flee successful
     */
    public boolean attemptFlee() {
        lock.writeLock().lock();
        try {
            if (!isActive) {
                return false;
            }

            double fleeChance = 0.40 + (player.getLevel() - enemy.getLevel()) * 0.05;
            fleeChance = Math.max(0.1, Math.min(0.9, fleeChance)); // Clamp 10-90%

            if (random.nextDouble() <= fleeChance) {
                endCombat(CombatResult.FLED);
                combatLog.add("Fled successfully!");
                return true;
            } else {
                combatLog.add("Failed to flee!");
                // Still execute enemy attack for failed flee attempt
                enemyAttack();
                if (player.getHealth() <= 0) {
                    endCombat(CombatResult.DEFEAT);
                }
                return false;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get combat result (VICTORY, DEFEAT, FLED, or null if ongoing).
     */
    public CombatResult getResult() {
        lock.readLock().lock();
        try {
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Check if combat is still active.
     */
    public boolean isActive() {
        lock.readLock().lock();
        try {
            return isActive;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get current combat round number.
     */
    public int getRound() {
        lock.readLock().lock();
        try {
            return round;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get combat log as immutable list.
     */
    public List<String> getCombatLog() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(combatLog);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get last combat log entry.
     */
    public String getLastLogEntry() {
        lock.readLock().lock();
        try {
            return combatLog.isEmpty() ? "" : combatLog.get(combatLog.size() - 1);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get full combat log as formatted string.
     */
    public String getFullLog() {
        lock.readLock().lock();
        try {
            return String.join("\n", combatLog);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Combat result enumeration.
     */
    public enum CombatResult {
        VICTORY,
        DEFEAT,
        FLED;

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    /**
     * Builder for CombatSystem.
     */
    public static class Builder {
        private Player player;
        private Enemy enemy;

        public Builder() {}

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder enemy(Enemy enemy) {
            this.enemy = enemy;
            return this;
        }

        public CombatSystem build() {
            return new CombatSystem(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return String.format("CombatSystem{round=%d, active=%s, result=%s}",
                    round, isActive, result);
        } finally {
            lock.readLock().unlock();
        }
    }
}
