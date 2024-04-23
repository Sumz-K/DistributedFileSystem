import java.awt.BorderLayout;
import java.awt.*;
import javax.swing.*;

public class Swap {
    public static JFrame frame ;
    public static JPanel center_panel;
    public static JPanel north_panel;
    public static JPanel toast_bar;
    public static JLabel file_nameva;
    static int flag = 1;
    public home h;
     

    public Swap(JFrame frame, JPanel center_panel, JPanel north_panel,home h) {
        Swap.frame = frame;
        Swap.center_panel = center_panel;
        Swap.north_panel = north_panel;
        this.h = h;
    }

    static void raise_toast(int f) {
        Toast t;
        if (f == 1) {
        t = new Toast("Upload Successful", 1);
        } else {
        t = new Toast("Upload Failed", 2);
        }   
        toast_bar.add(t.panel);
        t.display();
        toast_bar.revalidate();
        toast_bar.repaint();
    }
    static void change_lable(String file,String metadata , String content) {
        file_nameva.setText(file);
        addcard(file,metadata,content);
        
    }

    void yamete(String file,String metadata , String content) {

        JPanel searchPanel = h.createSearchPanel();
        searchPanel.setPreferredSize(new Dimension(100, 100));
        JPanel housing = new JPanel();
        housing.setLayout(new BorderLayout());
        housing.setBackground(new Color(0x1d1d1d));

        housing.add(searchPanel, BorderLayout.NORTH);
        JLabel fname = new JLabel();
        fname.setBackground(new Color(0x1d1d1d));
        fname.setText("         File Name:    ");
        fname.setFont(new Font("Arial", Font.BOLD, 20));
        fname.setForeground(Color.WHITE);
        housing.add(fname, BorderLayout.WEST);
        JLabel filenameva = new JLabel();
        filenameva.setText(file);
        filenameva.setFont(new Font("Rockwell", Font.PLAIN, 20));
        housing.add(filenameva, BorderLayout.CENTER);
        filenameva.setForeground(Color.WHITE);
        frame.add(housing, BorderLayout.NORTH);
        file_nameva = filenameva;
        JPanel center = new JPanel();
        frame.remove(center_panel);

        center.setPreferredSize(new Dimension(800, 800));
        center.setBackground(new Color(0x1d1d1d));
        // JScrollPane scrollPane = new JScrollPane(north_panel);
        // scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        // north_panel.setSize(new Dimension(20, 20));
        // center_panel.setSize(new Dimension(100,100));
        frame.add(north_panel, BorderLayout.CENTER);
        // scrollbarthing();

        frame.revalidate();
        frame.repaint();
        Swap.center_panel = north_panel;
        Swap.north_panel = housing;
        // Swap.center_panel.setBackground(Color.WHITE);
        addcard(file,"card",content);
    }

    static void addcard(String file, String meta, String content) {
        center_panel.add(new FileCard(file, meta, content));
        center_panel.revalidate();
        center_panel.repaint();

    }
    

}
