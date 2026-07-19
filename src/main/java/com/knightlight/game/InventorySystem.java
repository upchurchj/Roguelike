package com.knightlight.game;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe inventory management system for weapons, armor, rings, and potions.
 * Enforces capacity limits and tracks equipped items.
 */
public class InventorySystem implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String TAG = "InventorySystem";

    private static final int MAX_WEAPONS = 10;
    private static final int MAX_ARMOR = 10;
    private static final int MAX_RINGS = 5;
    private static final int MAX_POTIONS = 99;

    private final List<Equipment> weapons = new ArrayList<>();
    private final List<Equipment> armor = new ArrayList<>();
    private final List<Equipment> rings = new ArrayList<>();

    private Equipment equippedWeapon = null;
    private Equipment equippedArmor = null;
    private Equipment equippedRing = null;

    private int healthPotions = 0;

    /**
     * Add equipment to inventory. Returns true if successful, false if full.
     */
    public synchronized boolean addEquipment(Equipment equipment) {
        if (equipment == null) {
            Log.w(TAG, "Attempted to add null equipment");
            return false;
        }

        List<Equipment> targetList = getListForType(equipment.getType());
        int maxCapacity = getMaxCapacity(equipment.getType());

        if (targetList.size() >= maxCapacity) {
            Log.w(TAG, "Inventory full for " + equipment.getType());
            return false;
        }

        targetList.add(equipment);
        Log.d(TAG, "Added: " + equipment.getName());
        return true;
    }

    /**
     * Remove equipment from inventory.
     */
    public synchronized boolean removeEquipment(Equipment equipment) {
        Equipment.Type type = equipment.getType();
        List<Equipment> targetList = getListForType(type);

        if (targetList.remove(equipment)) {
            // If equipped item was removed, unequip it
            if (equipment.equals(equippedWeapon)) {
                equippedWeapon = null;
            } else if (equipment.equals(equippedArmor)) {
                equippedArmor = null;
            } else if (equipment.equals(equippedRing)) {
                equippedRing = null;
            }
            Log.d(TAG, "Removed: " + equipment.getName());
            return true;
        }
        return false;
    }

    /**
     * Equip an item. Returns true if successful.
     */
    public synchronized boolean equip(Equipment equipment) {
        if (equipment == null) {
            Log.w(TAG, "Cannot equip null equipment");
            return false;
        }

        switch (equipment.getType()) {
            case WEAPON:
                if (!weapons.contains(equipment)) {
                    Log.w(TAG, "Weapon not in inventory");
                    return false;
                }
                equippedWeapon = equipment;
                Log.d(TAG, "Equipped weapon: " + equipment.getName());
                return true;

            case ARMOR:
                if (!armor.contains(equipment)) {
                    Log.w(TAG, "Armor not in inventory");
                    return false;
                }
                equippedArmor = equipment;
                Log.d(TAG, "Equipped armor: " + equipment.getName());
                return true;

            case RING:
                if (!rings.contains(equipment)) {
                    Log.w(TAG, "Ring not in inventory");
                    return false;
                }
                equippedRing = equipment;
                Log.d(TAG, "Equipped ring: " + equipment.getName());
                return true;

            default:
                Log.w(TAG, "Unknown equipment type");
                return false;
        }
    }

    /**
     * Unequip an item type.
     */
    public synchronized void unequip(Equipment.Type type) {
        switch (type) {
            case WEAPON:
                equippedWeapon = null;
                break;
            case ARMOR:
                equippedArmor = null;
                break;
            case RING:
                equippedRing = null;
                break;
        }
        Log.d(TAG, "Unequipped: " + type);
    }

    /**
     * Get current health potion count.
     */
    public synchronized int getHealthPotions() {
        return healthPotions;
    }

    /**
     * Use one health potion. Returns true if successful, false if none available.
     */
    public synchronized boolean usePotion() {
        if (healthPotions > 0) {
            healthPotions--;
            Log.d(TAG, "Used health potion. Remaining: " + healthPotions);
            return true;
        }
        Log.w(TAG, "No health potions available");
        return false;
    }

    /**
     * Add health potions to inventory. Returns true if successful, false if would exceed max.
     */
    public synchronized boolean addHealthPotions(int count) {
        if (count < 0) {
            Log.w(TAG, "Cannot add negative potions");
            return false;
        }
        if (healthPotions + count <= MAX_POTIONS) {
            healthPotions += count;
            Log.d(TAG, "Added " + count + " health potions. Total: " + healthPotions);
            return true;
        }
        Log.w(TAG, "Health potion capacity exceeded. Max: " + MAX_POTIONS);
        return false;
    }

    // Getters for equipped items
    public synchronized Equipment getEquippedWeapon() { return equippedWeapon; }
    public synchronized Equipment getEquippedArmor() { return equippedArmor; }
    public synchronized Equipment getEquippedRing() { return equippedRing; }

    // Getters for inventory lists (defensive copies)
    public synchronized List<Equipment> getWeapons() { return new ArrayList<>(weapons); }
    public synchronized List<Equipment> getArmor() { return new ArrayList<>(armor); }
    public synchronized List<Equipment> getRings() { return new ArrayList<>(rings); }

    /**
     * Calculate total stat bonuses from equipped items.
     */
    public synchronized int getTotalAttackBonus() {
        int total = 0;
        if (equippedWeapon != null) total += equippedWeapon.getAttackBonus();
        if (equippedArmor != null) total += equippedArmor.getAttackBonus();
        if (equippedRing != null) total += equippedRing.getAttackBonus();
        return total;
    }

    public synchronized int getTotalDefenseBonus() {
        int total = 0;
        if (equippedWeapon != null) total += equippedWeapon.getDefenseBonus();
        if (equippedArmor != null) total += equippedArmor.getDefenseBonus();
        if (equippedRing != null) total += equippedRing.getDefenseBonus();
        return total;
    }

    public synchronized int getTotalHpBonus() {
        int total = 0;
        if (equippedWeapon != null) total += equippedWeapon.getHpBonus();
        if (equippedArmor != null) total += equippedArmor.getHpBonus();
        if (equippedRing != null) total += equippedRing.getHpBonus();
        return total;
    }

    // Private helpers
    private List<Equipment> getListForType(Equipment.Type type) {
        switch (type) {
            case WEAPON: return weapons;
            case ARMOR: return armor;
            case RING: return rings;
            default: throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    private int getMaxCapacity(Equipment.Type type) {
        switch (type) {
            case WEAPON: return MAX_WEAPONS;
            case ARMOR: return MAX_ARMOR;
            case RING: return MAX_RINGS;
            default: return 0;
        }
    }

    @Override
    public String toString() {
        return String.format("Inventory [W:%d/%d | A:%d/%d | R:%d/%d | P:%d/%d]",
                weapons.size(), MAX_WEAPONS,
                armor.size(), MAX_ARMOR,
                rings.size(), MAX_RINGS,
                healthPotions, MAX_POTIONS);
    }
}
