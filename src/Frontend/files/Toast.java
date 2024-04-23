import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


public class Toast {
    ToastLabel panel;
    int type;
    public Toast(String msg, int type) {
        panel = new ToastLabel();
        this.type = type;
        JLabel lbl = new JLabel();
        lbl.setText(msg);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8,8));
        panel.setPreferredSize(new Dimension(200, 50));
        NoScalingIcon icon = new NoScalingIcon(
                new ImageIcon("icons\\check.png"));
        lbl.setIcon(icon);
        panel.setForeground(Color.WHITE);
        panel.setBackground(new Color(0,0,0,0));
        panel.add(lbl);
    }
    

    public void display() {
        
        Timer timer = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.start();
        panel.setVisible(true);
    }

}