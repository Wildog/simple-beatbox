package space.limbo.BeatBox.Custom;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;

public class GlassPanel extends JPanel {
    static final long serialVersionUID = 1234135122L;
    int alpha = 200;
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, getWidth(), getHeight());
    };
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        this.repaint();
    }
};
