package ihm;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;

public class FenetreGestion extends JFrame {

    public FenetreGestion() {
        setTitle("MENU PAUSE");
        setSize(250, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1, 10, 10));

        JButton btnReprendre = new JButton("REPRENDRE");
        btnReprendre.addActionListener(new ReprendreAction());

        JButton btnQuitter = new JButton("QUITTER LE JEU");
        btnQuitter.addActionListener(new QuitterAction());

        add(btnReprendre);
        add(new JButton("OPTIONS (non implémenté)"));
        add(btnQuitter);
    }

    private class ReprendreAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }

    private class QuitterAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}
