package lazarus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import javax.swing.JFrame;

import wingman.GameClock;
import wingman.GameSounds;
import wingman.GameWorld;
import wingman.game.BackgroundObject;
import wingman.game.Bullet;
import wingman.game.PlayerShip;
import wingman.game.Ship;
import wingman.modifiers.AbstractGameModifier;
import wingman.modifiers.motions.MotionController;
import wingman.ui.GameMenu;
import wingman.ui.InfoBar;
import wingman.ui.InterfaceObject;

public class LazarusWorld extends GameWorld {

    private Thread thread;

    // GameWorld is a singleton class!
    private static final LazarusWorld game = new LazarusWorld();
    public static final GameSounds sound = new GameSounds();
    public static final GameClock clock = new GameClock();

    GameMenu menu;
    public LazarusLevel level;
    public static HashMap<String, Image> sprites = GameWorld.sprites;
    private BufferedImage bimg;
    Random generator = new Random();
    int sizeX, sizeY;
    Point mapSize;
    SoundPlayer musicPlayer;
    /*Some ArrayLists to keep track of game things*/
    private ArrayList<Bullet> bullets;
    private ArrayList<PlayerShip> players;
    private ArrayList<InterfaceObject> ui;
    private ArrayList<Ship> powerups;
    private ArrayList<Box> box;
    private ArrayList<Wall> wall;
    static ArrayList<StopButton> stopButton;
    private ArrayList<Box> boxInMotion;
    private ArrayList<ArrayList<Box>> boxInRest;

    public static HashMap<String, MotionController> motions = new HashMap<String, MotionController>();
    // is player still playing, did they win, and should we exit
    static boolean gameOver, gameWon, gameFinished;
    ImageObserver observer;

    // constructors makes sure the game is focusable, then
    // initializes a bunch of ArrayLists
    private LazarusWorld() {
        this.setFocusable(true);
        background = new ArrayList<BackgroundObject>();
        bullets = new ArrayList<Bullet>();
        players = new ArrayList<PlayerShip>();
        ui = new ArrayList<InterfaceObject>();
        powerups = new ArrayList<Ship>();
        stopButton = new ArrayList<StopButton>();
        box = new ArrayList<Box>();
        wall = new ArrayList<Wall>();
        boxInMotion = new ArrayList<Box>();
        boxInRest = new ArrayList<ArrayList<Box>>();
        for (int i = 0; i < 16; i++) {
            boxInRest.add(new ArrayList<Box>());
        }
    }
    /* This returns a reference to the currently running game*/
    public static LazarusWorld getInstance() {
        return game;
    }

    /*Game Initialization*/
    public void init() {
        setBackground(Color.white);
        loadSprites();

        gameOver = false;
        observer = this;

        level = new LazarusLevel("Resources/level.txt");
        level.addObserver(this);
        clock.addObserver(level);

        mapSize = new Point(level.w * 40, level.h * 40);
        GameWorld.setSpeed(new Point(0, 0));
        musicPlayer = new SoundPlayer(1, "Resources/Music.wav");
        addBackground(new LazarusBackground(mapSize.x, mapSize.y, GameWorld.getSpeed(), sprites.get("background")));
        level.load();
    }

    /*Functions for loading image resources*/
    @Override
    protected void loadSprites() {
        sprites.put("background", getSprite("Resources/Background.bmp"));
        sprites.put("Wall", getSprite("Resources/Wall.gif"));
        sprites.put("Mesh", getSprite("Resources/Mesh.gif"));
        sprites.put("CardBox", getSprite("Resources/CardBox.gif"));
        sprites.put("WoodBox", getSprite("Resources/WoodBox.gif"));
        sprites.put("MetalBox", getSprite("Resources/MetalBox.gif"));
        sprites.put("StoneBox", getSprite("Resources/StoneBox.gif"));
        sprites.put("StopButton", getSprite("Resources/Button.gif"));
        sprites.put("Lazarus_afraid", getSprite("Resources/Lazarus_afraid.gif"));
        sprites.put("Lazarus_jump_left", getSprite("Resources/Lazarus_jump_left.gif"));
        sprites.put("Lazarus_jump_right", getSprite("Resources/Lazarus_jump_right.gif"));
        sprites.put("Lazarus_left", getSprite("Resources/Lazarus_left.gif"));
        sprites.put("Lazarus_right", getSprite("Resources/Lazarus_right.gif"));
        sprites.put("Lazarus_squished", getSprite("Resources/Lazarus_squished.gif"));
        sprites.put("Lazarus_stand", getSprite("Resources/Lazarus_stand.gif"));
        sprites.put("Title", getSprite("Resources/Title.gif"));
        sprites.put("LazarusIcon", getSprite("Resources/lazarus.ico"));
        sprites.put("player1", getSprite("Resources/Lazarus_stand.gif"));
    }

    @Override
    public Image getSprite(String name) {
        Image img = null;
        try {
            img = ImageIO.read(LazarusWorld.class.getResource(name));
        } catch (IOException ex) {
            Logger.getLogger(LazarusWorld.class.getName()).log(Level.SEVERE, null, ex);
        }
        return img;
    }

    /**
     * ******************************
     * These functions GET things 
     * ****************************** 
     * @return
     */
    public int getFrameNumber() {
        return clock.getFrame();
    }

    public int getTime() {
        return clock.getTime();
    }

    public void removeClockObserver(Observer theObject) {
        clock.deleteObserver(theObject);
    }

    public ListIterator<BackgroundObject> getBackgroundObjects() {
        return background.listIterator();
    }

    public ListIterator<PlayerShip> getPlayers() {
        return players.listIterator();
    }

    public ListIterator<Bullet> getBullets() {
        return bullets.listIterator();
    }

    public ListIterator<Box> getBoxInMotion() {
        return boxInMotion.listIterator();
    }

    public ListIterator<StopButton> getStopButton() {
        return stopButton.listIterator();
    }

    public ListIterator<Box> getBoxInRest(int Column) {
        return boxInRest.get(Column).listIterator();
    }

    public int getBoxInRestColumn(int Column) {
        return boxInRest.get(Column).size();
    }

    public int getBoxInRestAbove(int Column, int LocationY) {
        int count = 0;
        ListIterator<Box> boxlist = getBoxInRest(Column);
        while (boxlist.hasNext()) {
            Box nextBox = boxlist.next();
            if (nextBox.getY() < LocationY) {
                count++;
            }
        }
        return count;
    }

    public ListIterator<Wall> getWalls() {
        return wall.listIterator();
    }

    public int countBoxInMotion() {
        return boxInMotion.size();
    }

    public int countPlayers() {
        return players.size();
    }

    public void setDimensions(int w, int h) {
        this.sizeX = w;
        this.sizeY = h;
    }

    /**
     * ***********************************
     * These functions ADD & REMOVE things
     *
     ***********************************
     * @param newObjects
     */
    public void addBoxInMotion(Box... newObjects) {
        for (Box box : newObjects) {
            boxInMotion.add(box);
        }
    }

    public void removeBoxInMotion(Box... newObjects) {
        for (Box box : newObjects) {
            boxInMotion.remove(box);
        }
    }

    @Override
    public void addBullet(Bullet... newObjects) {
        for (Bullet bullet : newObjects) {
            bullets.add(bullet);
        }
    }

    // add background items
    public void addBackground(BackgroundObject... newObjects) {
        for (BackgroundObject object : newObjects) {
            background.add(object);
        }
    }

    public void addPlayer(PlayerShip... newObjects) {
        for (PlayerShip player : newObjects) {
            players.add(player);
            ui.add(new InfoBar(player, Integer.toString(players.size())));
        }
    }

    public void addBackground(Box... newObjects) {
        for (Box object : newObjects) {
            box.add(object);
        }
    }

    public void addBackground(Wall... newObjects) {
        for (Wall object : newObjects) {
            wall.add(object);
        }
    }

    public void addBoxInRest(Box... newObjects) {
        for (Box object : newObjects) {
            Rectangle boxLocation = object.getLocation();
            int Columns = boxLocation.x / 40;
            boxInRest.get(Columns).add(object);
        }
    }

    public void addBoxInRest(int Column, Box... newObjects) {
        for (Box object : newObjects) {
            boxInRest.get(Column).add(object);
        }
    }

    public void removeBoxInRest(Box... newObjects) {
        for (Box object : newObjects) {
            Rectangle boxLocation = object.getLocation();
            int Columns = boxLocation.x / 40;
            boxInRest.get(Columns).remove(object);
        }
    }

    public void removeBoxInRest(int Column, int index) {
        boxInRest.get(Column).remove(index);
    }

    @Override
    public void addClockObserver(Observer object) {
        clock.addObserver(object);
    }

    // this is the main function where game stuff happens!
    // each frame is also drawn here
    public void drawFrame(int w, int h, Graphics2D g2) {
        ListIterator<?> iterator = getBackgroundObjects();
        while (iterator.hasNext()) {
            BackgroundObject obj = (BackgroundObject) iterator.next();
            obj.update(w, h);
            obj.draw(g2, this);
            // checks if the box coming down collides with the 
            // player. If there's a collision game is over
            ListIterator<Box> boxInMotion = getBoxInMotion();
            while (boxInMotion.hasNext()) {
                Box fallingBox = boxInMotion.next();
                Rectangle fallingLoc = fallingBox.getLocation();
                int Falling = fallingLoc.x / 40;
                ListIterator<PlayerShip> players = getPlayers();
                while (players.hasNext()) {
                    Lazarus player = (Lazarus) players.next();
                    if (fallingBox.collision(player)) {
                        player.die();
                    }
                }
                // checks the collision between the box coming down
                // and the box at rest. Depending on the strength of 
                // each box, the falling box will either crush the rested
                // box or stack on top of it
                ListIterator<Box> boxInRest = getBoxInRest(Falling);
                boolean fallingStopped = false;
                while (boxInRest.hasNext()) {
                    Box restedBox = boxInRest.next();
                    if (fallingBox.collision(restedBox)) {
                        if (restedBox.getStrength() < fallingBox.getStrength()) {
                            restedBox.hide();
                            boxInRest.remove();
                            GameSounds.play("Resources/Crush.wav");
                        } else {
                            boxInRest.add(fallingBox);
                            boxInMotion.remove();
                            fallingStopped = true;
                        }
                    }
                    restedBox.draw(g2, this);
                }
                // loop doesn't allow falling box to go through the bottom
                if (fallingStopped == false && fallingBox.getY() > 360) {
                    addBoxInRest(Falling, fallingBox);
                    boxInMotion.remove();
                }

                ListIterator<StopButton> stopButtons = getStopButton();
                while (stopButtons.hasNext()) {
                    StopButton stopBox = stopButtons.next();
                    while (players.hasNext()) {
                        Lazarus player = (Lazarus) players.next();
                        if (stopBox.collision(player)) {
                            player.win();
                        }
                    }
                }
            }
        }

        if (!gameFinished) {
            iterator = getPlayers();
            while (iterator.hasNext()) {
                PlayerShip player = (PlayerShip) iterator.next();

                if (player.isDead()) {
                    gameOver = true;
                    continue;
                }
                // get stop buttons
                ListIterator<StopButton> stopButtons = this.stopButton.listIterator();
                while (stopButtons.hasNext()) {
                    StopButton stopBox = stopButtons.next();
                    stopBox.draw(g2, this);
                }
                // gets next random box 
                ListIterator<Box> boxList = this.box.listIterator();
                while (boxList.hasNext()) {
                    Box box = boxList.next();
                    box.draw(g2, this);
                }
                // gets the boxes that come down
                ListIterator<Box> boxInMotionList = this.boxInMotion.listIterator();
                while (boxInMotionList.hasNext()) {
                    Box motion = boxInMotionList.next();
                    motion.update(w, h);
                    motion.draw(g2, this);
                }
                // gets the boxes that make the wall 
                ListIterator<Wall> wallList = this.wall.listIterator();
                while (wallList.hasNext()) {
                    Wall wall = wallList.next();
                    wall.draw(g2, this);
                }
            }

            PlayerShip p1 = players.get(0);
            p1.update(w, h);
            p1.draw(g2, this);

        } // end game stuff
        else {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Helvetica", Font.PLAIN, 24));
            if (!gameWon) {
                g2.drawString("GAME OVER!!!", (sizeX / 3) + 30, sizeY / 2);
            } else {
                g2.drawString("You Win!", (sizeX / 3) + 30, sizeY / 2);
            }
        }
    }

    @Override
    public Graphics2D createGraphics2D(int w, int h) {
        Graphics2D g2 = null;
        if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
            bimg = (BufferedImage) createImage(w, h);
        }
        g2 = bimg.createGraphics();
        g2.setBackground(getBackground());
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.clearRect(0, 0, w, h);
        return g2;
    }

    @Override
    public void paint(Graphics g) {
        if (players.size() != 0) {
            clock.tick();
        }
        Dimension windowSize = getSize();
        Graphics2D g2 = createGraphics2D(windowSize.width, windowSize.height);
        drawFrame(windowSize.width, windowSize.height, g2);
        g2.dispose();
        g.drawImage(bimg, 0, 0, this);
    }

    public void start() {
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    @Override
    public void run() {

        Thread me = Thread.currentThread();
        while (thread == me) {
            this.requestFocusInWindow();
            repaint();

            try {
                thread.sleep(23);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        AbstractGameModifier modifier = (AbstractGameModifier) o;
        modifier.read(this);
    }

    public static void main(String argv[]) {
        final LazarusWorld game = LazarusWorld.getInstance();
        JFrame f = new JFrame("Lazarus");
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                game.requestFocusInWindow();
            }
        });
        f.getContentPane().add("Center", game);
        f.pack();
        f.setSize(new Dimension(640, 505));
        game.setDimensions(640, 480);
        game.init();
        f.setVisible(true);
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.start();
    }
}
