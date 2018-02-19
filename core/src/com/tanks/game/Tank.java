package com.tanks.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public abstract class Tank {
    protected TextureRegion textureBase;
    protected TextureRegion textureTurret;
    protected TextureRegion textureTrack;
    protected TextureRegion hudBarBack;
    protected TextureRegion hudBarPower;
    protected TextureRegion hudBarHp;

    protected Vector2 position;
    protected Vector2 weaponPosition;
    protected GameScreen game;
    protected float turretAngle;
    protected int hp;
    protected int maxHp;
    protected Circle hitArea;
    protected float power;
    protected float maxPower;
    protected float speed;
    protected boolean makeTurn;
    protected float fuel;
    protected float time;
    protected float reddish;

    protected StringBuilder tmpStrBuilder = new StringBuilder();

    public boolean isMakeTurn() {
        return makeTurn;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setMakeTurn(boolean makeTurn) {
        this.makeTurn = makeTurn;
    }

    public final static float TURRET_ROTATION_ANGULAR_SPEED = 90.0f; // скорость поворота турели
    public final static float MINIMAL_POWER = 100.0f; // минимальная сила выстрела
    public final static int MAX_MOVEMENT_DY = 10; // максимальная разница в высоте земли при движении

    public Circle getHitArea() {
        return hitArea;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeTurn() {
        makeTurn = false;
        fuel = 0.8f;
    }

    public Tank(GameScreen game, Vector2 position) {
        this.game = game;
        this.position = position;
        this.weaponPosition = new Vector2(position).add(0, 0);

        this.textureBase = Assets.getInstance().getAtlas().findRegion("tankBody");
        this.textureTurret = Assets.getInstance().getAtlas().findRegion("tankTurret");
        this.textureTrack = Assets.getInstance().getAtlas().findRegion("tankTrack");
        this.hudBarBack = new TextureRegion(Assets.getInstance().getAtlas().findRegion("bars"), 0, 0, 80, 24);
        this.hudBarHp = new TextureRegion(Assets.getInstance().getAtlas().findRegion("bars"), 0, 24, 80, 24);
        this.hudBarPower = new TextureRegion(Assets.getInstance().getAtlas().findRegion("bars"), 0, 48, 80, 24);
        this.turretAngle = 0.0f;
        this.maxHp = 100;
        this.hp = this.maxHp;
        this.hitArea = new Circle(position, textureBase.getRegionWidth() * 0.4f);
        this.power = 0.0f;
        this.maxPower = 1200.0f;
        this.speed = 100.0f;
        this.makeTurn = true;
        this.reddish = 0.0f;
    }

    public void render(SpriteBatch batch) {
        float tmp = 1.0f;
        float rtmp = 1.0f;
        if (game.isMyTurn(this)) {
            tmp = 0.9f + 0.1f * (float) Math.sin(time * 4);
            rtmp = 0.9f + 0.1f * (float) Math.sin(time * 4);
        }
        tmp *= (1.0f - reddish);
        batch.setColor(rtmp, tmp, tmp, 1);
        float t = MathUtils.random(-0.5f, 0.5f);
        if (game.isMyTurn(this)) {
            t = MathUtils.random(-2f, 2f);
        }

        batch.draw(textureTurret, weaponPosition.x, weaponPosition.y + t, textureTurret.getRegionWidth() / 10, textureTurret.getRegionHeight() / 2, textureTurret.getRegionWidth(), textureTurret.getRegionHeight(), 1, 1, turretAngle);
        batch.draw(textureTrack, position.x + 4, position.y);
        batch.draw(textureBase, position.x, position.y + textureTrack.getRegionHeight() / 3 + t);


        batch.setColor(1, 1, 1, 1);
    }

    public void renderHUD(SpriteBatch batch, BitmapFont font) {
        //batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.draw(hudBarBack, position.x, position.y + 80, 80, 24);
        batch.draw(hudBarHp, position.x + 2, position.y + 80, (int) (76 * (float) hp / maxHp), 24);

        //batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        //batch.draw(hudBarHp, position.x + 2 + MathUtils.random(-reddish * 3, reddish * 3), position.y + 70 + MathUtils.random(-reddish * 3, reddish * 3), (int) (80 * (float) hp / maxHp), 24);
        //batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        tmpStrBuilder.setLength(0);
        tmpStrBuilder.append(hp);
        font.draw(batch, tmpStrBuilder, position.x, position.y + 100, 85, 1, false);
        if (power > 100.0f) {
            batch.draw(hudBarBack, position.x, position.y + 104, 80, 24);
            batch.draw(hudBarPower, position.x + 2, position.y + 104, (int) (76 * power / maxPower), 24);
        }
    }

    public void rotateTurret(int n, float dt) {
        turretAngle += n * TURRET_ROTATION_ANGULAR_SPEED * dt;
    }

    public void move(int n, float dt) {
        if (fuel > 0.0f) {
            float dstX = position.x + speed * dt * n;
            for (int i = 1; i < MAX_MOVEMENT_DY; i++) {
                if (!checkOnGround(dstX, position.y + i)) {
                    position.x = dstX;
                    position.y += i - 1;
                    break;
                }
            }
            fuel -= dt;
            for (int i = 0; i < textureBase.getRegionWidth(); i += 8) {
                game.getParticleEmitter().setup(position.x + i, position.y, MathUtils.random(60, 120) * -n, MathUtils.random(0, 40), 0.25f, 2, 2, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.1f);
            }
        }
    }

    public void update(float dt) {
        if (!checkOnGround()) {
            position.y -= 100.0f * dt;
        }
        if (reddish > 0.0f) {
            reddish -= dt;
        } else {
            reddish = 0.0f;
        }
        this.weaponPosition.set(position).add(34, 45);
        this.hitArea.x = position.x + textureBase.getRegionWidth() / 2;
        this.hitArea.y = position.y + textureBase.getRegionHeight() / 2;
        this.time += dt;
    }

    public boolean takeDamage(int dmg) {
        hp -= dmg;
        reddish += 1.0f;
        game.getInfoSystem().addMessage("-" + dmg, position.x + 20, position.y + 100, FlyingText.Colors.RED);
        if (hp <= 0) {
            return true;
        }
        return false;
    }

    public boolean checkOnGround(float x, float y) {
        for (int i = 10; i < textureBase.getRegionWidth() - 10; i += 2) {
            if (game.getMap().isGround(x + i, y + 10)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkOnGround() {
        return checkOnGround(position.x, position.y);
    }
}
