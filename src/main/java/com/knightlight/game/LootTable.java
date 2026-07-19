package com.knightlight.game;

import com.knightlight.game.Equipment;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe, immutable loot table for generating equipment drops.
 * Defines rarity tiers, drop pools, and probabilistic loot generation
 * based on floor difficulty and rarity thresholds.
 */
public class LootTable {
    private final Map<Equipment.Rarity, List<Equipment>> poolsByRarity;
    private final Map<Equipment.Rarity, Double> rarityWeights;
    private final Random random;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Rarity tier enumeration (mirrors Equipment.Rarity).
     * Maps to drop probability weights.
     */
    public enum LootRarity {
        COMMON(1.0),
        UNCOMMON(0.4),
        RARE(0.15),
        EPIC(0.04),
        LEGENDARY(0.01);

        private final double weight;

        LootRarity(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }
    }

    /**
     * Private constructor enforces Builder pattern.
     */
    private LootTable(Builder builder) {
        this.poolsByRarity = new EnumMap<>(builder.poolsByRarity);
        this.rarityWeights = new EnumMap<>(builder.rarityWeights);
        this.random = new Random();
    }

    /**
     * Generate a single loot item based on floor and rarity threshold.
     * @param floorNumber the dungeon floor (1-99)
     * @param rarityThreshold the probability threshold (0.0-1.0) from DungeonLevel
     * @return a randomly generated Equipment item, or null if no loot drops
     */
    public Equipment generateLoot(int floorNumber, double rarityThreshold) {
        lock.readLock().lock();
        try {
            // Roll for loot drop
            if (random.nextDouble() > rarityThreshold) {
                return null; // No loot
            }

            // Select rarity tier based on weights
            Equipment.Rarity selectedRarity = selectRarityByWeight();
            if (selectedRarity == null) {
                return null;
            }

            List<Equipment> pool = poolsByRarity.get(selectedRarity);
            if (pool == null || pool.isEmpty()) {
                return null;
            }

            // Select random item from rarity pool
            Equipment baseItem = pool.get(random.nextInt(pool.size()));

            // Scale stats by floor multiplier: 1.0 + (floor - 1) * 0.05
            double floorScalar = 1.0 + (floorNumber - 1) * 0.05;
            int scaledATK = (int) (baseItem.getAttackBonus() * floorScalar);
            int scaledDEF = (int) (baseItem.getDefenseBonus() * floorScalar);
            int scaledHP = (int) (baseItem.getHealthBonus() * floorScalar);

            return Equipment.builder()
                    .name(baseItem.getName())
                    .rarity(baseItem.getRarity())
                    .attackBonus(scaledATK)
                    .defenseBonus(scaledDEF)
                    .healthBonus(scaledHP)
                    .description(baseItem.getDescription())
                    .build();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Generate multiple loot items in a batch.
     * @param floorNumber the dungeon floor
     * @param rarityThreshold the rarity threshold
     * @param count number of roll attempts
     * @return list of generated Equipment (may be empty)
     */
    public List<Equipment> generateLootBatch(int floorNumber, double rarityThreshold, int count) {
        List<Equipment> loot = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Equipment item = generateLoot(floorNumber, rarityThreshold);
            if (item != null) {
                loot.add(item);
            }
        }
        return loot;
    }

    /**
     * Select a rarity tier probabilistically based on configured weights.
     * @return the selected Rarity, or null if selection fails
     */
    private Equipment.Rarity selectRarityByWeight() {
        double totalWeight = rarityWeights.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (totalWeight <= 0) {
            return Equipment.Rarity.COMMON;
        }

        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (Map.Entry<Equipment.Rarity, Double> entry : rarityWeights.entrySet()) {
            cumulative += entry.getValue();
            if (roll <= cumulative) {
                return entry.getKey();
            }
        }

        return Equipment.Rarity.COMMON;
    }

    /**
     * Add equipment to a rarity pool (for setup).
     * Thread-safe but should only be called before active use.
     */
    public void addItemToPool(Equipment item) {
        lock.writeLock().lock();
        try {
            poolsByRarity.computeIfAbsent(item.getRarity(), k -> new ArrayList<>())
                    .add(item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get the size of a rarity pool.
     */
    public int getPoolSize(Equipment.Rarity rarity) {
        lock.readLock().lock();
        try {
            List<Equipment> pool = poolsByRarity.get(rarity);
            return pool != null ? pool.size() : 0;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get the weight for a rarity tier.
     */
    public double getRarityWeight(Equipment.Rarity rarity) {
        lock.readLock().lock();
        try {
            return rarityWeights.getOrDefault(rarity, 0.0);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Builder for LootTable.
     */
    public static class Builder {
        private final Map<Equipment.Rarity, List<Equipment>> poolsByRarity = new EnumMap<>(Equipment.Rarity.class);
        private final Map<Equipment.Rarity, Double> rarityWeights = new EnumMap<>(Equipment.Rarity.class);

        public Builder() {
            // Initialize default weights
            rarityWeights.put(Equipment.Rarity.COMMON, 1.0);
            rarityWeights.put(Equipment.Rarity.UNCOMMON, 0.4);
            rarityWeights.put(Equipment.Rarity.RARE, 0.15);
            rarityWeights.put(Equipment.Rarity.EPIC, 0.04);
            rarityWeights.put(Equipment.Rarity.LEGENDARY, 0.01);

            // Initialize empty pools
            for (Equipment.Rarity rarity : Equipment.Rarity.values()) {
                poolsByRarity.put(rarity, new ArrayList<>());
            }
        }

        public Builder addItem(Equipment item) {
            if (item != null) {
                poolsByRarity.computeIfAbsent(item.getRarity(), k -> new ArrayList<>())
                        .add(item);
            }
            return this;
        }

        public Builder addItems(List<Equipment> items) {
            if (items != null) {
                for (Equipment item : items) {
                    addItem(item);
                }
            }
            return this;
        }

        public Builder setRarityWeight(Equipment.Rarity rarity, double weight) {
            if (rarity != null) {
                rarityWeights.put(rarity, Math.max(0, weight));
            }
            return this;
        }

        public LootTable build() {
            return new LootTable(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            int totalItems = poolsByRarity.values().stream()
                    .mapToInt(List::size)
                    .sum();
            return String.format("LootTable{totalItems=%d, rarities=%d}", 
                    totalItems, poolsByRarity.size());
        } finally {
            lock.readLock().unlock();
        }
    }
}
