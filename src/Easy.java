import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.*;

public class Easy extends JPanel implements ActionListener, KeyListener{

    Stngs stng;
    private boolean isMuted = false;
    boolean jumpingAllowed = true;
    boolean gameStarted = false;
    int bordrWidth = 350;  
    int bordrHeight = 640;
    int highScore = 0;

    // Bird properties
    int birdX = bordrWidth / 8;
    int birdY = bordrHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;
    double gravity = 1;
    int jumpPower = -10;

    // Pipe properties
    int pipeX = bordrWidth;
    int pipeY = 0;
    int velocityX = -4; // move pipe to the left speed (simulates bird moving right)
    int velocityY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    final int pipeGap = 250;

    // Game objects
    Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;
    Bird bird;
    ArrayList<Pipe> pipes;
    Random random = new Random();

    // Timers
    Timer gameloop;
    Timer placePipesTimer;
    boolean gameOver = false;
    int score = 0;
    int lastSpeedIncreaseScroe = 0;
    private final int pipeSpawnDelay = 1500;

    // Sound effects
    Sound themeSound, wingSound, coinSound, deathSound; // gameOverSound;

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

        Pipe(Image img) {
            this.img = img;
        }
    } 

    class Sound {
        private Clip clip;

        public Sound(String soundFile) {
            try {
                File file = new File(soundFile); 
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void start() {
            if (!isMuted && clip != null) {
                clip.setFramePosition(0);
                clip.start();
            }
        }

        public void loop() {
            if (!isMuted && clip != null) {
                clip.setFramePosition(0);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }

        public void stop() {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }

        public Clip getClip() {
            return clip;
        }

        public void mute(boolean mute) {
            isMuted = mute;
            if(isMuted) stop();
        }

    }
    
    Main frame;
    public Easy (Main frame) {
        this.frame = frame;

        setPreferredSize(new Dimension(bordrWidth, bordrHeight));
        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        // Load Images
        backgroundImg = new ImageIcon(getClass().getResource("Photos/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("Photos/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("Photos/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("Photos/bottompipe.png")).getImage();

        // Bird object
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // Timer for pipe Generation and Game Loop
        placePipesTimer = new Timer(pipeSpawnDelay, e -> placePipes());

        gameloop = new Timer(1000/60, this);   
        
        // Load sounds
        themeSound = new Sound("src/Sounds/theme.wav"); // Added theme sound
        coinSound = new Sound("src/Sounds/coin.wav");
        wingSound = new Sound("src/Sounds/wing.wav"); // Added wing sound
        deathSound = new Sound("src/Sounds/death.wav"); // Added death sound

      //  gameOverSound = new Sound(""); // Added game over sound
        themeSound.loop(); // Start background music

        ImageIcon Imageicon = new ImageIcon(getClass().getResource("SettingsPhotos/setting.png"));
        Image scaledImage = Imageicon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaledImage);

        JButton butn = new JButton(icon);
        butn.setBounds(260, 10, 30, 30);
        butn.setBorderPainted(false);
        butn.setContentAreaFilled(false);
        butn.setFocusPainted(false);

        stng = new Stngs(this, null, null, frame);
        stng.setVisible(false);
        add(stng);
        
        butn.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                stng.setVisible(!stng.isVisible());
                stng.revalidate();;
                stng.repaint();

                if (stng.isVisible()) {
                    gameloop.stop();
                    placePipesTimer.stop();
                } 
     
            }
        });
        add(butn);

    }

    public void CloseEasyPanel() {
        themeSound.stop();
        coinSound.stop();
        wingSound.stop();
        deathSound.stop();

        gameloop.stop();
        placePipesTimer.stop();

        pipes.clear();
    }

    public void mutesound(boolean mute) {
        isMuted = mute;
    
        if (isMuted) {
            if (themeSound != null) themeSound.stop();
            if (wingSound != null) wingSound.stop();
            if (coinSound != null) coinSound.stop();
            if (deathSound != null) deathSound.stop();
        } else {
            if (themeSound != null) themeSound.loop();
        } 
    }
    
    
    // Function to Place New Pipes
    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 3 - Math.random() * (pipeHeight / 2));
        int openingSpace = bordrHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
 
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void updateDifficulty() {
        if (score >= 20 && score % 20 == 0 && score > lastSpeedIncreaseScroe) {
            lastSpeedIncreaseScroe = (int) score;

            if (velocityX > -10) velocityX--;
            if (jumpPower < -5) jumpPower++;           
        }
    }
    

    public void gameOver() {
        if (!gameOver) { 
            gameOver = true;
            themeSound.stop();
            placePipesTimer.stop();
            gameloop.stop();

        }
    }
    
    // Movement and Collison Detection 
    public void move() {
        if (!gameStarted) return;
        velocityY += gravity;
        bird.y += velocityY;

        for (int i = 0; i < pipes.size(); i++) { // Pipes Movement
            Pipe pipe = pipes.get(i);
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
                deathSound.start();
                gameOver();
            }

            if (bird.y < 0) {
                deathSound.start();
                gameOver();
            }

            if (bird.y + bird.height >= bordrHeight) {
                deathSound.start();
                gameOver();               
            }
        }
    }

    // Collision Detection Function
    public boolean collision(Bird a, Pipe b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    // Draw Method for Graphics
    public void draw(Graphics g) {
        try {
            //Backgrouond
            g.drawImage(backgroundImg, 0, 0, bordrWidth, bordrHeight, null);

            // Bird
            g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

            // Pipes
            for(int i = 0; i < pipes.size(); i++) {
                Pipe pipe = pipes.get(i);
                g.drawImage(pipe.img, pipe.x, pipe.y, pipeWidth, pipeHeight, null);
            }
            // Scrore
            g.setColor(Color.white);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("High Score: "+ highScore, 10, 30);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score: " + score, 10, 60);
            
            if (gameOver) {
                g.setFont(new Font("Arial", Font.PLAIN, 40));
                g.drawString("Game Over : " + String.valueOf((int) score), bordrWidth / 8, bordrHeight / 2);
               
            }
    
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void restartGame() {

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
        gravity = 1.0;
        jumpPower = -10;
        lastSpeedIncreaseScroe = 0;

        gameloop.start();
        themeSound.start();
    }

    // Repaint the Game Screen
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        requestFocusInWindow();
        draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    // Jump to Spacebar Press
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_SPACE) { 
            
            if (!gameStarted && !stng.isVisible()) {
                gameStarted = true;
                gameloop.start();
                placePipesTimer.start();   

            } else if (!gameloop.isRunning() && !gameOver && !stng.isVisible()) {
                gameloop.start();
                placePipesTimer.start();
            }
        } 

        if (!stng.isVisible() && jumpingAllowed) { 
            velocityY = jumpPower;
            jumpingAllowed = false; 
        }

        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER && !stng.isVisible()) {
            restartGame();
        }
    } 

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            jumpingAllowed = true;
        }
    }

}