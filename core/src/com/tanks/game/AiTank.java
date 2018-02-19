package com.tanks.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class AiTank extends Tank {
    //    private static final float TARGETING_POWER_ERROR = 150.0f;
//    private static final float TARGETING_POWER_ANGLE = 20.0f;
    private static final float TARGETING_POWER_ERROR = 10.0f;
    private static final float TARGETING_POWER_ANGLE = 2.0f;

    public AiTank(GameScreen game, Vector2 position) {
        super(game, position);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (game.isMyTurn(this) && !makeTurn) {
            float tmpAngle = 0.0f;
            float tmpPower = 0.0f;
            Tank aim = null;

            do {
                aim = game.getPlayers().get(MathUtils.random(0, game.getPlayers().size() - 1));
            } while (aim == this);

            boolean ready = false;
            BulletEmitter.BulletType currentBulletType = BulletEmitter.BulletType.LIGHT_AMMO;
            do {
                tmpPower = MathUtils.random(MINIMAL_POWER, maxPower);
                tmpAngle = MathUtils.random(0, 180.0f);

                float ammoPosX = weaponPosition.x + 12 + 28 * (float) Math.cos(Math.toRadians(tmpAngle));
                float ammoPosY = weaponPosition.y + 16 + 28 * (float) Math.sin(Math.toRadians(tmpAngle));

                float ammoVelX = tmpPower * (float) Math.cos(Math.toRadians(tmpAngle));
                float ammoVelY = tmpPower * (float) Math.sin(Math.toRadians(tmpAngle));

                Bullet tmpBullet = game.getBulletEmitter().setup(currentBulletType, ammoPosX, ammoPosY, ammoVelX, ammoVelY);

                do {
                    ready = game.traceCollision(aim, tmpBullet, dt);
                } while (!ready && tmpBullet.isActive());
            } while (!ready);

            game.getBulletEmitter().checkPool();

            turretAngle = tmpAngle + MathUtils.random(-TARGETING_POWER_ANGLE / 2, TARGETING_POWER_ANGLE / 2);
            tmpPower = tmpPower + MathUtils.random(-TARGETING_POWER_ERROR / 2, TARGETING_POWER_ERROR / 2);

            float ammoPosX = weaponPosition.x + 4 + 28 * (float) Math.cos(Math.toRadians(tmpAngle));
            float ammoPosY = weaponPosition.y + 6 + 28 * (float) Math.sin(Math.toRadians(tmpAngle));

            float ammoVelX = tmpPower * (float) Math.cos(Math.toRadians(turretAngle));
            float ammoVelY = tmpPower * (float) Math.sin(Math.toRadians(turretAngle));

            game.getBulletEmitter().setup(currentBulletType, ammoPosX, ammoPosY, ammoVelX, ammoVelY);
            makeTurn = true;
            power = 0.0f;
        }
    }
}
