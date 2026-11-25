package com.codingame.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class Grid {
    public static final int PILLAR = 9;
    public static final int VOID = 0;

    private static final int[][] WOOD3_GRIDS = {{9, 9}, {11, 9}, {11, 11}};
    private static final int[][] WOOD2_GRIDS = {{9, 9}, {11, 9}, {11, 11}, {13, 9}, {13, 11}};
    private static final int[][] WOOD1_GRIDS = {{9, 9}, {11, 9}, {11, 11}, {13, 9}, {13, 11}, {13, 13}};

    private int width = 11;
    private int height = 11;
    private int[][] tiles = new int[height][width];
    private final List<Beacon> beacons = new ArrayList<>();

    public static final int[] DX = {0, 1, 0, -1};
    public static final int[] DY = {-1, 0, 1, 0};

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void generate(Random rng, int leagueLevel, List<int[]> p0Spawns, List<int[]> p1Spawns) {
        int[][] gridChoices = leagueLevel == 1 ? WOOD3_GRIDS : (leagueLevel == 2 ? WOOD2_GRIDS : WOOD1_GRIDS);
        int[] dims = gridChoices[rng.nextInt(gridChoices.length)];
        width = dims[0];
        height = dims[1];
        tiles = new int[height][width];

        int midX = width / 2;
        int midY = height / 2;
        int area = width * height;

        // Initialize with integrity 2
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = 2;
            }
        }

        // Rotationally symmetric random generation across center (width-1-x, height-1-y)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x <= midX; x++) {
                int sx = width - 1 - x;
                int sy = height - 1 - y;
                if (x == midX && y > midY) {
                    continue;
                }
                double roll = rng.nextDouble();
                int val;
                if (roll < 0.12) {
                    val = VOID;
                } else if (roll < 0.22) {
                    val = PILLAR;
                } else if (roll < 0.50) {
                    val = 1;
                } else if (roll < 0.80) {
                    val = 2;
                } else {
                    val = 3;
                }
                tiles[y][x] = val;
                tiles[sy][sx] = val;
            }
        }

        // Center tile is always walkable high-integrity
        tiles[midY][midX] = 3;

        // Adaptive unit count per player:
        // Wood 3: 1, Wood 2: 2, Wood 1: 2 (9x9), 3 (11x9 / 11x11 / 13x9), 4 (13x11 / 13x13)
        int unitsPerPlayer;
        if (leagueLevel == 1) {
            unitsPerPlayer = 1;
        } else if (leagueLevel == 2) {
            unitsPerPlayer = 2;
        } else {
            unitsPerPlayer = area <= 81 ? 2 : (area <= 130 ? 3 : 4);
        }

        int[][] baseSpawns0 = {
            {1, midY},
            {2, 2},
            {2, height - 3},
            {3, midY}
        };
        for (int i = 0; i < unitsPerPlayer; i++) {
            int ux = baseSpawns0[i][0];
            int uy = baseSpawns0[i][1];
            int ex = width - 1 - ux;
            int ey = height - 1 - uy;
            p0Spawns.add(new int[]{ux, uy});
            p1Spawns.add(new int[]{ex, ey});

            // Ensure spawn neighbourhood is walkable
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int nx0 = ux + dx, ny0 = uy + dy;
                    int nx1 = ex - dx, ny1 = ey - dy;
                    if (inBounds(nx0, ny0) && (tiles[ny0][nx0] == VOID || tiles[ny0][nx0] == PILLAR)) {
                        tiles[ny0][nx0] = 2;
                        tiles[ny1][nx1] = 2;
                    }
                }
            }
            tiles[uy][ux] = 3;
            tiles[ey][ex] = 3;
        }

        // Carve guaranteed horizontal and vertical lanes (Top, Mid, Bot / Left, Mid, Right)
        for (int x = 1; x < width - 1; x++) {
            if (!isWalkable(x, midY)) tiles[midY][x] = 2;
            if (!isWalkable(x, 2)) tiles[2][x] = 2;
            if (!isWalkable(x, height - 3)) tiles[height - 3][x] = 2;
        }
        for (int y = 1; y < height - 1; y++) {
            if (!isWalkable(2, y)) tiles[y][2] = 2;
            if (!isWalkable(width - 3, y)) tiles[y][width - 3] = 2;
            if (!isWalkable(midX, y)) tiles[y][midX] = 2;
        }

        // Place symmetric beacons (5 on 9x9, 7 on 11x9 / 11x11 / 13x9, 9 on 13x11 / 13x13)
        beacons.clear();
        beacons.add(new Beacon(midX, midY)); // Center beacon

        int[][] candidateBeacons = {
            {2, midY - 1},
            {midX - 1, 2},
            {2, midY + 1},
            {midX - 1, height - 3},
            {3, 1},
            {1, 3},
            {midX, 2},
            {1, height - 4}
        };

        // Shuffle candidate beacons deterministically using rng
        for (int i = candidateBeacons.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int[] tmp = candidateBeacons[i];
            candidateBeacons[i] = candidateBeacons[j];
            candidateBeacons[j] = tmp;
        }

        int pairsNeeded = area <= 81 ? 2 : (area <= 130 ? 3 : 4);
        int pairsAdded = 0;
        for (int[] cand : candidateBeacons) {
            if (pairsAdded >= pairsNeeded) break;
            int bx = cand[0];
            int by = cand[1];
            int sbx = width - 1 - bx;
            int sby = height - 1 - by;
            tiles[by][bx] = 3;
            tiles[sby][sbx] = 3;
            beacons.add(new Beacon(bx, by));
            beacons.add(new Beacon(sbx, sby));
            pairsAdded++;
        }
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public boolean isWalkable(int x, int y) {
        if (!inBounds(x, y)) return false;
        int t = tiles[y][x];
        return t >= 1 && t <= 3;
    }

    public int getTile(int x, int y) {
        if (!inBounds(x, y)) return VOID;
        return tiles[y][x];
    }

    public void setTile(int x, int y, int value) {
        if (inBounds(x, y)) {
            tiles[y][x] = value;
        }
    }

    public List<Beacon> getBeacons() {
        return beacons;
    }

    public String getRowString(int y) {
        StringBuilder sb = new StringBuilder(width);
        for (int x = 0; x < width; x++) {
            int t = tiles[y][x];
            if (t == PILLAR) {
                sb.append('#');
            } else {
                sb.append((char) ('0' + t));
            }
        }
        return sb.toString();
    }

    /**
     * Finds the first adjacent step from (sx, sy) towards (tx, ty) using BFS on walkable tiles.
     */
    public int[] nextStepTowards(int sx, int sy, int tx, int ty) {
        if (sx == tx && sy == ty) {
            return new int[]{sx, sy};
        }
        // If directly adjacent and walkable, step directly
        if (Math.abs(sx - tx) + Math.abs(sy - ty) == 1 && isWalkable(tx, ty)) {
            return new int[]{tx, ty};
        }

        int[][] dist = new int[height][width];
        int[][][] firstStep = new int[height][width][2];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dist[y][x] = -1;
            }
        }

        Queue<int[]> q = new ArrayDeque<>();
        dist[sy][sx] = 0;
        q.add(new int[]{sx, sy});

        int[] bestCell = {sx, sy};
        int bestManhattan = Math.abs(sx - tx) + Math.abs(sy - ty);
        int bestDist = 0;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int cx = cur[0], cy = cur[1];
            if (cx == tx && cy == ty) {
                return firstStep[cy][cx];
            }
            for (int d = 0; d < 4; d++) {
                int nx = cx + DX[d];
                int ny = cy + DY[d];
                if (isWalkable(nx, ny) && dist[ny][nx] == -1) {
                    dist[ny][nx] = dist[cy][cx] + 1;
                    if (cx == sx && cy == sy) {
                        firstStep[ny][nx][0] = nx;
                        firstStep[ny][nx][1] = ny;
                    } else {
                        firstStep[ny][nx][0] = firstStep[cy][cx][0];
                        firstStep[ny][nx][1] = firstStep[cy][cx][1];
                    }
                    q.add(new int[]{nx, ny});

                    int manhattan = Math.abs(nx - tx) + Math.abs(ny - ty);
                    if (manhattan < bestManhattan || (manhattan == bestManhattan && dist[ny][nx] < bestDist)) {
                        bestManhattan = manhattan;
                        bestDist = dist[ny][nx];
                        bestCell = firstStep[ny][nx];
                    }
                }
            }
        }
        return bestCell;
    }
}
