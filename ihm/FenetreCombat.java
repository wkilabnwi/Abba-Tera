package ihm;

import data.architecture.Case;
import data.unites.Unite;
import process.CombatManager;
import process.MoteurJeu;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FenetreCombat extends JDialog {

    private Unite attaquant;
    private Unite defenseur;
    private Case caseCible;
    private CombatManager combatManager;
    private MoteurJeu moteur;

    private JLabel lblStatut;
    private JProgressBar barreAttaquant;
    private JProgressBar barreDefenseur;
    private JPanel pActions;
    private boolean combatTermine = false;

    public FenetreCombat(JFrame parent, Unite attaquant, Unite defenseur,
                         Case caseCible, CombatManager cm, MoteurJeu moteur) {
        super(parent, "Combat Tactique", true);
        this.attaquant     = attaquant;
        this.defenseur     = defenseur;
        this.caseCible     = caseCible;
        this.combatManager = cm;
        this.moteur        = moteur;

        setSize(520, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(20, 20, 30));

        JPanel pHeader = new JPanel(new GridLayout(5, 1, 3, 3));
        pHeader.setBackground(new Color(30, 30, 45));
        pHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        lblStatut = new JLabel("Choisissez votre action !", SwingConstants.CENTER);
        lblStatut.setForeground(Color.YELLOW);
        lblStatut.setFont(new Font("Monospaced", Font.BOLD, 14));

        barreAttaquant = creerBarre(attaquant, new Color(60, 120, 220));
        barreDefenseur = creerBarre(defenseur, new Color(200, 60, 60));

        JLabel lblA = makeLabel(
            attaquant.getCamp() + " — " + attaquant.getType() +
            "  Niv." + attaquant.getNiveau() +
            "  Force:" + attaquant.getForce() +
            "  PM:" + attaquant.getPointsDeplacement(), Color.CYAN);

        JLabel lblD = makeLabel(
            defenseur.getCamp() + " — " + defenseur.getType() +
            "  Niv." + defenseur.getNiveau() +
            "  Force:" + defenseur.getForce() +
            "  Terrain:+" + caseCible.getBonusDefense(), Color.ORANGE);

        pHeader.add(lblStatut);
        pHeader.add(lblA);
        pHeader.add(barreAttaquant);
        pHeader.add(lblD);
        pHeader.add(barreDefenseur);

        pActions = new JPanel(new GridLayout(1, 3, 10, 0));
        pActions.setBackground(new Color(20, 20, 30));
        pActions.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        JButton btnAttaque  = makeButton("Attaquer",           new Color(160, 40, 40));
        JButton btnGarde    = makeButton("Se Mettre en Garde", new Color(40, 100, 40));
        JButton btnRetraite = makeButton("Retraite",           new Color(80, 80, 80));

        btnAttaque.addActionListener(new AttaquerAction());
        btnGarde.addActionListener(new GardeAction());
        btnRetraite.addActionListener(new RetraiteAction());

        pActions.add(btnAttaque);
        pActions.add(btnGarde);
        pActions.add(btnRetraite);

        add(pHeader, BorderLayout.CENTER);
        add(pActions, BorderLayout.SOUTH);

        actualiserBarres();
    }

    private JButton makeButton(String texte, Color bg) {
        JButton b = new JButton(texte);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setFocusPainted(false);
        return b;
    }

    private JProgressBar creerBarre(Unite u, Color c) {
        JProgressBar b = new JProgressBar(0, u.getPvMax());
        b.setValue(u.getPv());
        b.setStringPainted(true);
        b.setForeground(c);
        b.setBackground(new Color(40, 40, 50));
        return b;
    }

    private JLabel makeLabel(String t, Color c) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setForeground(c);
        l.setFont(new Font("Monospaced", Font.BOLD, 11));
        return l;
    }

    private void actualiserBarres() {
        barreAttaquant.setMaximum(attaquant.getPvMax());
        barreAttaquant.setValue(attaquant.getPv());
        barreAttaquant.setString(attaquant.getType() + " : " + attaquant.getPv() + "/" + attaquant.getPvMax() + " PV  XP:" + attaquant.getXP());
        barreDefenseur.setMaximum(defenseur.getPvMax());
        barreDefenseur.setValue(defenseur.getPv());
        barreDefenseur.setString(defenseur.getType() + " : " + defenseur.getPv() + "/" + defenseur.getPvMax() + " PV");
    }

    private void verifierFinCombat() {
        if (defenseur.estMort()) {
            lblStatut.setText("Victoire ! " + attaquant.getType() + " a gagne !");
            lblStatut.setForeground(new Color(100, 255, 100));
            pActions.setVisible(false);
            combatTermine = true;
            planifierFermeture();
            return;
        }
        if (attaquant.estMort()) {
            lblStatut.setText("Defaite ! " + defenseur.getType() + " a resiste !");
            lblStatut.setForeground(new Color(255, 80, 80));
            pActions.setVisible(false);
            combatTermine = true;
            planifierFermeture();
        }
    }

    private void planifierFermeture() {
        Timer t = new Timer(1800, new FermetureAction());
        t.setRepeats(false);
        t.start();
    }

    private class FermetureAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    private class AttaquerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (combatTermine) return;
            String log = combatManager.executerCombat(attaquant, defenseur, caseCible);
            lblStatut.setText(log);
            lblStatut.setForeground(Color.YELLOW);
            actualiserBarres();
            verifierFinCombat();
        }
    }

    private class GardeAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (combatTermine) return;
            attaquant.consommerDeplacement(attaquant.getPointsDeplacementMax());
            lblStatut.setText(attaquant.getType() + " se met en garde !");
            lblStatut.setForeground(new Color(100, 200, 255));
            pActions.setVisible(false);
            combatTermine = true;
            planifierFermeture();
        }
    }

    private class RetraiteAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            attaquant.consommerDeplacement(attaquant.getPointsDeplacementMax());
            lblStatut.setText("Retraite !");
            lblStatut.setForeground(Color.GRAY);
            combatTermine = true;
            planifierFermeture();
        }
    }
}