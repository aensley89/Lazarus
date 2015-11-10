package lazarus;

import java.awt.Point;

import wingman.game.BackgroundObject;
import java.awt.Rectangle;

public class StopButton extends BackgroundObject {

    int x;
    int y;
    Rectangle box;
    
    public StopButton(int x, int y) {
        super(new Point(x * 40, y * 40), new Point(0, 0), LazarusWorld.sprites.get("StopButton"));
    
        this.x = x;
        this.y = y;
    }
    
    public boolean collision(int x, int y, int w, int h){
        box = new Rectangle(this.x, this.y, 40, 40);
        Rectangle otherBox = new Rectangle(x, y, w, h);
        if (box.intersects(otherBox)){
            System.out.println("true");
            return true;
        }
        return false;
    }
 
}
