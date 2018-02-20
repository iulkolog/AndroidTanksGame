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
    private static final float MAX_WIND_POWER = 50f;
    private static final int MAX_CLOUDS_COUNT = 50;

    private float cloud_color_r;
    private float cloud_color_g;
    private float cloud_color_b;
    private float cloud_color_a;

    private float ground_color_a;
    private float ground_color_b;
    private float ground_color_r;

    private boolean night_light;

    public boolean isNight_light() {
        return night_light;
    }


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
        int cloudCount = MathUtils.random(1, MAX_CLOUDS_COUNT);
        this.clouds = new Vector3[cloudCount];
        for (int i = 0; i < clouds.length; i++) {
            clouds[i] = new Vector3(MathUtils.random(-640, 1280 + 640), MathUtils.random(560, 700), MathUtils.random(0, 2));
        }
        this.windPower = MathUtils.random(-MAX_WIND_POWER, MAX_WIND_POWER);
        this.cloud_color_a = MathUtils.random(0f, 1f);
        if (MathUtils.random(0,1) == 1) {
            this.cloud_color_r = MathUtils.random(0f, 1f);
            this.cloud_color_g = MathUtils.random(0f, 1f);
            this.cloud_color_b = MathUtils.random(0f, 1f);
        }
        else {
            this.cloud_color_r = 1;
            this.cloud_color_g = 1;
            this.cloud_color_b = 1;

        }

        this.ground_color_a = MathUtils.random(0f, 1f);
        if (MathUtils.random(0,1) == 1) {
            this.ground_color_r = MathUtils.random(0f, 1f);
            this.ground_color_b = MathUtils.random(0f, 1f);
        }
        else {
            this.ground_color_r = 0;
            this.ground_color_b = 0;

        }
        ;
        if (MathUtils.random(0,1) == 1){
            night_light = true;
            cloud_color_r *= 0.5;
            cloud_color_g *= 0.5;
            cloud_color_b *= 0.5;
            ground_color_r *= 0.3;
            ground_color_b *= 0.3;
        }
        else{
            cloud_color_r /= 0.5;
            cloud_color_g /= 0.5;
            cloud_color_b /= 0.5;
            if (cloud_color_r >1)
                cloud_color_r = 1;
            if (cloud_color_g >1)
                cloud_color_g = 1;
            if (cloud_color_b >1)
                cloud_color_b = 1;
        }

    }

    public void generate() {
        int[] heightMap = new int[WIDTH];
        heightMap[0] = MathUtils.random(100, HEIGHT / 5 * 3);
        heightMap[WIDTH - 1] = MathUtils.random(100, HEIGHT / 5 * 3);
        int iterDecrease = MathUtils.random(10,20);
        split(heightMap, 0, WIDTH - 1, 80, iterDecrease);
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

        //рендер земли
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (data[i][j] == 1) {
                    batch.setColor(ground_color_r, color[i][j], ground_color_b, ground_color_a);
                    batch.draw(textureGround, i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        //облака
        batch.setColor(cloud_color_r, cloud_color_g, cloud_color_b, cloud_color_a);
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
