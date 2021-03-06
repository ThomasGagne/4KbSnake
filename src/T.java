import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
// For playing audio with SourceDataLine
import javax.sound.sampled.*;
import java.util.LinkedList;
import java.util.Random;

// Only implement runnable if you want audio!
public class T extends JFrame implements ActionListener, Runnable {

    // Final static constants don't impact overall size
    private static final int WINDOW_SIZE = 600;
    private static final int PIXEL_SIZE = 20;
    private static final int BOARD_DIMENSIONS = WINDOW_SIZE / PIXEL_SIZE;
    private static final int TIMER_TICK_RATE = 100;
    private static final int MENU_HEIGHT = 20;
    private static final Color[] C = {new Color(137, 204, 195), new Color(0, 0, 0), new Color(204, 10, 10), new Color(195, 195, 195)};

    public void restart() {
        rand = new Random();

        snakeX = new LinkedList<Integer>();
        snakeY = new LinkedList<Integer>();
        snakeX.add(15);
        snakeY.add(15);

        appleX = rand.nextInt(BOARD_DIMENSIONS);
        // Because java's frames count what's under the header to be part of the frame
        appleY = rand.nextInt(BOARD_DIMENSIONS - 1) + 1;

        // Prevent the apple from spawning on top of the snake
        if(appleX == 15) {
            appleX = 10;
        }

        playSuccess = false;
        playTenSuccess = false;
        playLose = false;
        lost = false;
        started = false;
        flicker = false;
        title = false;

        entered = new LinkedList<Integer>();

        score = 0;
    }

    protected void processKeyEvent(final KeyEvent ke) {
        if(KeyEvent.KEY_PRESSED == ke.getID()) {
            entered.add(ke.getKeyCode());
        }
    }

    // All game's logic goes in here, which is triggered by the timer in the constructor
    public void actionPerformed(ActionEvent e) {

        // Shared int to bring down size a little
        int i;

        // Process the user's key input
        if(entered.size() > 0) {
            i = entered.removeFirst();
            if(i == KeyEvent.VK_ENTER) {
                restart();
            } else if(!lost) {
                int togo = 0;
                switch(i) {
                case KeyEvent.VK_UP:
                    togo = 0;
                    break;
                case KeyEvent.VK_RIGHT:
                    togo = 1;
                    break;
                case KeyEvent.VK_DOWN:
                    togo = 2;
                    break;
                case KeyEvent.VK_LEFT:
                    togo = 3;
                    break;
                }

                if((togo + 2) % 4 != direction) {
                    direction = togo;
                }
            }
        }

        if(!lost) {
            // move the snake forward
            switch(direction) {
            case 0:
                snakeX.push(snakeX.getFirst());
                snakeY.push(snakeY.getFirst() - 1);
                break;
            case 1:
                snakeX.push(snakeX.getFirst() + 1);
                snakeY.push(snakeY.getFirst());
                break;
            case 2:
                snakeX.push(snakeX.getFirst());
                snakeY.push(snakeY.getFirst() + 1);
                break;
            case 3:
                snakeX.push(snakeX.getFirst() - 1);
                snakeY.push(snakeY.getFirst());
                break;
            }

            // Add the rest of the snake once we've made our first movement
            if(!started) {
                snakeX.add(15);
                snakeX.add(15);
                snakeX.add(15);
                snakeY.add(15);
                snakeY.add(15);
                snakeY.add(15);

                started = true;
            }

            // If the snake's on top of the fruit, eat it, grow, and spawn a new fruit
            if(snakeX.getFirst() == appleX && snakeY.getFirst() == appleY) {
                score++;

                if(score % 10 == 0) {
                    playTenSuccess = true;
                } else {
                    playSuccess = true;
                }

                Integer endX = snakeX.getLast();
                snakeX.add(endX);
                snakeX.add(endX);
                snakeX.add(endX);
                snakeX.add(endX);
                Integer endY = snakeY.getLast();
                snakeY.add(endY);
                snakeY.add(endY);
                snakeY.add(endY);
                snakeY.add(endY);

                boolean inSnake;
                do {
                    inSnake = false;

                    appleX = rand.nextInt(BOARD_DIMENSIONS);
                    appleY = rand.nextInt(BOARD_DIMENSIONS - 1) + 1;

                    for(i = 0; i < snakeX.size(); i++) {
                        if(appleX == snakeX.get(i) && appleY == snakeY.get(i)) {
                            inSnake = true;
                        }
                    }
                } while(inSnake);
            }

            // End the game if the snake hits the border
            if(snakeX.getFirst() == -1 || snakeX.getFirst() == BOARD_DIMENSIONS ||
               snakeY.getFirst() == 0 || snakeY.getFirst() == BOARD_DIMENSIONS) {
                lost = true;
                playLose = true;
                snakeX.removeFirst();
                snakeY.removeFirst();
                repaint();
                return;
            }

            // End the game if the snake hits itself
            for(i = 1; i < snakeX.size(); i++) {
                if(snakeX.getFirst() == snakeX.get(i) &&
                   snakeY.getFirst() == snakeY.get(i)) {
                    lost = true;
                    playLose = true;
                }
            }

            // Remove the last piece of the snake
            snakeX.removeLast();
            snakeY.removeLast();
        }

        repaint();
    }

    public void paint(final Graphics gr) {
        Graphics2D g;

        if(offscreenBuffer == null) {
            offscreenBuffer = createImage(WINDOW_SIZE, WINDOW_SIZE + MENU_HEIGHT);

            background = createImage(WINDOW_SIZE, WINDOW_SIZE);
            g = (Graphics2D) background.getGraphics();

            // Draw onto the background here
            g.setColor(C[0]);
            g.fillRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);
        }

        g = (Graphics2D) offscreenBuffer.getGraphics();

        // Draw color for the bottom menu
        g.setColor(C[3]);
        g.fillRect(0, WINDOW_SIZE, WINDOW_SIZE, MENU_HEIGHT);

        g.setColor(C[1]);
        g.drawLine(0, WINDOW_SIZE, WINDOW_SIZE, WINDOW_SIZE);

        // Always draw the score
        g.drawString("SCORE:", 5, WINDOW_SIZE + 15);
        g.drawString(Integer.toString(score), 52, WINDOW_SIZE + 15);

        // Clear the space with the background
        g.drawImage(background, 0, 0, this);

        if(title) {
            g.drawString("PRESS ENTER TO START", 450, WINDOW_SIZE + 15);
        } else {
            if(lost) {
                g.drawString("GAME OVER. PRESS ENTER TO RESTART", 355, WINDOW_SIZE + 15);
            }

            // Draw the apple
            g.setColor(C[2]);
            g.fillRect(appleX * PIXEL_SIZE, appleY * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);

            // Draw the snake
            if((lost && flicker) || !lost) {
                g.setColor(C[1]);
            }

            int i;
            for(i = 0; i < snakeX.size(); i++) {
                g.fillRect(snakeX.get(i) * PIXEL_SIZE, snakeY.get(i) * PIXEL_SIZE,
                           PIXEL_SIZE, PIXEL_SIZE);
            }

            flicker = !flicker;
        }

        gr.drawImage(offscreenBuffer, 0, 0, this);
    }

    // Don't include this method if you don't want audio
    public void run() {
        try {
            SourceDataLine line = (SourceDataLine)
                AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,
                                                      new AudioFormat(16000, 8, 1, true, false)
                                                      )
                                    );
            line.open();
            line.start();
            for(;;) {
                Thread.sleep(TIMER_TICK_RATE);

                if(playSuccess) {
                    playSuccess = false;
                    line.write(success_sound, 0, 1024);
                }

                if(playLose) {
                    playLose = false;
                    line.write(lose_sound, 0, 2048);
                }

                if(playTenSuccess) {
                    playTenSuccess = false;
                    line.write(ten_success_sound, 0, 1024);
                }
            }
        } catch(final Exception ex) {
        }
    }

    // Used for double-buffering
    static private Image offscreenBuffer;
    // The main background of the frame
    static private Image background;
    static private Random rand;

    // snakeX holds the x-coordinates of the snake's parts, snakeY the y-coordinates
    static private LinkedList<Integer> snakeX;
    static private LinkedList<Integer> snakeY;

    // The current position of the apple
    static private int appleX;
    static private int appleY;

    // The current position of the part of the snake to clear
    static private int cX;
    static private int cY;

    // The direction the snake is currently going
    // 0 means up, 1 means right, 2 means down, 3 means left
    static private int direction;

    // Whether the player has lost the game
    static private boolean lost;

    // Boolean for whether we've started our first movement, to prevent the snake losing
    // from when it's coiled at the start of the game
    static private boolean started;

    // A boolean for making the snake flicker upon loss
    static private boolean flicker;

    // A list of all the characters the user has entered, so they can back-propogate.
    static private LinkedList<Integer> entered;

    // Player's score
    static private int score;

    // Whether the game is in title screen mode
    static private boolean title;

    // Sound effects
    static private byte[] success_sound = new byte[1024];
    static private byte[] ten_success_sound = new byte[1024];
    static private byte[] lose_sound = new byte[2048];
    // Play a sound for when you eat one
    static private boolean playSuccess;
    // Play a sound for when you lose
    static private boolean playLose;
    // Play a sound for when you've eaten a multiple of ten
    static private boolean playTenSuccess;


    T() {
        super("4k-snake");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WINDOW_SIZE, WINDOW_SIZE + MENU_HEIGHT);

        // Do initialization here
        restart();

        lost = true;
        title = true;

        // Generate success audio sample
        int i;
        for(i = 0; i < 1024; i++) {
            success_sound[i] = i % 40 < 20 ? (byte)' ' : (byte)'~';
        }

        for(i = 0; i < 1024; i++) {
            if(i < 512) {
                ten_success_sound[i] = i % 20 < 10 ? (byte)' ' : (byte)'~';
            } else {
                ten_success_sound[i] = i % 15 < 5 ? (byte)' ' : (byte)'~';
            }
        }

        // Generate lose audio sample
        for(i = 0; i < 2048; i++) {
            lose_sound[i] = i % 256 < 32 ? (byte)' ' : (byte)'~';
        }

        // Only if we need to play audio
        new Thread(this).start();

        // Trigger running the actionPerformed() method at TIMER_TICK_RATE
        new Timer(TIMER_TICK_RATE, this).start();

        setVisible(true);
    }

    public static void main(String[] args) {
        new T();
    }

}
