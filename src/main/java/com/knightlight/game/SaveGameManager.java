package com.knightlight.game;

import com.knightlight.game.Player;
import com.knightlight.game.DungeonManager;
import com.knightlight.game.InventorySystem;
import com.knightlight.game.Equipment;
import com.knightlight.game.InputValidator;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe manager for saving and loading game state.
 * Persists player, inventory, dungeon progress, and metadata to disk.
 * Uses JSON-like serialization (mimicked via maps/lists for Android compatibility).
 */
public class SaveGameManager {
    private final String savePath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Create a new SaveGameManager with a given save file directory.
     * @param savePath path to save directory (e.g., app cache directory)
     */
    public SaveGameManager(String savePath) {
        if (!InputValidator.isNotEmpty(savePath)) {
            throw new IllegalArgumentException("Save path cannot be null or empty");
        }
        this.savePath = savePath;
    }

    /**
     * Save the current game state (player, inventory, dungeon progress, metadata).
     * @param player the player to save
     * @param dungeonManager the dungeon manager holding current floor state
     * @param timestamp save timestamp (milliseconds since epoch)
     * @return true if save succeeded, false otherwise
     */
    public boolean saveGame(Player player, DungeonManager dungeonManager, long timestamp) {
        if (!InputValidator.isNotNull(player) || !InputValidator.isNotNull(dungeonManager)) {
            return false;
        }

        lock.writeLock().lock();
        try {
            Map<String, Object> gameState = new HashMap<>();

            // Player data
            gameState.put("playerName", player.getName());
            gameState.put("playerLevel", player.getLevel());
            gameState.put("playerExperience", player.getExperience());
            gameState.put("playerHealth", player.getHealth());
            gameState.put("playerMaxHealth", player.getMaxHealth());
            gameState.put("playerAttack", player.getAttack());
            gameState.put("playerDefense", player.getDefense());

            // Dungeon progress
            gameState.put("currentFloor", dungeonManager.getCurrentFloor());
            gameState.put("floorsVisited", dungeonManager.getFloorsVisited());

            // Inventory (serialize as list of equipment data)
            InventorySystem inventory = player.getInventory();
            List<Map<String, Object>> equipmentList = new ArrayList<>();
            for (Equipment eq : inventory.getAllEquipment()) {
                Map<String, Object> eqData = new HashMap<>();
                eqData.put("name", eq.getName());
                eqData.put("rarity", eq.getRarity().name());
                eqData.put("attackBonus", eq.getAttackBonus());
                eqData.put("defenseBonus", eq.getDefenseBonus());
                eqData.put("healthBonus", eq.getHealthBonus());
                eqData.put("floor", eq.getFloorFound());
                equipmentList.add(eqData);
            }
            gameState.put("inventory", equipmentList);

            // Equipped items (current equipment)
            Equipment mainHand = player.getMainHandEquipment();
            Equipment offHand = player.getOffHandEquipment();
            Equipment armor = player.getArmorEquipment();

            if (mainHand != null) {
                gameState.put("equippedMainHand", serializeEquipment(mainHand));
            }
            if (offHand != null) {
                gameState.put("equippedOffHand", serializeEquipment(offHand));
            }
            if (armor != null) {
                gameState.put("equippedArmor", serializeEquipment(armor));
            }

            // Metadata
            gameState.put("timestamp", timestamp);
            gameState.put("version", 1);

            // Write to file
            String filename = "savegame_" + System.currentTimeMillis() + ".dat";
            File saveFile = new File(savePath, filename);
            return writeGameState(saveFile, gameState);

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Load a game from the most recent save file.
     * @return SaveGameData containing player state and metadata, or null if no save found
     */
    public SaveGameData loadLatestGame() {
        lock.readLock().lock();
        try {
            File dir = new File(savePath);
            if (!dir.exists() || !dir.isDirectory()) {
                return null;
            }

            File[] files = dir.listFiles((d, name) -> name.startsWith("savegame_") && name.endsWith(".dat"));
            if (files == null || files.length == 0) {
                return null;
            }

            // Sort by modification time, get latest
            Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            return loadGameFromFile(files[0]);

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Load a specific save game by filename.
     * @param filename the save file name
     * @return SaveGameData or null if load fails
     */
    public SaveGameData loadGameByFilename(String filename) {
        if (!InputValidator.isNotEmpty(filename)) {
            return null;
        }

        lock.readLock().lock();
        try {
            File saveFile = new File(savePath, filename);
            if (!saveFile.exists()) {
                return null;
            }
            return loadGameFromFile(saveFile);

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * List all available save games with their metadata.
     * @return list of save file info (filename, timestamp)
     */
    public List<SaveFileInfo> listSaveGames() {
        List<SaveFileInfo> saves = new ArrayList<>();

        lock.readLock().lock();
        try {
            File dir = new File(savePath);
            if (!dir.exists() || !dir.isDirectory()) {
                return saves;
            }

            File[] files = dir.listFiles((d, name) -> name.startsWith("savegame_") && name.endsWith(".dat"));
            if (files == null) {
                return saves;
            }

            for (File f : files) {
                saves.add(new SaveFileInfo(f.getName(), f.lastModified()));
            }

            // Sort by modification time (newest first)
            saves.sort((a, b) -> Long.compare(b.modifiedTime, a.modifiedTime));
            return saves;

        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Delete a save game file.
     * @param filename the save file name
     * @return true if deleted, false otherwise
     */
    public boolean deleteSaveGame(String filename) {
        if (!InputValidator.isNotEmpty(filename)) {
            return false;
        }

        lock.writeLock().lock();
        try {
            File saveFile = new File(savePath, filename);
            return saveFile.exists() && saveFile.delete();

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Serialize an Equipment object to a map.
     */
    private Map<String, Object> serializeEquipment(Equipment eq) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", eq.getName());
        data.put("rarity", eq.getRarity().name());
        data.put("attackBonus", eq.getAttackBonus());
        data.put("defenseBonus", eq.getDefenseBonus());
        data.put("healthBonus", eq.getHealthBonus());
        data.put("floor", eq.getFloorFound());
        return data;
    }

    /**
     * Write game state map to file (serialized as ObjectOutputStream for simplicity).
     */
    private boolean writeGameState(File file, Map<String, Object> gameState) {
        try {
            file.getParentFile().mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(gameState);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load game state map from file.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> readGameState(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<String, Object>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load a game from a specific file and return SaveGameData.
     */
    private SaveGameData loadGameFromFile(File file) {
        Map<String, Object> gameState = readGameState(file);
        if (gameState == null) {
            return null;
        }

        // Validate version
        int version = ((Number) gameState.getOrDefault("version", 1)).intValue();
        if (version != 1) {
            return null;
        }

        // Build SaveGameData
        SaveGameData data = new SaveGameData();
        data.playerName = (String) gameState.get("playerName");
        data.playerLevel = ((Number) gameState.getOrDefault("playerLevel", 1)).intValue();
        data.playerExperience = ((Number) gameState.getOrDefault("playerExperience", 0)).intValue();
        data.playerHealth = ((Number) gameState.getOrDefault("playerHealth", 100)).intValue();
        data.playerMaxHealth = ((Number) gameState.getOrDefault("playerMaxHealth", 100)).intValue();
        data.playerAttack = ((Number) gameState.getOrDefault("playerAttack", 5)).intValue();
        data.playerDefense = ((Number) gameState.getOrDefault("playerDefense", 1)).intValue();
        data.currentFloor = ((Number) gameState.getOrDefault("currentFloor", 1)).intValue();
        data.floorsVisited = ((Number) gameState.getOrDefault("floorsVisited", 0)).intValue();
        data.timestamp = ((Number) gameState.getOrDefault("timestamp", 0)).longValue();
        data.filename = file.getName();

        // Load inventory
        data.inventory = (List<Map<String, Object>>) gameState.getOrDefault("inventory", new ArrayList<>());
        data.equippedMainHand = (Map<String, Object>) gameState.get("equippedMainHand");
        data.equippedOffHand = (Map<String, Object>) gameState.get("equippedOffHand");
        data.equippedArmor = (Map<String, Object>) gameState.get("equippedArmor");

        return data;
    }

    /**
     * Data class for save file information.
     */
    public static class SaveFileInfo {
        public final String filename;
        public final long modifiedTime;

        public SaveFileInfo(String filename, long modifiedTime) {
            this.filename = filename;
            this.modifiedTime = modifiedTime;
        }

        @Override
        public String toString() {
            return "SaveFileInfo{" +
                    "filename='" + filename + '\'' +
                    ", modifiedTime=" + modifiedTime +
                    '}';
        }
    }

    /**
     * Data class for loaded game state.
     * Contains all player, inventory, and dungeon progress data.
     */
    public static class SaveGameData {
        public String filename;
        public String playerName;
        public int playerLevel;
        public int playerExperience;
        public int playerHealth;
        public int playerMaxHealth;
        public int playerAttack;
        public int playerDefense;
        public int currentFloor;
        public int floorsVisited;
        public long timestamp;
        public List<Map<String, Object>> inventory;
        public Map<String, Object> equippedMainHand;
        public Map<String, Object> equippedOffHand;
        public Map<String, Object> equippedArmor;

        @Override
        public String toString() {
            return "SaveGameData{" +
                    "filename='" + filename + '\'' +
                    ", playerName='" + playerName + '\'' +
                    ", playerLevel=" + playerLevel +
                    ", currentFloor=" + currentFloor +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SaveGameManager{" +
                "savePath='" + savePath + '\'' +
                '}';
    }
}
