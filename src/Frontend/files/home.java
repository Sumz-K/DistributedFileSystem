import java.awt.*;
import javax.swing.*;
public class home {
    JFrame frame;
    Swap swp;

    public home() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fill();
        frame.setSize(1980,1080);
        frame.setVisible(true);
    }
    public void fill() {

        // JPanel searchPanel = createSearchPanel();
        // searchPanel.setPreferredSize(new Dimension( 100 , 100));
        // frame.add(searchPanel, BorderLayout.NORTH);
        start();

        JPanel southPanel = createSouthPanel();
        frame.add(southPanel, BorderLayout.SOUTH);


    }

    public void fill_center(JPanel panel,Swap swp) {
        JPanel searchJPanel = createSearchPanel(swp);
        searchJPanel.setPreferredSize(new Dimension(100,150));
        panel.add(searchJPanel, BorderLayout.NORTH);
        searchJPanel.setBorder(BorderFactory.createEmptyBorder(5, 50, 50, 50));

        JLabel instruction = new JLabel();
        instruction.setText("Search or Upload a file");
        instruction.setFont(new Font("Arial", Font.BOLD, 50));
        instruction.setForeground(Color.WHITE);
        instruction.setHorizontalAlignment(SwingConstants.CENTER);
        
        
        panel.add(instruction, BorderLayout.CENTER);

    }

    void start() {
        
        JPanel contentPanel = new JPanel();
        JPanel northJPanel = new JPanel();
        northJPanel.setPreferredSize(new Dimension(100, 301));
        // contentPanel.setPreferredSize(new Dimension(100, 301));
        northJPanel.setBackground(new Color(0x1d1d1d));
        contentPanel.setLayout(new BorderLayout());
        
        contentPanel.setBackground(new Color(0x1d1d1d));
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.add(northJPanel, BorderLayout.NORTH);
        swp = new Swap(frame, contentPanel, northJPanel,this);
        fill_center(contentPanel, swp);
        
    }


    public JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        JButton btn = new JButton("Upload");
        return contentPanel;

    }

    public JPanel createSearchPanel() {
        // String placeholder = "Search...";
        Searching searchPanel = new Searching();
        return searchPanel.getSearchPanel() ;
    }
    public JPanel createSearchPanel(Swap swp) {
        // String placeholder = "Search...";
        Searching searchPanel = new Searching(swp);
        return searchPanel.getSearchPanel() ;
    }


    public JPanel createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(0x1d1d1d));

        JPanel toastPanel = createToastPanel();
        // toastPanel.setLayout(null);

        Swap.toast_bar = toastPanel;

        southPanel.add(toastPanel, BorderLayout.SOUTH);
        
        JPanel uploadPanel = new Upload(toastPanel).uploadPanel;
        uploadPanel.setBackground(new Color(0,0,0,0));
        southPanel.add(uploadPanel, BorderLayout.NORTH);
        return southPanel;
    }

    public JPanel createToastPanel()
    {
        JPanel toastPanel = new JPanel();
        toastPanel.setBackground(new Color(0x1d1d1d));
        toastPanel.setForeground(Color.WHITE);
        toastPanel.setPreferredSize(new Dimension(100,70));
        // JLabel lbl = new JLabel("TOAST");
        // lbl.setForeground(Color.WHITE);
        // toastPanel.add(lbl); 
        return toastPanel;
    }

    public static void main(String[] args) {
        try {

            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch (Exception e) {
            System.out.println("Look and Feel not set");
        }
        new home();
    }
}