package ihm;

import config.Config;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogPanel extends JPanel {

    private JTextArea logArea;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public LogPanel() {

        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(Config.LARGEUR_ECRAN, 100));
        this.setBackground(new Color(10, 10, 10));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(10, 10, 10)); 
        logArea.setForeground(new Color(200, 200, 200));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setMargin(new java.awt.Insets(5, 10, 5, 10));

        JScrollPane scroll = new JScrollPane(logArea);
        


        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
        

        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.setBackground(new Color(10, 10, 10));
        scroll.getViewport().setBackground(new Color(10, 10, 10));

        this.add(scroll, BorderLayout.CENTER);
        ajouterLog("Système initialisé...");
    }

    public void ajouterLog(String message) {
        String time = LocalTime.now().format(dtf);
        logArea.append("[" + time + "] " + message + "\n");

        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}