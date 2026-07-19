package com.knightlight.game;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe manager for dungeon progression and floor state.
 * Maintains the collection of dungeon levels, current floor position,
 * and coordinates floor transitions with scaling logic.
 */
public class DungeonManager {
    private final Map<Integer, DungeonLevel> levels;
    private final int maxFloor;
    private int currentFloor;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Private constructor enforces Builder pattern.
     */
    private DungeonManager(Builder builder) {
        this.levels = new TreeMap<>(builder.levels);
        this.maxFloor = builder.maxFloor;
        this.currentFloor = builder.startFloor;
    }

    /**
     * Get the DungeonLevel for a specific floor.
     * @param floorNumber the floor number (1-99)
     * @return the DungeonLevel, or null if not generated
     */
    public DungeonLevel getLevel(int floorNumber) {
        lock.readLock().lock();
        try {
            return levels.get(floorNumber);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get or generate a DungeonLevel for a specific floor.
     * @param floorNumber the floor number (1-99)
     * @return the DungeonLevel
     */
    public DungeonLevel getOrCreateLevel(int floorNumber) {
        lock.readLock().lock();
        try {
            if (levels.containsKey(floorNumber)) {
                return levels.get(floorNumber);
            }
        } finally {
            lock.readLock().unlock();
        }

        // Generate new level outside read lock
        DungeonLevel newLevel = DungeonLevel.builder()
                .floorNumber(floorNumber)
                .baseEnemyCount(5)
                .difficultyScaling(1.0)
                .rarityThreshold(0.2)
                .build();

        lock.writeLock().lock();
        try {
            // Double-check pattern
            if (!levels.containsKey(floorNumber)) {
                levels.put(floorNumber, newLevel);
            }
            return levels.get(floorNumber);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Attempt to progress to the next floor.
     * @return true if progression successful, false if at max floor
     */
    public boolean progressFloor() {
        lock.writeLock().lock();
        try {
            if (currentFloor < maxFloor) {
                currentFloor++;
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retreat to the previous floor.
     * @return true if retreat successful, false if already on floor 1
     */
    public boolean retreatFloor() {
        lock.writeLock().lock();
        try {
            if (currentFloor > 1) {
                currentFloor--;
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Get the current floor number.
     */
    public int getCurrentFloor() {
        lock.readLock().lock();
        try {
            return currentFloor;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get the current DungeonLevel.
     */
    public DungeonLevel getCurrentLevel() {
        return getOrCreateLevel(getCurrentFloor());
    }

    /**
     * Set the current floor (for dungeon entry or save state restore).
     * @param floorNumber the target floor (1-99)
     * @return true if valid, false otherwise
     */
    public boolean setCurrentFloor(int floorNumber) {
        lock.writeLock().lock();
        try {
            if (floorNumber >= 1 && floorNumber <= maxFloor) {
                currentFloor = floorNumber;
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Check if player can progress further.
     */
    public boolean canProgress() {
        lock.readLock().lock();
        try {
            return currentFloor < maxFloor;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get the maximum floor depth.
     */
    public int getMaxFloor() {
        return maxFloor;
    }

    /**
     * Get all generated levels (snapshot for iteration).
     */
    public Map<Integer, DungeonLevel> getAllLevels() {
        lock.readLock().lock();
        try {
            return new TreeMap<>(levels);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clear all cached levels (for memory management).
     */
    public void clearLevels() {
        lock.writeLock().lock();
        try {
            levels.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Builder for DungeonManager.
     */
    public static class Builder {
        private final Map<Integer, DungeonLevel> levels = new TreeMap<>();
        private int maxFloor = 99;
        private int startFloor = 1;

        public Builder() {}

        public Builder maxFloor(int maxFloor) {
            this.maxFloor = Math.max(1, Math.min(maxFloor, 99));
            return this;
        }

        public Builder startFloor(int startFloor) {
            this.startFloor = Math.max(1, Math.min(startFloor, this.maxFloor));
            return this;
        }

        public Builder addLevel(DungeonLevel level) {
            if (level != null) {
                levels.put(level.getFloorNumber(), level);
            }
            return this;
        }

        public Builder addLevels(Map<Integer, DungeonLevel> levelsMap) {
            if (levelsMap != null) {
                levels.putAll(levelsMap);
            }
            return this;
        }

        public DungeonManager build() {
            return new DungeonManager(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return String.format("DungeonManager{currentFloor=%d, maxFloor=%d, levelsLoaded=%d}",
                    currentFloor, maxFloor, levels.size());
        } finally {
            lock.readLock().unlock();
        }
    }
}
