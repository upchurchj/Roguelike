package com.knightlight.game;

import com.knightlight.game.Equipment.Rarity;

/**
 * Represents a single dungeon floor/level.
 * Encapsulates floor difficulty, enemy spawning parameters, and loot mechanics.
 * Thread-safe immutable design.
 */
public class DungeonLevel {
    private final int floorNumber;
    private final int baseEnemyCount;
    private final float enemyDifficultyScaling;  // Multiplier for enemy stats
    private final float lootRarityThreshold;     // Min roll for rare+ loot
    private final String floorTheme;             // "Cavern", "Tomb", "Laboratory", etc.
    private final int minimumLevel;              // Recommended player level
    private final int baseLootXP;                // Base XP from defeating floor boss

    private DungeonLevel(Builder builder) {
        this.floorNumber = builder.floorNumber;
        this.baseEnemyCount = builder.baseEnemyCount;
        this.enemyDifficultyScaling = builder.enemyDifficultyScaling;
        this.lootRarityThreshold = builder.lootRarityThreshold;
        this.floorTheme = builder.floorTheme;
        this.minimumLevel = builder.minimumLevel;
        this.baseLootXP = builder.baseLootXP;
    }

    // ===== Getters =====
    public int getFloorNumber() {
        return floorNumber;
    }

    public int getBaseEnemyCount() {
        return baseEnemyCount;
    }

    public float getEnemyDifficultyScaling() {
        return enemyDifficultyScaling;
    }

    public float getLootRarityThreshold() {
        return lootRarityThreshold;
    }

    public String getFloorTheme() {
        return floorTheme;
    }

    public int getMinimumLevel() {
        return minimumLevel;
    }

    public int getBaseLootXP() {
        return baseLootXP;
    }

    // ===== Difficulty Calculations =====

    /**
     * Returns the expected enemy count for this floor,
     * accounting for procedural scaling.
     */
    public int getScaledEnemyCount() {
        return Math.max(1, (int) (baseEnemyCount + (floorNumber * 0.5)));
    }

    /**
     * Returns scaled enemy HP based on floor depth.
     * Base calculation: 20 HP * (1.3 ^ (floor - 1))
     */
    public int getScaledEnemyHP() {
        return (int) (20 * Math.pow(1.3, floorNumber - 1));
    }

    /**
     * Returns scaled enemy attack based on floor depth.
     * Base calculation: 5 ATK * (1.2 ^ (floor - 1))
     */
    public int getScaledEnemyAttack() {
        return (int) (5 * Math.pow(1.2, floorNumber - 1));
    }

    /**
     * Returns scaled enemy defense based on floor depth.
     * Base calculation: 1 DEF * (1.15 ^ (floor - 1))
     */
    public int getScaledEnemyDefense() {
        return Math.max(0, (int) (1 * Math.pow(1.15, floorNumber - 1)));
    }

    /**
     * Determines if a player is under-leveled for this floor.
     * Recommendation: player level >= floor minimum level.
     */
    public boolean isPlayerUnderleveled(int playerLevel) {
        return playerLevel < minimumLevel;
    }

    // ===== Builder Pattern =====
    public static class Builder {
        private final int floorNumber;
        private int baseEnemyCount = 3;
        private float enemyDifficultyScaling = 1.0f;
        private float lootRarityThreshold = 0.6f;
        private String floorTheme = "Generic Dungeon";
        private int minimumLevel = 1;
        private int baseLootXP = 100;

        public Builder(int floorNumber) {
            if (floorNumber < 1 || floorNumber > 99) {
                throw new IllegalArgumentException("Floor number must be 1-99.");
            }
            this.floorNumber = floorNumber;
            // Auto-scale minimum level
            this.minimumLevel = Math.max(1, floorNumber - 1);
            // Auto-scale base XP
            this.baseLootXP = (int) (100 * Math.pow(1.2, floorNumber - 1));
        }

        public Builder baseEnemyCount(int count) {
            if (count < 1 || count > 20) {
                throw new IllegalArgumentException("Base enemy count must be 1-20.");
            }
            this.baseEnemyCount = count;
            return this;
        }

        public Builder enemyDifficultyScaling(float scaling) {
            if (scaling < 0.5f || scaling > 3.0f) {
                throw new IllegalArgumentException("Difficulty scaling must be 0.5-3.0.");
            }
            this.enemyDifficultyScaling = scaling;
            return this;
        }

        public Builder lootRarityThreshold(float threshold) {
            if (threshold < 0.0f || threshold > 1.0f) {
                throw new IllegalArgumentException("Rarity threshold must be 0.0-1.0.");
            }
            this.lootRarityThreshold = threshold;
            return this;
        }

        public Builder floorTheme(String theme) {
            if (theme == null || theme.trim().isEmpty()) {
                throw new IllegalArgumentException("Theme cannot be null or empty.");
            }
            this.floorTheme = theme;
            return this;
        }

        public Builder minimumLevel(int level) {
            if (level < 1 || level > 99) {
                throw new IllegalArgumentException("Minimum level must be 1-99.");
            }
            this.minimumLevel = level;
            return this;
        }

        public DungeonLevel build() {
            return new DungeonLevel(this);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "DungeonLevel{floor=%d, theme='%s', minLv=%d, enemies=%d, rarityThresh=%.2f}",
            floorNumber, floorTheme, minimumLevel, baseEnemyCount, lootRarityThreshold
        );
    }
}
