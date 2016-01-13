package space.limbo.BeatBox.Custom;

import java.io.File;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.LayoutManager;
import java.awt.Graphics;

public class BgPanel extends JPanel {
    static final long serialVersionUID = 123413252098314L;

    public BgPanel(LayoutManager layout) {
        super(layout);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            BufferedImage bgImage = ImageIO.read(new File("bg.png"));
            g.drawImage(bgImage, 0, 0, 680, 480, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

