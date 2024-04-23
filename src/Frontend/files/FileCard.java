import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

class FileCard extends JPanel {
    String encoded_string;
    String filename;

    public FileCard(String filename, String meta, String encdata) {
        this.filename = filename;
        this.encoded_string = encdata;
        setBackground(new Color(0x321d5e));
        setLayout(new BorderLayout());

        JPanel labPanel = new JPanel();
        labPanel.setLayout(new BorderLayout());
        labPanel.setOpaque(false);

        JLabel filenamelabel = new JLabel();
        filenamelabel.setForeground(Color.WHITE);
        filenamelabel.setFont(new Font("Montserrat", Font.PLAIN, 20));
        filenamelabel.setText(filename);
        filenamelabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel datelabel = new JLabel();
        datelabel.setForeground(Color.GRAY);
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("dd MMM yyyy");
        String date = today.format(dateformat);
        datelabel.setFont(new Font("Montserrat", Font.PLAIN, 16));
        // datelabel.setText(meta);
        datelabel.setHorizontalAlignment(SwingConstants.CENTER);

        labPanel.add(filenamelabel, BorderLayout.NORTH);
        labPanel.add(datelabel, BorderLayout.SOUTH);

        JPanel trasoverdata = new JPanel();
        trasoverdata.setLayout(new FlowLayout(FlowLayout.LEFT));
        trasoverdata.add(labPanel);
        trasoverdata.setOpaque(false);

        add(trasoverdata, BorderLayout.SOUTH);

        JPanel butPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        butPanel.setOpaque(false);

        JButton del = new JButton();
        NoScalingIcon del_icon = new NoScalingIcon(
                new ImageIcon("icons\\delete.png"));
        del.setIcon(del_icon);
        del.setOpaque(false);
        del.setBackground(new Color(0, 0, 0, 0));
        del.setBorder(null);
        del.setForeground(Color.RED);
        del.setFont(new Font("Montserrat", Font.PLAIN, 30));
        del.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code to execute when the button is clicked
                delpanel();
            }
        });

        JButton dwn = new JButton();
        dwn.setOpaque(false);
        dwn.setBorder(null);
        NoScalingIcon dwn_icon = new NoScalingIcon(
                new ImageIcon("icons\\down.png"));
        dwn.setIcon(dwn_icon);
        dwn.setBackground(new Color(0, 0, 0, 0));
        dwn.setForeground(new Color(0x38b6ff));
        dwn.setPreferredSize(new Dimension(30, 30));
        dwn.setFont(new Font("Montserrat", Font.PLAIN, 30));
        dwn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Code to execute when the button is clicked
                System.out.println(" download Button Clicked! " + encoded_string);
                downloading(Base64.getDecoder().decode(encoded_string),filename);
            }
        });

        butPanel.add(dwn);
        butPanel.add(del);

        add(butPanel, BorderLayout.NORTH);

        setPreferredSize(new Dimension(200, 200));
    }

    private void delpanel() {
        System.out.println(" delete Button Clicked!" + encoded_string);
        // request to delete
        Request request = new Request();
        request.delete("https://namenode-esdwt.run-ap-south1.goorm.site/receive?filename=" + filename);
        encoded_string = request.reply_in_text();
        System.out.println(getParent());
        getParent().remove(this);
        repaint();
        revalidate();
    }

    private void downloading(byte[] byteArray,String name) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(name));
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                FileOutputStream fos = new FileOutputStream(fileToSave);
                fos.write(byteArray);
                fos.close();
                JOptionPane.showMessageDialog(this, "File downloaded successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error downloading file: " + ex.getMessage());
            }
        }
    }
}