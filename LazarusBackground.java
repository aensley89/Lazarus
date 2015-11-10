package lazarus;

import java.awt.Image;
import java.awt.Point;

import wingman.game.BackgroundObject;

public class LazarusBackground extends BackgroundObject {

    int w, h;

    public LazarusBackground(int w, int h, Point speed, Image img) {
        super(new Point(0, 0), speed, img);
        this.setImage(img);
        this.img = img;
        this.w = w;
        this.h = h;
    }
}
