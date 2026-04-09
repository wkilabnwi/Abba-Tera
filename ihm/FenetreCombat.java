package ihm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import data.unites.Faction;
import process.CombatManager;
import process.MoteurJeu;

public class FenetreCombat extends JDialog {

    private CombatManager combatManager;
    private Faction fJoueur;
    private Faction fEnnemie;
    private MoteurJeu moteur;

    private JLabel lblStatut;
    private JProgressBar barreJoueur;
    private JProgressBar barreEnnemi;
    private JPanel pActions;

    public FenetreCombat(JFrame parent, Faction joueur, Faction ennemi,
                         CombatManager cm, MoteurJeu moteur) {
        super(parent, "Champ de Bataille", true);
        this.fJoueur = joueur;
        this.fEnnemie = ennemi;
        this.combatManager = cm;
        this.moteur = moteur;

        this.setSize(550, 450);
        this.setLocationRelativeTo(parent);
        this.setLayout(new BorderLayout());


        JPanel pHeader = new JPanel(new GridLayout(5, 1, 5, 5));

        lblStatut = new JLabel("Choisissez votre attaque !", SwingConstants.CENTER);
        lblStatut.setFont(new Font("Monospaced", Font.BOLD, 16));

        barreJoueur = new JProgressBar(0, Math.max(1, fJoueur.calculerEnduranceTotale()));
        barreJoueur.setStringPainted(true);
        barreJoueur.setForeground(new Color(39, 174, 96));

        barreEnnemi = new JProgressBar(0, Math.max(1, fEnnemie.calculerEnduranceTotale()));
        barreEnnemi.setStringPainted(true);
        barreEnnemi.setForeground(new Color(192, 57, 43));

        pHeader.add(lblStatut);
        pHeader.add(new JLabel("VOTRE ARMÉE :"));
        pHeader.add(barreJoueur);
        pHeader.add(new JLabel("ARMÉE ENNEMIE :"));
        pHeader.add(barreEnnemi);


        pActions = new JPanel(new GridLayout(2, 2, 10, 10));
        construireBoutonsAttaque();

        JButton btnFuir = new JButton("Repli Tactique");
        btnFuir.addActionListener(new FuirAction());

        this.add(pHeader, BorderLayout.NORTH);
        this.add(pActions, BorderLayout.CENTER);
        this.add(btnFuir, BorderLayout.SOUTH);

        actualiserAffichage();
    }

    private void construireBoutonsAttaque() {
        pActions.removeAll();

        int nbS = moteur.getPuissanceTotale("JOUEUR", "Soldat");
        int nbA = moteur.getPuissanceTotale("JOUEUR", "Archer");
        int nbC = moteur.getPuissanceTotale("JOUEUR", "Chevalier");

        JButton btnSoldat = new JButton("Assaut Soldat (" + nbS + ")");
        btnSoldat.setEnabled(nbS > 0);
        btnSoldat.addActionListener(new AttaquerAction("Soldat"));

        JButton btnArcher = new JButton("Tir Archer (" + nbA + ")");
        btnArcher.setEnabled(nbA > 0);
        btnArcher.addActionListener(new AttaquerAction("Archer"));

        JButton btnChevalier = new JButton("Charge Chevalier (" + nbC + ")");
        btnChevalier.setEnabled(nbC > 0);
        btnChevalier.addActionListener(new AttaquerAction("Chevalier"));

        JButton btnGarde = new JButton("Position Défensive");
        btnGarde.addActionListener(new GardeAction());

        pActions.add(btnSoldat);
        pActions.add(btnArcher);
        pActions.add(btnChevalier);
        pActions.add(btnGarde);
        pActions.revalidate();
    }

    private void executerTour(String typeChoisi, boolean defenseActive) {
        if (!defenseActive) {
            int degats = calculerDegats(typeChoisi);
            fEnnemie.subirDegats(degats);
            lblStatut.setText("Vos " + typeChoisi + "s infligent " + degats + " dégâts !");
        } else {
            lblStatut.setText("Vous renforcez vos lignes !");
        }

        actualiserAffichage();

        if (fEnnemie.calculerEnduranceTotale() <= 0) {
            JOptionPane.showMessageDialog(this, "VICTOIRE ! L'ennemi est décimé.");
            dispose();
            return;
        }


        int degatsEnnemi = combatManager.calculerDegatsFaction(fEnnemie, fJoueur);
        if (defenseActive) {
            degatsEnnemi /= 2;
        }
        fJoueur.subirDegats(degatsEnnemi);
        lblStatut.setText("L'ennemi contre-attaque : -" + degatsEnnemi + " PV !");

        construireBoutonsAttaque();
        actualiserAffichage();

        if (fJoueur.calculerEnduranceTotale() <= 0) {
            JOptionPane.showMessageDialog(this, "DÉFAITE... Votre armée a péri.");
            dispose();
        }
    }

    private int calculerDegats(String type) {
        int nb = moteur.getPuissanceTotale("JOUEUR", type);
        if (type.equals("Soldat"))    return nb * 3;
        if (type.equals("Archer"))    return nb * 5;
        if (type.equals("Chevalier")) return nb * 10;
        return 0;
    }

    private void actualiserAffichage() {
        int ej = fJoueur.calculerEnduranceTotale();
        int ee = fEnnemie.calculerEnduranceTotale();

        if (ej > barreJoueur.getMaximum()) barreJoueur.setMaximum(ej);
        if (ee > barreEnnemi.getMaximum()) barreEnnemi.setMaximum(ee);

        barreJoueur.setValue(ej);
        barreJoueur.setString("Armée Joueur : " + ej + " PV");
        barreEnnemi.setValue(ee);
        barreEnnemi.setString("Défense Ennemie : " + ee + " PV");
    }



    private class AttaquerAction implements ActionListener {
        private String type;

        public AttaquerAction(String type) {
            this.type = type;
        }

        public void actionPerformed(ActionEvent e) {
            executerTour(type, false);
        }
    }

    private class GardeAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            executerTour("Defense", true);
        }
    }

    private class FuirAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }
}
