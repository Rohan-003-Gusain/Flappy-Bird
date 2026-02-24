package game;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Main extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	
    CardLayout cardLayout;
    JPanel mainPanel, homePanel;
    Image bg, birdImg;
    JLabel heading;
    JButton butn, butn2, butn3;

    Main() {
        setTitle("My JFrame");
        setSize(350,640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        homePanel = new JPanel(){
            public void paintComponent(Graphics g) {
                super.paintComponents(g);
                draw(g);
            }
        };
        
        homePanel.setLayout(null);

        bg = new ImageIcon(getClass().getResource("/assets/photos/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/assets/photos/flappybird.png")).getImage();

        heading = new JLabel("<html>FLAPPY<br>BIRD 2</html>");
        heading.setBounds(90, 20, 170, 100);
        heading.setFont(new Font("Arial", Font.BOLD, 40));
        homePanel.add(heading);

        butn = new JButton("Easy");
        butn.setBounds(125, 300, 100, 30);
        setuButton(butn);
        butn.addActionListener(this);
        homePanel.add(butn);

        butn2 = new JButton("Medium");
        butn2.setBounds(125, 380, 100, 30);
        setuButton(butn2);
        butn2.addActionListener(this);
        homePanel.add(butn2);

        butn3 = new JButton("Hard");
        butn3.setBounds(125, 460, 100, 30);
        setuButton(butn3);
        butn3.addActionListener(this);
        homePanel.add(butn3);

        mainPanel.add(homePanel, "Home");

        setContentPane(mainPanel);
        setVisible(true);

    }
    
    private void setuButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(Color.BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFocusable(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 153, 255)); 
            }
        
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.BLUE); 
            }
        });
    }

    public void draw(Graphics g) {
        try {
            g.drawImage(bg, 0, 0, 350, 640, null);
            g.drawImage(birdImg, 130, 140, 64, 54, null);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponents(g);
        draw(g);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == butn) {
            mainPanel.add(new Easy(this), "Easy");
            cardLayout.show(mainPanel, "Easy");

        } else if (e.getSource() == butn2) {
            mainPanel.add(new Medium(this), "Medium");
            cardLayout.show(mainPanel, "Medium");
            
        } else if (e.getSource() == butn3) {
            mainPanel.add(new Hard(this),"Hard");
            cardLayout.show(mainPanel, "Hard");
        }
            revalidate();
            repaint();
        }

    public static void main(String[] args) {
        new Main();
    }
}