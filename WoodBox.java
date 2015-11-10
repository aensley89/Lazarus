package lazarus;

import java.awt.Point;

public class WoodBox extends Box {

    public WoodBox(int x, int y) {
        super(new Point(x, y), new Point(0, 0), 2, LazarusWorld.sprites.get("WoodBox"));
    }

}
