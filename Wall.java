package lazarus;

import java.awt.Point;

import wingman.game.BackgroundObject;

public class Wall extends BackgroundObject {

    public Wall(int x, int y) {
        super(new Point(x * 40, y * 40), new Point(0, 0), LazarusWorld.sprites.get("Wall"));
    }

    public void damage(int damage) {
        return;
    }
}
