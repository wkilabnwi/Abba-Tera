package ihm;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import data.unites.Faction;
import process.MoteurJeu;

public class FenetreArmee extends JDialog {

    private MoteurJeu moteur;

    public FenetreArmee(JFrame parent, MoteurJeu moteur) {
        super(parent, "Gestion de l'Armée", true);
        this.moteur = moteur;
        this.setSize(400, 300);
        this.setLocationRelativeTo(parent);
        this.setLayout(new BorderLayout());

        Faction f = moteur.getFactionJoueur();

        JPanel pTroupes = new JPanel(new GridLayout(3, 1, 10, 10));
        pTroupes.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        pTroupes.add(creerLigneTroupe("Soldat",    f.getNbSoldats(),    3));
        pTroupes.add(creerLigneTroupe("Archer",    f.getNbArchers(),    5));
        pTroupes.add(creerLigneTroupe("Chevalier", f.getNbChevaliers(), 7));

        JLabel info = new JLabel(
            "Sélectionnez une unité puis cliquez sur la carte pour la déployer.",
            SwingConstants.CENTER);

        JButton btnFermer = new JButton("Retour");
        btnFermer.addActionListener(new FermerAction());

        this.add(info, BorderLayout.NORTH);
        this.add(pTroupes, BorderLayout.CENTER);
        this.add(btnFermer, BorderLayout.SOUTH);
    }

    private JPanel creerLigneTroupe(String nom, int quantite, int atk) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel info = new JLabel(nom + " [Atk: " + atk + "] x" + quantite);
        JButton btnDeployer = new JButton("DÉPLOYER");
        btnDeployer.setEnabled(quantite > 0);
        btnDeployer.addActionListener(new DeployerAction(nom));
        p.add(info, BorderLayout.CENTER);
        p.add(btnDeployer, BorderLayout.EAST);
        return p;
    }

    private class DeployerAction implements ActionListener {
        private String nom;

        public DeployerAction(String nom) {
            this.nom = nom;
        }

        public void actionPerformed(ActionEvent e) {
            moteur.setUniteSelectionnee(nom);
            dispose();
        }
    }

    private class FermerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }
}
