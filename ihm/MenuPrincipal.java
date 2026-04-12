package ihm;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import config.Config;
import process.MoteurJeu;
import process.MoteurInterface;

public class MenuPrincipal extends JFrame implements Runnable {

    private MoteurInterface moteur;
    private JCheckBox checkBrouillard;

    public MenuPrincipal(MoteurInterface moteur) {
        this.moteur = moteur;
        this.setTitle("ABAT-TERRA - Menu Principal");
        this.setSize(400, 340);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new GridLayout(5, 1, 10, 10));

        JLabel titre = new JLabel("ABAT-TERRA", SwingConstants.CENTER);
        titre.setFont(new Font("Serif", Font.BOLD, 28));

        JPanel pPseudo = new JPanel();
        pPseudo.add(new JLabel("Pseudo : "));
        pPseudo.add(new JTextField(15));

        JPanel pFog = new JPanel();
        checkBrouillard = new JCheckBox("Brouillard de guerre actif", Config.BROUILLARD_INITIAL);
        checkBrouillard.setFont(new Font("Arial", Font.PLAIN, 14));
        pFog.add(checkBrouillard);

        JButton btnStart = new JButton("COMMENCER LA PARTIE");
        btnStart.addActionListener(new DemarrerPartieAction());

        JButton btnQuitter = new JButton("QUITTER");
        btnQuitter.addActionListener(new QuitterAction());

        this.add(titre);
        this.add(pPseudo);
        this.add(pFog);
        this.add(btnStart);
        this.add(btnQuitter);

        this.setVisible(true);
    }

    public void run() {
    }

    private class DemarrerPartieAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Config.BROUILLARD_INITIAL = checkBrouillard.isSelected();
            new FenetreJeu(moteur);
            dispose();
        }
    }

    private class QuitterAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}