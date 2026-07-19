package com.knightlight.game;

import java.io.Serializable;

/**
 * Immutable Equipment item with rarity, stats, and type classification.
 * Uses Builder pattern for safe, flexible construction.
 */
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        WEAPON, ARMOR, RING
    }

    public enum Rarity {
        COMMON("#CCCCCC"),           // Light gray
        UNCOMMON("#4CAF50"),         // Green
        RARE("#2196F3"),             // Blue
        LEGENDARY("#FFD700");        // Gold

        private final String hexColor;

        Rarity(String hexColor) {
            this.hexColor = hexColor;
        }

        public String getHexColor() {
            return hexColor;
        }
    }

    private final String name;
    private final Type type;
    private final Rarity rarity;
    private final int attackBonus;
    private final int defenseBonus;
    private final int hpBonus;
    private final String description;

    /**
     * Private constructor. Use Builder to instantiate.
     */
    private Equipment(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.rarity = builder.rarity;
        this.attackBonus = builder.attackBonus;
        this.defenseBonus = builder.defenseBonus;
        this.hpBonus = builder.hpBonus;
        this.description = builder.description;
    }

    // Getters
    public String getName() { return name; }
    public Type getType() { return type; }
    public Rarity getRarity() { return rarity; }
    public int getAttackBonus() { return attackBonus; }
    public int getDefenseBonus() { return defenseBonus; }
    public int getHpBonus() { return hpBonus; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) | ATK+%d DEF+%d HP+%d",
                rarity, name, type, attackBonus, defenseBonus, hpBonus);
    }

    /**
     * Builder class for Equipment construction.
     */
    public static class Builder {
        private final String name;
        private final Type type;
        private Rarity rarity = Rarity.COMMON;
        private int attackBonus = 0;
        private int defenseBonus = 0;
        private int hpBonus = 0;
        private String description = "";

        public Builder(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder attackBonus(int bonus) {
            this.attackBonus = bonus;
            return this;
        }

        public Builder defenseBonus(int bonus) {
            this.defenseBonus = bonus;
            return this;
        }

        public Builder hpBonus(int bonus) {
            this.hpBonus = bonus;
            return this;
        }

        public Builder description(String desc) {
            this.description = desc;
            return this;
        }

        public Equipment build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Equipment name cannot be null or empty");
            }
            if (type == null) {
                throw new IllegalArgumentException("Equipment type cannot be null");
            }
            return new Equipment(this);
        }
    }
}
