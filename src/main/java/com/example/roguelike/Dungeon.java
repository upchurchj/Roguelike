package com.example.roguelike;

import java.util.Random;

public class Dungeon {
    public static final char TILE_WALL = '#';
    public static final char TILE_FLOOR = '.';
    public static final char TILE_GOLD = '*';

    private int width;
    private int height;
    private char[][] grid;
    private Random random = new Random(System.nanoTime());

    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][width];
        generateMap();
    }

    private void generateMap() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isBorderTile(x, y)) {
                    grid[y][x] = TILE_WALL;
                } else {
                    double rand = random.nextDouble();
                    if (rand < 0.15) {
                        grid[y][x] = TILE_WALL;
                    } else if (rand < 0.20) {
                        grid[y][x] = TILE_GOLD;
                    } else {
                        grid[y][x] = TILE_FLOOR;
                    }
                }
            }
        }
        grid[1][1] = TILE_FLOOR;
    }

    private boolean isBorderTile(int x, int y) {
        return x == 0 || x == width - 1 || y == 0 || y == height - 1;
    }

    public char getTile(int x, int y) {
        if (!isInBounds(x, y)) return TILE_WALL;
        return grid[y][x];
    }

    public void setTile(int x, int y, char tile) {
        if (!isInBounds(x, y)) {
            throw new IllegalArgumentException("Tile out of bounds: (" + x + ", " + y + ")");
        }
        grid[y][x] = tile;
    }

    public boolean isWalkable(int x, int y) {
        if (!isInBounds(x, y)) return false;
        return grid[y][x] != TILE_WALL;
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
