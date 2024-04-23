import java.awt.*;
import javax.swing.*;
import java.awt.event.*;


public class Searching {
    SearchBar searchBar;
    String placeholder = "Search...";
    Swap swap;
    String resp;
    JTextField searchField;
    Searching() {
        searchBar = new SearchBar();
    }
    
    Searching(Swap swp) {
        searchBar = new SearchBar();
        this.swap = swp;
    }

    JPanel getSearchPanel() {
        SearchBar searchPanel = this.searchBar;
        searchPanel.setLayout(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 50));

        searchField = new JTextField(100);

        searchField.setFont(new Font("Arial", Font.PLAIN, 20));
        searchField.setText(placeholder);
        searchField.setForeground(Color.lightGray);
        searchField.setBackground(new Color(0, 0, 0, 0));
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (searchField.getText().equals(placeholder)) {
                    searchField.setText("");
                    searchField.setForeground(Color.WHITE);
                }
            }
        });
        JButton button = new JButton();
        NoScalingIcon icon = new NoScalingIcon(
                new ImageIcon("icons\\system-regular-42-search.png"));
        button.setIcon(icon);
        button.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 0));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        // button.setEnabled(false);
        ActionListener searchAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        };

        button.addActionListener(searchAction);
        searchField.addActionListener(searchAction);

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(button, BorderLayout.EAST);
        return searchBar;
    }

    void performSearch(){
        String file_name = (searchField.getText());
        System.out.println(file_name);
        searchField.setText(placeholder);
        {
            // here comes the request code  to send a HTTP GET 
            // parse it to get 
            // filecontents,
            // name,metadata
            Request req = new Request();
            req.get("https://namenode-esdwt.run-ap-south1.goorm.site/receive?filename=" + file_name);
            // System.out.println("have requeted and response is " + req.reply_in_text());
            resp = req.reply_in_text();

        }

        searchField.setForeground(Color.lightGray);
        if (Swap.flag == 1) {
            
            Swap.flag = 0;
            swap.yamete(file_name, "meta", resp);
        }
        else {
            Swap.change_lable(file_name,"meta",resp);
        }
    }


}