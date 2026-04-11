package ihm;

import config.Config;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogPanel extends JPanel {

    private JTextArea logArea;

    public LogPanel() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(Config.LARGEUR_ECRAN, 100));
        this.setBackground(new Color(10, 10, 10));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(10, 10, 10));
        logArea.setForeground(new Color(200, 200, 200));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBackground(new Color(10, 10, 10));
        scroll.getViewport().setBackground(new Color(10, 10, 10));

        this.add(scroll, BorderLayout.CENTER);
        ajouterLog("Systeme initialise...");
    }

    public void ajouterLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}