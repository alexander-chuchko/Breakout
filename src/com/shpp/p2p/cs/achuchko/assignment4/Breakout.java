package com.shpp.p2p.cs.achuchko.assignment4;

import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;


public class Breakout extends WindowProgram {

    /**
     * Width and height of application window in pixels
     */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /**
     * Dimensions of the paddle
     */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 10;

    /**
     * Offset of the paddle up from the bottom
     */
    private static final int PADDLE_Y_OFFSET = 30;

    /**
     * Number of bricks per row
     */
    private static final int NBRICKS_PER_ROW = 10;

    /**
     * Number of rows of bricks
     */
    private static final int NBRICK_ROWS = 10;

    /**
     * Separation between bricks
     */
    private static final int BRICK_SEP = 4;

    /**
     * Height of a brick
     */
    private static final int BRICK_HEIGHT = 8;

    /**
     * Radius of the ball in pixels
     */
    private static final int BALL_RADIUS = 10;

    /**
     * Offset of the top brick row from the top
     */
    private static final int BRICK_Y_OFFSET = 70;

    /**
     * Number of turns
     */
    private static final int NTURNS = 3;

    /**
     * Constant array with brick colors
     */
    private static final Color[] brickColors = {
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
    };

    /**
     * Array of constant strings
     */
    private static final String[] STRING_SET = {"CLICK TO START ON THE ", "FAILURE", "GAME OVER", "THE GAME IS WON", "CLICK TO RESTART GAME"};

    /**Counter of attempts used by the player*/
    public static int counterAttempt = 0;

    /**Broken brick counter*/
    public static int counterBricks = 0;

    /**Index of the last destroyed category of bricks*/
    public static int lastIndexColor = 4;

    /**The current rectangle */
    private GRect currentRect;

    /**
     * Variable speeds along the axes
     */
    private double vx, vy;

    public void run() {
        /**
         * We create components for the program and run the game
         * */
        creatingGameComponents();
    }

    /**
     * Creating components for the game
     */
    private void creatingGameComponents() {
        createBrick();
        counterBricks = NBRICK_ROWS * NBRICKS_PER_ROW;
        currentRect = createRacket();
        addMouseListeners();
        startGame();
    }

    /**
     * Application launch
     */
    private void startGame() {
        GLabel message;

        if (counterAttempt > 0 && counterAttempt < NTURNS) {
            message = showMessage(STRING_SET[1]);
            pause(1000);
            remove(message);
        }

        if (counterAttempt < NTURNS) {
            message = showMessage(STRING_SET[0] + (counterAttempt + 1) + "-ST TRY");
            waitForClick();
            remove(message);
        } else {
            message = showMessage(STRING_SET[2]);
            pause(2000);
            remove(message);
            restartGame();
        }

        if (counterAttempt < NTURNS) {
            playBall(createBall(), getRandomValue());
        }
    }

    /**
     * Restarting the game
     */

    private void restartGame() {
        showMessage(STRING_SET[4]);
        waitForClick();
        removeAll();
        counterAttempt = 0;
        lastIndexColor = 4;
        counterBricks = NBRICK_ROWS * NBRICKS_PER_ROW;
        creatingGameComponents();
    }

    /**
     * Method for creating a message
     */
    private GLabel showMessage(String informationString) {
        GLabel message = new GLabel(informationString);
        message.setFont("Verdana-18");
        message.setColor(Color.RED);
        message.setLocation((getWidth() - message.getWidth()) / 2, (getHeight() - message.getDescent()) / 2);
        add(message);

        return message;
    }

    /**
     * Ball Launch Method
     */
    private void playBall(GOval ball, double randomValue) {
        vx = randomValue;
        vy = 3.0;
        double speed = getSpeedValue(lastIndexColor);

        while (counterAttempt < NTURNS) {
            //right wall, change vy sign to negative
            if (ball.getX() + BALL_RADIUS * 2 >= getWidth()) {
                vx *= -1;
                //left wall, change the sign vy to positive
            } else if (ball.getX() <= 0) {
                vx *= -1;
                //bottom wall, change the sign vy to negative
            } else if (getHeight() <= ball.getY() + BALL_RADIUS * 2) {
                vy *= -1;
                remove(ball);
                pause(100);
                counterAttempt++;
                startGame();
            } //top wall, change vy sign to positive
            else if (ball.getY() <= 0) {
                vy *= -1;
            } else {
                //check collision with object - rocket
                GObject collider = getCollidingObject(ball);

                //Remove brick
                if (collider != null && collider.getHeight() == BRICK_HEIGHT) {
                    //Change speed about remove brick
                    Color color = collider.getColor();
                    int index = Arrays.asList(brickColors).indexOf(color);

                    if (lastIndexColor > index) {
                        lastIndexColor = index;
                        speed = getSpeedValue(lastIndexColor);
                    }

                    remove(collider);
                    counterBricks--;
                }

                //Checking the number of bricks
                if (counterBricks == 0) {
                    GLabel message = showMessage(STRING_SET[3]);
                    remove(ball);
                    pause(2000);
                    remove(message);
                    restartGame();
                    break;
                }
            }

            moveBall(ball, vx, vy, speed);
        }
    }

    /**
     * Method for getting speed index
     */
    private double getSpeedValue(int index) {
        double speed = 15.0;
        switch (index) {
            case 0:
                speed = 15.0 - 8.0;
                break;
            case 1:
                speed = 15.0 - 6.0;
                break;
            case 2:
                speed = 15.0 - 4.0;
                break;
            case 3:
                speed = 15.0 - 2.0;
                break;
            case 4:
                speed = 15.0;
                break;
        }

        return speed;
    }

    /**
     * Method for moving
     */
    private void moveBall(GOval ball, double vx1, double vy1, double time) {
        ball.move(vx1, vy1);

        pause(time);
    }

    /**
     * Method for getting the route around the points of the ball
     */
    private int[] getWaypointRoute(double vx, double vy) {
        int[] bypassDataArray = new int[]{0, 0, 0, 0};

        if (vx < 0 && vy > 0) {
            //start checking from point number 3
            bypassDataArray = new int[]{4, 3, 1, 2};
        } else if (vx > 0 && vy > 0) {
            //start checking from point number 4
            bypassDataArray = new int[]{3, 4, 2, 1};
        } else if (vx < 0 && vy < 0) {
            //start checking from point number 2
            bypassDataArray = new int[]{2, 1, 3, 4};
        } else if (vx > 0 && vy < 0) {
            //start checking from point number 1
            bypassDataArray = new int[]{1, 2, 4, 3};
        }

        return bypassDataArray;
    }

    /**
     * Method that returns the object involved in the collision
     */
    private GObject getCollidingObject(GObject gObject) {
        int[] bypassDataArray = getWaypointRoute(vx, vy);
        GObject gStone = null;

        for (int i = 0; i < 4; i++) {

            switch (bypassDataArray[i]) {
                //Describe point one
                case 1:
                    if (getElementAt(gObject.getX(), gObject.getY()) != null && ((vx > 0 && vy < 0) || (vx < 0 && vy < 0) || (vx < 0 && vy > 0))) {
                        gStone = getElementAt(gObject.getX(), gObject.getY());

                        var differenceX = gStone.getX() + gStone.getWidth() - gObject.getX();
                        var differenceY = gStone.getY() + gStone.getHeight() - gObject.getY();

                        if (differenceX == differenceY) {
                            vx = vx < 0 ? vx * -1 : vx;
                            vy = vy < 0 ? vy * -1 : vy;
                        } else if (differenceX >= differenceY && vx < 0 && vy < 0) {
                            vy *= -1;
                        } else if (differenceX <= differenceY && vx < 0 && vy < 0) {
                            vx *= -1;
                        } else if (vx > 0 && vy < 0) {
                            vy *= -1;
                        } else if (vx < 0 && vy > 0) {
                            vx *= -1;
                        }
                    }
                    break;
                //Describe point two
                case 2:
                    if (getElementAt(gObject.getX() + 2 * BALL_RADIUS, gObject.getY()) != null && ((vx > 0 && vy < 0) || (vx < 0 && vy < 0) || vx > 0 && vy > 0)) {
                        gStone = getElementAt(gObject.getX() + 2 * BALL_RADIUS, gObject.getY());

                        var differenceX = gObject.getX() + gObject.getWidth() - gStone.getX();
                        var differenceY = gStone.getY() + gStone.getHeight() - gObject.getY();

                        if (differenceX == differenceY) {
                            vx = vx > 0 ? vx * -1 : vx;
                            vy = vy < 0 ? vy * -1 : vy;
                        } else if (differenceX >= differenceY && vx > 0 && vy < 0) {
                            vy *= -1;
                        } else if (differenceX <= differenceY && vx > 0 && vy < 0) {
                            vx *= -1;
                        } else if (vx < 0 && vy < 0) {
                            vy *= -1;
                        } else if (vx > 0 && vy > 0) {
                            vx *= -1;
                        }
                    }
                    break;
                //Describe point three
                case 3:
                    if (getElementAt(gObject.getX(), gObject.getY() + BALL_RADIUS * 2) != null && ((vx < 0 && vy > 0) || (vx < 0 && vy < 0) || (vx < 0 && vy > 0) || (vx > 0 && vy > 0))) {
                        gStone = getElementAt(gObject.getX(), gObject.getY() + BALL_RADIUS * 2);

                        var differenceX = gStone.getX() + gStone.getWidth() - gObject.getX();
                        var differenceY = gObject.getY() + gObject.getHeight() - gStone.getY();

                        if (differenceX == differenceY) {
                            vx = vx < 0 ? vx * -1 : vx;
                            vy = vy > 0 ? vy * -1 : vy;
                        } else if (differenceX >= differenceY && vx < 0 && vy > 0) {
                            vy *= -1;
                        } else if (differenceX <= differenceY && vx < 0 && vy > 0) {
                            vx *= -1;
                        } else if (vx < 0 && vy < 0) {
                            vx *= -1;
                        } else if (vx > 0 && vy > 0) {
                            vy *= -1;
                        }
                    }
                    break;
                //Describe point four
                case 4:
                    if (getElementAt(gObject.getX() + 2 * BALL_RADIUS, gObject.getY() + 2 * BALL_RADIUS) != null && ((vx > 0 && vy < 0) || (vx > 0 && vy > 0) || (vx < 0 && vy > 0))) {
                        gStone = getElementAt(gObject.getX() + 2 * BALL_RADIUS, gObject.getY() + 2 * BALL_RADIUS);

                        var differenceX = gObject.getX() + gObject.getWidth() - gStone.getX();
                        var differenceY = gObject.getY() + gObject.getHeight() - gStone.getY();

                        if (differenceX == differenceY) {
                            vx = vx > 0 ? vx * -1 : vx;
                            vy = vy > 0 ? vy * -1 : vy;
                        } else if (differenceX >= differenceY && vx > 0 && vy > 0) {
                            vy *= -1;
                        } else if (differenceX <= differenceY && vx > 0 && vy > 0) {
                            vx *= -1;
                        } else if (vx > 0 && vy < 0) {
                            vx *= -1;
                        } else if (vx < 0 && vy > 0) {
                            vy *= -1;
                        }
                    }

                    break;
                default:
                    gStone = null;
            }

            if (gStone != null) {
                break;
            }
        }

        return gStone;
    }

    /**
     * Called on mouse drag to reshape the current rectangle
     */
    public void mouseDragged(MouseEvent e) {
        double x = e.getX() - (PADDLE_WIDTH / 2);
        double y = getHeight() - PADDLE_Y_OFFSET;

        if (counterAttempt < NTURNS) {
            if (e.getX() - PADDLE_WIDTH / 2 >= 0 && getWidth() - PADDLE_WIDTH / 2 >= e.getX()) {
                currentRect.setLocation(x, y);
            }
        }
    }

    /**
     * Method for creating a ball
     */
    private GOval createBall() {
        double radius = BALL_RADIUS;

        GOval ball = new GOval(radius * 2, radius * 2);
        ball.setFilled(true);
        ball.setColor(Color.BLACK);
        ball.setFillColor(Color.BLACK);
        ball.setLocation((getWidth() - radius * 2) / 2, (getHeight() - radius * 2) / 2);
        add(ball);

        return ball;
    }

    /**
     * Get a randomly generated number
     */
    private double getRandomValue() {
        RandomGenerator rgen = RandomGenerator.getInstance();
        double vx = rgen.nextDouble(1.0, 3.0);

        if (rgen.nextBoolean(0.5)) {
            vx = -vx;
        }

        return vx;
    }

    /**
     * Method for building a rocket
     */
    private GRect createRacket() {
        GRect racket = new GRect(PADDLE_WIDTH, PADDLE_HEIGHT);
        racket.setColor(Color.WHITE);
        racket.setFillColor(Color.BLACK);
        racket.setFilled(true);
        racket.setLocation((getWidth() - PADDLE_WIDTH) / 2, getHeight() - PADDLE_Y_OFFSET);
        add(racket);

        return racket;
    }

    /**
     * Method createBrick - creates bricks
     */
    private void createBrick() {
        double widthConsole = getWidth();
        double brickWidth = (widthConsole - BRICK_SEP * (NBRICKS_PER_ROW + 1)) / NBRICKS_PER_ROW;
        int offsetY = BRICK_Y_OFFSET;
        int index = 0;

        //Managing the number of rows
        for (int i = 0; i < NBRICK_ROWS; i++) {
            int offsetX = BRICK_SEP;

            //Managing the number of bricks in a row
            for (int j = 0; j < NBRICKS_PER_ROW; j++) {
                GRect square = new GRect(brickWidth, BRICK_HEIGHT);
                square.setColor(brickColors[index]);
                square.setFillColor(brickColors[index]);
                square.setFilled(true);
                square.setLocation(offsetX, offsetY);
                add(square);
                pause(5);

                offsetX += (BRICK_SEP + brickWidth);
            }

            offsetY += BRICK_HEIGHT + BRICK_SEP;

            if (i % 2 != 0) {
                index++;
            }
        }
    }
}
