package com.knightlight.game;

import android.util.Log;
import java.io.Serializable;

public class ExperienceSystem implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAG = "ExperienceSystem";
    
    private int currentLevel;
    private long currentExperience;
    private long experienceForNextLevel;
    private final Object levelLock = new Object();
    
    // Experience thresholds using exponential scaling: baseExp * (1.5 ^ (level - 1))
    private static final long BASE_EXPERIENCE = 100L;
    private static final double EXP_MULTIPLIER = 1.5;
    
    // Stat growth per level
    private static final int HEALTH_PER_LEVEL = 5;
    private static final int ATTACK_PER_LEVEL = 2;
    private static final int DEFENSE_PER_LEVEL = 1;
    
    public ExperienceSystem() {
        this.currentLevel = 1;
        this.currentExperience = 0;
        this.experienceForNextLevel = calculateExperienceThreshold(1);
        Log.d(TAG, "ExperienceSystem initialized");
    }
    
    /**
     * Calculates experience needed for a given level
     * @param level Target level (1-based)
     * @return Experience points required
     */
    private static long calculateExperienceThreshold(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Level must be >= 1");
        }
        // Exponential scaling: 100 * 1.5^(level-1)
        return Math.round(BASE_EXPERIENCE * Math.pow(EXP_MULTIPLIER, level - 1));
    }
    
    /**
     * Add experience and check for level-up
     * @param amount Experience to add (validated to be non-negative)
     * @return Number of levels gained (0 if no level-up)
     */
    public synchronized int addExperience(int amount) {
        if (amount < 0) {
            Log.w(TAG, "addExperience called with negative amount: " + amount);
            return 0;
        }
        if (amount == 0) {
            return 0;
        }
        
        synchronized (levelLock) {
            currentExperience += amount;
            int levelsGained = 0;
            
            // Check for level-ups
            while (currentExperience >= experienceForNextLevel && currentLevel < 99) {
                currentExperience -= experienceForNextLevel;
                currentLevel++;
                experienceForNextLevel = calculateExperienceThreshold(currentLevel);
                levelsGained++;
                Log.d(TAG, "Level up! New level: " + currentLevel);
            }
            
            return levelsGained;
        }
    }
    
    public synchronized int getLevel() {
        return currentLevel;
    }
    
    public synchronized long getExperience() {
        return currentExperience;
    }
    
    public synchronized long getExperienceForNextLevel() {
        return experienceForNextLevel;
    }
    
    /**
     * Get progress to next level (0.0 to 1.0)
     */
    public synchronized float getExperienceProgress() {
        long prevThreshold = calculateExperienceThreshold(currentLevel);
        long currentProgress = currentExperience;
        return (float) currentProgress / experienceForNextLevel;
    }
    
    /**
     * Calculate stat bonuses based on level
     */
    public int getHealthBonus() {
        return (currentLevel - 1) * HEALTH_PER_LEVEL;
    }
    
    public int getAttackBonus() {
        return (currentLevel - 1) * ATTACK_PER_LEVEL;
    }
    
    public int getDefenseBonus() {
        return (currentLevel - 1) * DEFENSE_PER_LEVEL;
    }
    
    public synchronized void reset() {
        currentLevel = 1;
        currentExperience = 0;
        experienceForNextLevel = calculateExperienceThreshold(1);
        Log.d(TAG, "ExperienceSystem reset");
    }
}
