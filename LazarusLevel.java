package lazarus;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import java.util.ListIterator;
import wingman.game.PlayerShip;
import wingman.modifiers.AbstractGameModifier;

public class LazarusLevel extends AbstractGameModifier implements Observer {


    int start;
    String filename;
    BufferedReader level;
    Box currentBox;
    Box nextBox;
    int w, h;
    int endgameDelay = 100;	
    Random rand = new Random();

    public LazarusLevel(String filename) {
        super();
        this.filename = filename;
        String line;
        try {
            level = new BufferedReader(new InputStreamReader(LazarusWorld.class.getResource(filename).openStream()));
            line = level.readLine();
            w = line.length();
            h = 0;
            while (line != null) {
                h++;
                line = level.readLine();
            }
            level.close();
        } catch (IOException e) {
            System.exit(1);
        }
    }

    @Override
    public void read(Object theObject) {
    }

    public void load() {
        LazarusWorld world = LazarusWorld.getInstance();

        try {
            level = new BufferedReader(new InputStreamReader(LazarusWorld.class.getResource(filename).openStream()));
        } catch (IOException e) {
            System.exit(1);
        }

        String line;
        try {
            line = level.readLine();
            w = line.length();
            h = 0;
            while (line != null) {
                for (int i = 0, j = line.length(); i < j; i++) {
                    char c = line.charAt(i);
                    
                    if (c == '1') {
                        // the wall boxes that set up each level
                        Wall wall = new Wall(i, h);
                        world.addBackground(wall);
                    }

                    if (c == '2') {
                        // stop buttons in each level
                        StopButton stopbutton = new StopButton(i, h);
                        world.addBackground(stopbutton);
                        LazarusWorld.stopButton.add(new StopButton(i, h));
                    }

                    if (c == '3') {
                        // players intial position in the level
                        int[] controls = new int[]{KeyEvent.VK_LEFT, KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_ENTER};
                        world.addPlayer(new Lazarus(new Point(i * 40, h * 40), world.sprites.get("player1"), controls, "2"));
                    }

                    if (c == '4') {
                        // next random box placement
                        CardBoardBox cardboardbox = new CardBoardBox(i * 40, h * 40);
                        world.addBackground(cardboardbox);
                        currentBox = cardboardbox;
                        nextBox = cardboardbox;
                    }
                }
                h++;
                line = level.readLine();
            }

            level.close();
        } catch (IOException e) {
        }
    }

    private Box getRandomBox(int x, int y) {
        // chooses a random box to fall in the level
        Box nextBox = null;
        int r = rand.nextInt(4) + 1;
        if (r == 1) {
            nextBox = new CardBoardBox(x, y);
        } else if (r == 2) {
            nextBox = new WoodBox(x, y);
        } else if (r == 3) {
            nextBox = new MetalBox(x, y);
        } else if (r == 4) {
            nextBox = new StoneBox(x, y);
        }
        
        return nextBox;
    }

@Override
        public void update(Observable o, Object arg) {
        LazarusWorld world = LazarusWorld.getInstance();
        if (world.countBoxInMotion() < 1) {
            Rectangle playerloc = new Rectangle(160, 0, 40, 40);
            Box box = getRandomBox(0, (11 * 40));
            currentBox = nextBox;
            ListIterator<PlayerShip> players = world.getPlayers();
            while (players.hasNext()) {
                Lazarus player = (Lazarus) players.next();
                playerloc = player.getLocation();
            }

            currentBox.setLocation(new Point(playerloc.x, 0));
            currentBox.setSpeed(new Point(0, 2));
            world.addBoxInMotion(currentBox);
            nextBox = box;
            world.addBackground(nextBox);
            setChanged();
            notifyObservers();
        }
    }
}
