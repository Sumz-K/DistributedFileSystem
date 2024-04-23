import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import java.awt.*;

public class SearchBar extends JPanel {
    private static final Color BACKGROUND_COLOR = new Color(0x1d1d1d);
    private static final Color SEARCH_BACKGROUND_COLOR = new Color(0x333333);
    private static final int CORNER_RADIUS = 50;
    private static final int SEARCH_PADDING = 30;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Set up anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw search bar
        int searchBarWidth = getWidth() - SEARCH_PADDING * 2;
        int searchBarHeight = 40;
        int searchBarX = SEARCH_PADDING;
        int searchBarY = SEARCH_PADDING;

        RoundRectangle2D searchBarShape = new RoundRectangle2D.Float(searchBarX, searchBarY, searchBarWidth,
                searchBarHeight, CORNER_RADIUS / 2, CORNER_RADIUS / 2);
        g2d.setColor(SEARCH_BACKGROUND_COLOR);
        g2d.fill(searchBarShape);
        g2d.dispose();
    }
    
    
}