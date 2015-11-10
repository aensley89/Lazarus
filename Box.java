package lazarus;

import java.awt.Image;
import java.awt.Point;
import wingman.game.Ship;

public class Box extends Ship {

    public Box(Point location, Point speed, int strength, Image img) {
        super(location, speed, strength, img);
    }

    public void setSpeed(Point point) {
        speed = point;
    }

    @Override
    public int getStrength() {
        return strength;
    }

    @Override
    public void fire() {
    }

}
