import java.awt.geom.RoundRectangle2D;
import java.awt.Graphics2D;
import javax.swing.*;
import java.awt.*;


public class ToastLabel extends JPanel {
    private static final Color GRADIENT_COLOR_1 = new Color(0xE3FEF7);
    private static final Color GRADIENT_COLOR_2 = new Color(0xC5FF95);
    private static final Color GRADIENT_COLOR_3 = new Color(0x00A372);

    private static final int CORNER_RADIUS = 10;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // // Enable anti-aliasing for smooth edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D roundRect = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 
                 2*CORNER_RADIUS, 2*CORNER_RADIUS);

        
        int x = 10;
        int y = 10;
        int width = 200;

        // Define colors for the gradient
        Color[] colors = { GRADIENT_COLOR_1,GRADIENT_COLOR_2, GRADIENT_COLOR_3};

        float[] positions = {0.0f, 0.5f, 1.0f};

        LinearGradientPaint paint = new LinearGradientPaint(
                x, y, x + width, y, positions, colors);

        g2d.setPaint(paint);
        g2d.fill(roundRect);
    }
}
