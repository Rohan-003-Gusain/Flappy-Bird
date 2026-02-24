package game;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;

public class Hard extends JPanel implements ActionListener, KeyListener{

	private static final long serialVersionUID = 1L;
	
    Stngs stng;
    
    // Game state flags
    private boolean isMuted = false;
    private boolean jumpingAllowed = true;
    private boolean gameStarted = false;
    private boolean gameOver = false;
    
    // Screen dimension
    private int BORDER_WIDTH = 350;  
    private int BORDER_HEIGHT = 640;
    
 // Score tracking 
    private int score = 0;
    private int highScore = 0;
    private int lastSpeedIncreaseScore  = 0;

    // Bird properties
    private final int birdX = BORDER_WIDTH / 8;
    private final int birdY = BORDER_HEIGHT / 2;
    private final int birdWidth = 34;
    private final int birdHeight = 24;
    private double gravity = 0.6;
    private double velocityY = 0;
    private int jumpPower = -7;
    private final int maxFall = 8;

    // Pipe properties
    private int pipeX = BORDER_WIDTH;
    private int pipeY = 0;    
    private int velocityX = -4; // Horizontal speed of pipe
    private int pipeVelocityY = 3;
    private int pipeWidth = 64;
    private int pipeHeight = 512; 

    // Game objects
    private Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;
    private Bird bird;
    private ArrayList<Pipe> pipes;

    // Timers
    Timer gameloop;
    Timer placePipesTimer;
    private int PIPE_SPAWN_DELAY = 1500;

    // Sound 
    Sound themeSound, wingSound, coinSound, deathSound; 
    
    // ========== INNER CLASSES ==========
    
    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }    
    }

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false; 
        boolean directionSet = false;
        int direction = 0;

        Pipe(Image img) {
            this.img = img;
        }
    } 

    class Sound {
        private Clip clip;

        Sound(String path) {
            try {
            	AudioInputStream audioStream = AudioSystem.getAudioInputStream(getClass().getResource(path));
                clip = AudioSystem.getClip();
                clip.open(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void start() {
            if (!isMuted && clip != null) {
                clip.setFramePosition(0);
                clip.start();
            }
        }

        void loop() {
            if (!isMuted && clip != null) {
                clip.setFramePosition(0);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }

        void stop() {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
        
        Clip getClip() {
            return clip;
        }

        void mute(boolean mute) {
            isMuted = mute;
            if(isMuted) stop();
        }
    }
    
    // ========== CONSTRUCTOR ==========
    
    Main frame;
    public Hard(Main frame) {
        this.frame = frame;
        
        setPreferredSize(new Dimension(BORDER_WIDTH, BORDER_HEIGHT));
        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        // Load Images
        backgroundImg = new ImageIcon(getClass().getResource("/assets/photos/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/assets/photos/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/assets/photos/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/assets/photos/bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Timer 
        placePipesTimer = new Timer(PIPE_SPAWN_DELAY, e -> placePipes());
        gameloop = new Timer(1000/60, this);   
        
        // Load
        themeSound = new Sound("/assets/sounds/theme.wav"); // Added theme sound
        coinSound = new Sound("/assets/sounds/coin.wav");
        wingSound = new Sound("/assets/sounds/wing.wav"); // Added wing sound
        deathSound = new Sound("/assets/sounds/death.wav"); // Added death sound

        themeSound.loop(); 

        ImageIcon icon = new ImageIcon(
        		new ImageIcon(getClass().getResource("/assets/settingsIcons/setting.png"))
        		.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));

        JButton settingsBtn = new JButton(icon);
        settingsBtn.setBounds(260, 10, 30, 30);
        settingsBtn.setFocusable(false);
        settingsBtn.setBorderPainted(false);
        settingsBtn.setContentAreaFilled(false);
        settingsBtn.setFocusPainted(false);

        stng = new Stngs(null, null, this, frame);
        stng.setVisible(false);
        add(stng);
        
        settingsBtn.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
            	stng.setVisible(!stng.isVisible());
                stng.revalidate();;
                stng.repaint();
                if (stng.isVisible()) {
                    gameloop.stop();
                    placePipesTimer.stop();
                } else {
                	requestFocusInWindow();
                }
            }
        });
        
        add(settingsBtn);

    }

    public void CloseHardPanel() {
        themeSound.stop();
        coinSound.stop();
        deathSound.stop();
        gameloop.stop();
        placePipesTimer.stop();

        pipes.clear();
    }

    public void mutesound(boolean mute) {
        isMuted = mute;
    
        if (isMuted) {
            if (themeSound != null) themeSound.stop();
            // if (wingSound != null) wingSound.stop();
            if (coinSound != null) coinSound.stop();
            if (deathSound != null) deathSound.stop();
        } else {
            if (themeSound != null) themeSound.loop();
        } 
    }

    // ========== GAME LOGIC ==========
    
    private void placePipes() {
    	int randomPipeY = -pipeHeight / 2 - (int)(Math.random() * 150);
        int openingSpace = BORDER_HEIGHT / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        
        pipes.add(topPipe);
        pipes.add(bottomPipe);
    }

    private void movePipes() {
        
        if (!gameStarted) return;
        if (pipes.size() < 2) return;

        Random rand = new Random();

        for (int i = 0; i < pipes.size(); i += 2) { 
            Pipe topPipe = pipes.get(i);
            Pipe bottomPipe = pipes.get(i + 1);

            int offSet = 100;
            int topLimit = -pipeHeight + offSet;
            int bottomLimit = BORDER_HEIGHT - offSet;
            
            if (rand.nextBoolean()) {
            	
                if (!topPipe.directionSet) {
                    topPipe.directionSet = true;
                    topPipe.direction = (int)(Math.random() * 3) -1;
                }
                
                if (topPipe.direction == -1 && topPipe.y > topLimit) {
                    topPipe.y -= pipeVelocityY;
                    bottomPipe.y -= pipeVelocityY; 
                    
                } else if (topPipe.direction == 1 && bottomPipe.y < bottomLimit) {
                    topPipe.y += pipeVelocityY;
                    bottomPipe.y += pipeVelocityY; 
                }
                
            }
        }
    }

    private void updateDifficulty() {
        if (score >= 15 && score % 15 == 0 && score > lastSpeedIncreaseScore) {
        	lastSpeedIncreaseScore = (int) score;

        	if (velocityX > maxFall) {
            	velocityX -= 0.5;
            }
            
            if (PIPE_SPAWN_DELAY > 900) {
            	PIPE_SPAWN_DELAY -= 150;
            	placePipesTimer.setDelay(PIPE_SPAWN_DELAY);
            }         
        }
    }

    private void triggerGameOver() {
        if (!gameOver) { 
            gameOver = true;
            deathSound.start();
            themeSound.stop();
            placePipesTimer.stop();
            gameloop.stop();

        }
    }
    
    // Movement and Collision Detection 
    private void move() {
        if (!gameStarted) return;

        velocityY += gravity;
        
        if (velocityY > 8) {
        	velocityY = 8;
        }
        
        bird.y += velocityY;

        movePipes();

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                if (pipe.y > 0) {
                    score += 1; 
                    System.out.println("Score Updated: " + score);
                }
                pipe.passed = true;
                coinSound.start();
                updateDifficulty();
            }

            if (collision(bird, pipe)) {
                triggerGameOver();
            }

            if (bird.y < 0) {
                triggerGameOver();
            }

            if (bird.y + bird.height >= BORDER_HEIGHT) {
                triggerGameOver();               
            }
        }

    }

    // Collision Detection Function
    private boolean collision(Bird a, Pipe b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    // ========== RENDERING ==========
    
    private void draw(Graphics g) {
        try {
            g.drawImage(backgroundImg, 0, 0, BORDER_WIDTH, BORDER_HEIGHT, null);
            g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

            for(int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                g.drawImage(pipe.img, pipe.x, pipe.y, pipeWidth, pipeHeight, null);
            }

            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("High Score: "+ highScore, 10, 30);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score: " + score, 10, 60);
            
            if (gameOver) {
                g.setFont(new Font("Arial", Font.PLAIN, 40));
                g.drawString("Game Over : " + String.valueOf((int) score), BORDER_WIDTH / 8, BORDER_HEIGHT / 2);
               
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    // Repaint the Game Screen
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
    	
    	if (stng.isVisible()) {
    		return;
    	}

        if (e.getKeyCode() == KeyEvent.VK_SPACE && !stng.isVisible() && jumpingAllowed) { 
            if (!gameStarted) {
                gameStarted = true;
                gameloop.start();
                placePipesTimer.start();   

            }
            velocityY = jumpPower;
            jumpingAllowed = false;
        } 

        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) {
            restartGame();
        }
    } 


    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            jumpingAllowed = true;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void addNotify() {
    	super.addNotify();
    	requestFocusInWindow();
    }

    
    private void restartGame() {

        if (score > highScore) {
            highScore = score;
        }
        
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false; 
        gameStarted = false;
        jumpingAllowed = true;

        velocityX = -4;
        gravity = 0.6;
        jumpPower = -7;
        lastSpeedIncreaseScore = 0;

        gameloop.start();
        themeSound.start();
    }
}