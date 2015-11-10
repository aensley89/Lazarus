package lazarus;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ListIterator;
import wingman.GameSounds;
import wingman.GameWorld;
import wingman.game.PlayerShip;
import wingman.modifiers.motions.InputController;

public class Lazarus extends PlayerShip {

    int prevRight = 0;
    int prevLeft = 0;
    int prevUp = 0;
    int prevDown = 0;

    public Lazarus(Point location, Image img, int[] controls,
            String name) {
        super(location, new Point(0, 0), img, controls, name);
        resetPoint = new Point(location);

        this.name = name;
        motion = new InputController(this, controls, LazarusWorld.getInstance());
        lives = 1;
        health = 100;
        strength = 100;
        score = 0;
        respawnCounter = 0;
        height = 40;
        width = 40;
        this.location = new Rectangle(location.x, location.y, width, height);
    }

    private boolean isPlayerWallCollision() {
        ListIterator<Wall> wallList = LazarusWorld.getInstance().getWalls();
        while (wallList.hasNext()) {
            Wall wall = wallList.next();
            if (wall.collision(this)) {
                if (right == 1) {
                    if ((location.x + 40) >= wall.getX()) {
                        return true;
                    }
                } else if ((location.x - 40) <= wall.getX()) {
                    return true;
                } else if ((location.y + 40) >= wall.getY()) {
                    return true;
                } else if ((location.y - 40) <= wall.getY()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPlayerBoxCollision() {
        int Col = location.x / 40;
        ListIterator<Box> boxlist = LazarusWorld.getInstance().getBoxInRest(Col);
        while (boxlist.hasNext()) {
            Box boxCollision = boxlist.next();
            if (boxCollision.collision(this)) {
                if (LazarusWorld.getInstance().getBoxInRestAbove(Col, boxCollision.getY()) > 0) {
                    return true;
                }
                location.y -= 40;
                return false;
            }
        }
        if (location.y < 360 && (location.x > 120 && location.x < 520)) {
            location.y += 40;
            return isPlayerBoxCollision();
        }
        return false;
    }

    boolean isPlayerStopCollision() {
        ListIterator<StopButton> stopButton = LazarusWorld.getInstance().getStopButton();
        while (stopButton.hasNext()) {
            StopButton stop = stopButton.next();
            if (stop.collision(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(int w, int h) {
        if (prevRight != right || prevLeft != left) {
            prevRight = right;
            prevLeft = left;
            if (right == 1 || left == 1) {
                location.x += (right - left) * 40;
                // checks for a collision between the player and the wall
                // allows player to jump on box at rest
                if (isPlayerWallCollision() == true || isPlayerBoxCollision() == true) {
                    GameSounds.play("Resources/Wall.wav");
                    location.x -= (right - left) * 40;
                } else {
                    GameSounds.play("Resources/Move.wav");
                }
            }
        }
        if (isPlayerStopCollision()) {
            this.win();
        }
    }

    @Override
    // if player is crushed by a box the game is over
    public void die() {
        this.show = false;
        GameSounds.play("Resources/Squished.wav");
        GameWorld.setSpeed(new Point(0, 0));
        LazarusWorld.getInstance().removeClockObserver(this.motion);
        reset();
        this.motion.delete(this);
        LazarusWorld.gameFinished = true;
    }

    // if player reaches stop button the game is won
    public void win() {
        LazarusWorld.gameWon = true;
        LazarusWorld.gameFinished = true;
    }

    @Override
    public void reset() {
        this.setLocation(resetPoint);
        health = strength;
        respawnCounter = 160;
    }

}
