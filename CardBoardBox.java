package lazarus;

import java.awt.Point;

public class CardBoardBox extends Box {

    public CardBoardBox(int x, int y) {
        super(new Point(x, y), new Point(0, 0), 1, LazarusWorld.sprites.get("CardBox"));
    }
}
