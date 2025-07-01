import java.awt.*;
import javax.swing.*;

public class Stngs extends JPanel {

    JButton butn, b1, b2, b3;
    ImageIcon ImageIcon1, ImageIcon2, ImageIcon3;
    ImageIcon SIcon1, SIcon2, SIcon3;
    Image scaledImage1, scaledImage2, scaledImage3;
    JPanel PopUp;
    boolean isSoundOn = true;

    Easy easyPanel;
    Medium mediumPanel;
    Hard hardPanel;
    Main frame;
    public Stngs (Easy easyPanel, Medium mediumPanel, Hard hardPanel, Main frame) {
        this.easyPanel = easyPanel;
        this.mediumPanel = mediumPanel;
        this.hardPanel = hardPanel;
        this.frame = frame;
       
        this.setBounds(290, 10, 40, 100);
        this.setOpaque(false);
        this.setLayout(null);
        
        ImageIcon1 = new ImageIcon(getClass().getResource("SettingsPhotos/home.png"));
        scaledImage1 = ImageIcon1.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        SIcon1 = new ImageIcon(scaledImage1);

        ImageIcon2 = new ImageIcon(getClass().getResource("SettingsPhotos/soundOn.png"));
        scaledImage2 = ImageIcon2.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        SIcon2 = new ImageIcon(scaledImage2);

        ImageIcon3 = new ImageIcon(getClass().getResource("SettingsPhotos/soundOff.png"));
        scaledImage3 = ImageIcon3.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        SIcon3 = new ImageIcon(scaledImage3);

        PopUp = new JPanel();
        
        PopUp.setLayout(new GridLayout(3, 1, 5,5));
        PopUp.setBounds(0, 0, 30,100);
        PopUp.setOpaque(false);
        PopUp.setVisible(true); 

        b1 = new JButton(SIcon1);
        b1.setBorderPainted(false);
        b1.setContentAreaFilled(false);
        b1.setFocusPainted(false);
        b1.addActionListener(e ->{
            if (easyPanel != null) {
                easyPanel.setVisible(false);
                easyPanel.CloseEasyPanel();
            }

            if (mediumPanel != null) {
                mediumPanel.setVisible(false);
                mediumPanel.CloseMediumPanel();
            }

            if (hardPanel != null) {
                hardPanel.setVisible(false);
                hardPanel.CloseHardPanel();
            }
            
            frame.cardLayout.show(frame.mainPanel, "Home");
        });

        b2 = new JButton(SIcon2);
        b2.setBorderPainted(false);
        b2.setContentAreaFilled(false);
        b2.setFocusPainted(false);
        b2.addActionListener(e -> {
            if (easyPanel != null && easyPanel.isVisible()) {
                if (isSoundOn == true) {
                    b2.setIcon(SIcon3);
                    easyPanel.mutesound(true);
                    isSoundOn = false;
                } 
                
                else {
                    b2.setIcon(SIcon2);
                    easyPanel.mutesound(false);
                    isSoundOn = true;
                }
            }
            
            if (mediumPanel != null && mediumPanel.isVisible()) {
                if (isSoundOn == true) {
                    b2.setIcon(SIcon3);
                    mediumPanel.mutesound(true);
                    isSoundOn = false;
                } 
                else {
                    b2.setIcon(SIcon2);
                    mediumPanel.mutesound(false);
                    isSoundOn = true;
                }
            }

            if (hardPanel != null && hardPanel.isVisible()) {
                if (isSoundOn == true) {
                    b2.setIcon(SIcon3);
                    hardPanel.mutesound(true);
                    isSoundOn = false;
                } 
                else {
                    b2.setIcon(SIcon2);
                    hardPanel.mutesound(false);
                    isSoundOn = true;
                }
            }
        });

        PopUp.add(b1);
        PopUp.add(b2);

        add(PopUp);
     
    }

}


