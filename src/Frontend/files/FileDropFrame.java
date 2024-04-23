import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.List;
import java.util.*;
import javax.swing.border.Border;

import org.json.JSONObject;


public class FileDropFrame extends JFrame {
    List<File> fileList = new ArrayList<>();
    Swap swp;
    public FileDropFrame() {
        super("Drag and Drop");
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeUI();
    }

    private void initializeUI() {
        JPanel coverPanel = new JPanel();
        coverPanel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
        coverPanel.setBackground(new Color(0xD9D9D9));
        JPanel dropPanel = createDropPanel();

        JPanel donePanel = new JPanel();
        JButton doneButton = new JButton("Done");
        donePanel.add(doneButton);

        ActionListener onDone = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // System.out.println("pressed done button");
                // Swap.raise_toast(1);
                System.out.println("raised toast");
                dispose();
            }
        };

        doneButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                //System.out.println("pressed done button");
                Request req = new Request();
                for (File file : fileList) {
                    try {
                        String contents = readFile(file);
                        String fileName = file.getName();
                        // System.out.println(fileName + " " + contents);
                        JSONObject jobj = new JSONObject();
                        jobj.put("filename", fileName);
                        jobj.put("contents", contents);
                        dispose();
                        req.post("https://namenode-esdwt.run-ap-south1.goorm.site/receive", jobj);
                        System.out.println("resp" + req.reply_in_text());
                    } catch (Exception ex) {
                        System.out.println("file not find "+ex.getMessage());
                    }
                }
                Swap.raise_toast(1);
                
                System.out.println("raised toast");
               
            }
        });

        coverPanel.add(dropPanel);
        add(coverPanel, BorderLayout.CENTER);
        add(donePanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setVisible(true);

    }
    
    public JPanel createToastPanel() {
        JPanel toastPanel = new JPanel();
        toastPanel.setBackground(new Color(0x1d1d1d));
        toastPanel.setForeground(Color.WHITE);
        toastPanel.setPreferredSize(new Dimension(100, 70));
        // JLabel lbl = new JLabel("TOAST");
        // lbl.setForeground(Color.WHITE);
        // toastPanel.add(lbl);
        return toastPanel;
    }
    private JPanel createDropPanel() {

        JPanel dropPanel = new JPanel();
        dropPanel.setLayout(new BorderLayout());// Fixed BoxLayout creation
        dropPanel.setBackground(new Color(0xF1F1F1));
        dropPanel.setPreferredSize(new Dimension(400, 300));
        Border dashedBorder = BorderFactory.createStrokeBorder(
                new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 5.0f }, 0.0f),
                new Color(0x072e5b) // Use the specified color
        );
        dropPanel.setBorder(dashedBorder);


        JLabel dropLabel = new JLabel("Drag and drop files here");
        dropLabel.setForeground((Color.BLACK));
        dropLabel.setVerticalAlignment(SwingConstants.CENTER);
        dropLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dropPanel.add(dropLabel);

        DropTargetListener dropTargetListener = new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent e) {
                e.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = e.getTransferable();
                DataFlavor[] flavors = transferable.getTransferDataFlavors();

                dropPanel.remove(dropLabel);
                dropPanel.setLayout(new BoxLayout(dropPanel, BoxLayout.Y_AXIS));

                for (DataFlavor flavor : flavors) {
                    try {
                        if (flavor.isFlavorJavaFileListType()) {
                            List<File> files = (List<File>) transferable.getTransferData(flavor);
                            for (File file : files) {
                                String filename = file.getName();
                                JProgressBar bar = createProgressBar();
                                dropPanel.add(bar);
                                fileList.add(file);
                                fill(bar, filename, () -> {
                                    JPanel uploadedfilelabel = new JPanel();
                                    uploadedfilelabel.setPreferredSize(new Dimension(400, 50));
                                    uploadedfilelabel.setOpaque(false);

                                    JLabel label = new JLabel(filename);
                                    label.setForeground(new Color(0x000000));
                                    label.setFont((new Font("Rockwell", Font.PLAIN, 16)));

                                    JButton delButton = new JButton("Remove");
                                    delButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent arg0) {
                                            dropPanel.remove(uploadedfilelabel);
                                            dropPanel.revalidate();
                                            dropPanel.repaint();
                                        }
                                    });

                                    dropPanel.remove(bar);
                                    uploadedfilelabel.add(label);
                                    uploadedfilelabel.add(delButton);
                                    dropPanel.add(uploadedfilelabel);
                                    System.out.println("File received: " + filename);
                                    dropPanel.revalidate();
                                    dropPanel.repaint();
                                    try {
                                        String contents = readFile(file);
                                        // String fileName = file.getName();
                                        // JSONObject jobj = new JSONObject();
                                        // jobj.put("filename", fileName);
                                        // jobj.put("contents", contents);

                                        // Request req = new Request();
                                        // req.post("http://127.0.0.1:8080", jobj);
                                        
                                    } catch (FileNotFoundException e1) {
                                        e1.printStackTrace();
                                    }
                                });

                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                dropPanel.revalidate();
                dropPanel.repaint();
            }
        };

        new DropTarget(dropPanel, dropTargetListener);

        return dropPanel;
    }

    public JProgressBar createProgressBar()
    {
        JProgressBar bar = new JProgressBar();
        bar.setValue(0);
        // bar.setForeground(new Color(0xa4ceee));
        // bar.setPreferredSize(new Dimension(200, 20));
        bar.setStringPainted(true);
        return bar;
    }

    public void fill(JProgressBar bar, String filename, Runnable onCompletion)
    {
        Thread thread = new Thread(() -> {
            bar.setString("Uploading "+filename);
            try {
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(100);
                    bar.setValue(i);
                }

                SwingUtilities.invokeLater(onCompletion);

            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        });
        thread.start();
    }

    public String readFile(File file) throws FileNotFoundException
    {
        try {
           
            byte[] fileBytes = new byte[(int) file.length()];
            
            // Read the file content into a byte array
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileBytes);
            fileInputStream.close();
            
            // Encode the byte array into a Base64 string
           // System.out.println(Base64.getEncoder().encodeToString(fileBytes));
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}