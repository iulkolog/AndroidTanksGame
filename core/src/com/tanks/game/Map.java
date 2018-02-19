package com.tanks.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Map {
    private TextureRegion textureGround;
    private TextureRegion[] textureClouds;
    private byte[][] data;
    private float[][] color;
    private float time;
    private Vector3[] clouds;
    private float windPower;

    private static final int CELL_SIZE = 2;
    private static final int WIDTH = 1280 / CELL_SIZE;
    private static final int HEIGHT = 720 / CELL_SIZE;


    public Map() {
        this.textureGround = new TextureRegion(Assets.getInstance().getAtlas().findRegion("grass"));
        this.textureClouds = new TextureRegion[3];
        TextureRegion[][] tmp = new TextureRegion(Assets.getInstance().getAtlas().findRegion("clouds")).split(256, 128);
        for (int i = 0; i < 3; i++) {
            textureClouds[i] = tmp[i][0];
        }
        this.data = new byte[WIDTH][HEIGHT];
        this.color = new float[WIDTH][HEIGHT];
        this.generate();
        this.clouds = new Vector3[14];
        for (int i = 0; i < clouds.length; i++) {
            clouds[i] = new Vector3(MathUtils.random(-640, 1280 + 640), MathUtils.random(560, 700), MathUtils.random(0, 2));
        }
        this.windPower = 20.0f;
    }

    public void generate() {
        int[] heightMap = new int[WIDTH];
        heightMap[0] = MathUtils.random(100, HEIGHT / 5 * 3);
        heightMap[WIDTH - 1] = MathUtils.random(100, HEIGHT / 5 * 3);
        split(heightMap, 0, WIDTH - 1, 80, 20);
        for (int i = 0; i < 2; i++) {
            slideWindow(heightMap, 7);
        }

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < heightMap[i]; j++) {
                data[i][j] = 1;
                color[i][j] = ((float) j / HEIGHT);
            }
        }
    }

    public void split(int[] arr, int x1, int x2, int iter, int iterDecrease) {
        int center = (x1 + x2) / 2;
        if (iter < 0) {
            iter = 0;
        }
        arr[center] = (arr[x1] + arr[x2]) / 2 + MathUtils.random(-iter, iter);
        if (x2 - x1 > 2) {
            split(arr, x1, center, iter - iterDecrease, iterDecrease);
            split(arr, center, x2, iter - iterDecrease, iterDecrease);
        }
    }

    public void slideWindow(int[] arr, int halfWin) {
        for (int i = 0; i < arr.length; i++) {
            int x1 = i - halfWin;
            int x2 = i + halfWin;
            if (x1 < 0) {
                x1 = 0;
            }
            if (x2 > WIDTH - 1) {
                x2 = WIDTH - 1;
            }
            int avg = 0;
            for (int j = x1; j <= x2; j++) {
                avg += arr[j];
            }
            avg /= (x2 - x1 + 1);
            arr[i] = avg;
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (data[i][j] == 1) {
                    batch.setColor(0, color[i][j], 0, 1);
                    batch.draw(textureGround, i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        batch.setColor(1, 1, 1, 1);
        for (int i = 0; i < clouds.length; i++) {
            batch.draw(textureClouds[(int) clouds[i].z], clouds[i].x, clouds[i].y);
        }
    }

    public void drop() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT - 1; j++) {
                if (data[i][j] == 0 && data[i][j + 1] == 1) {
                    data[i][j] = 1;
                    data[i][j + 1] = 0;
                    color[i][j] = color[i][j + 1];
                    color[i][j + 1] = 0;
                }
            }
        }
    }

    public boolean isGround(float x, float y) {
        int cellX = (int) (x / CELL_SIZE);
        int cellY = (int) (y / CELL_SIZE);
        if (cellX < 0 || cellX > WIDTH - 1 || cellY < 0 || cellY > HEIGHT - 1) {
            return false;
        }
        return data[cellX][cellY] == 1;
    }

    public void clearGround(float x, float y, int r) {
        int cellX = (int) (x / CELL_SIZE);
        int cellY = (int) (y / CELL_SIZE);

        int left = cellX - r;
        if (left < 0) {
            left = 0;
        }
        int right = cellX + r;
        if (right > WIDTH - 1) {
            right = WIDTH - 1;
        }
        int down = cellY - r;
        if (down < 0) {
            down = 0;
        }
        int up = cellY + r;
        if (up > HEIGHT - 1) {
            up = HEIGHT - 1;
        }

        for (int i = left; i <= right; i++) {
            for (int j = down; j <= up; j++) {
                if (Math.sqrt((i - cellX) * (i - cellX) + (j - cellY) * (j - cellY)) < r) {
                    data[i][j] = 0;
                }
            }
        }
    }

    public int getHeightInX(int x) {
        for (int i = 0; i < HEIGHT; i++) {
            if (data[x / CELL_SIZE][i] == 0) {
                return i * CELL_SIZE;
            }
        }
        return -1;
    }

    public void update(float dt) {
        time += dt;
        for (int i = 0; i < clouds.length; i++) {
            clouds[i].x += windPower * dt;
            if (clouds[i].x < -640) {
                clouds[i].set(MathUtils.random(1280, 1280 + 640), MathUtils.random(560, 700), MathUtils.random(0, 2));
            }
            if (clouds[i].x > 1280 + 640) {
                clouds[i].set(MathUtils.random(-640, -256), MathUtils.random(560, 700), MathUtils.random(0, 2));
            }
        }
        if (time > 0.5f) {
            drop();
        }
    }
}
