package space.limbo.BeatBox.Utils;

import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.JFrame;

public class WindowUtils {
    public static void displayOnCenter(JFrame frame) {
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        int width = dim.width;
        int height = dim.height;
        int w = frame.getWidth();
        int h = frame.getHeight();
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        frame.setLocation(x, y);
    }
}
