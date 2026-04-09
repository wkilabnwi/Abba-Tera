package ihm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import data.architecture.QG;
import process.MoteurJeu;

public class FenetreAchat extends JDialog {

    private MoteurJeu moteur;

    public FenetreAchat(JFrame parent, MoteurJeu moteur, String typeBatiment) {
        super(parent, "Achat - " + typeBatiment, true);
        this.moteur = moteur;
        this.setSize(600, 400);
        this.setLocationRelativeTo(parent);
        this.setLayout(new BorderLayout());


        JPanel pOptions = new JPanel(new GridLayout(2, 3, 15, 15));
        pOptions.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (typeBatiment.equals("QG")) {
            pOptions.add(creerBouton("Caserne", 150, "res/Caserne.png", true));
            pOptions.add(creerBouton("Ferme", 120, "res/Ferme.png", true));

            pOptions.add(creerBouton("Colon", 500, "res/Colon.png", false)); 
        } else if (typeBatiment.equals("Caserne")) {
            pOptions.add(creerBouton("Soldat", 40, "res/Soldat.png", false));
            pOptions.add(creerBouton("Archer", 60, "res/Archer.png", false));
            pOptions.add(creerBouton("Chevalier", 90, "res/Chevalier.png", false));
        }

        JButton btnFermer = new JButton("Fermer");
        btnFermer.addActionListener(new FermerAction());

        this.add(pOptions, BorderLayout.CENTER);
        this.add(btnFermer, BorderLayout.SOUTH);
    }

    private JButton creerBouton(String nom, int prix, String pathImg, boolean estBatiment) {
        Image img = lireImage(pathImg);
        JButton btn = new JButton();

        if (img != null) {

            Image scaled = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaled));
        }

        btn.setText(nom + " (" + prix + " Or)");
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(120, 120));
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEtchedBorder());
        btn.addActionListener(new AchatAction(nom, prix, estBatiment));

        return btn;
    }

    private class AchatAction implements ActionListener {
        private String nom;
        private int prix;
        private boolean estBatiment;

        public AchatAction(String nom, int prix, boolean estBatiment) {
            this.nom = nom;
            this.prix = prix;
            this.estBatiment = estBatiment;
        }

        public void actionPerformed(ActionEvent e) {
            if (moteur.getFactionJoueur().getOr() >= prix) {
                if (estBatiment) {
                    moteur.getFactionJoueur().retirerOr(prix);
                    moteur.preparerConstruction(nom);
                    dispose();
                } else {

                    QG qg = moteur.getFactionJoueur().getQG();
                    if (qg != null) {
                        moteur.getFactionJoueur().retirerOr(prix);

                        int tempsProduction = nom.equals("Colon") ? 5 : 2;
                        qg.demarrerProduction(nom, tempsProduction);
                        moteur.setDernierMouvement("Production de " + nom + " lancée !");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(FenetreAchat.this, "Il vous faut un QG pour produire des unités !");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(FenetreAchat.this, "Or insuffisant !");
            }
        }
    }

    private class FermerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    public static Image lireImage(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) return null;
            return ImageIO.read(f);
        } catch (IOException e) {
            return null;
        }
    }
}