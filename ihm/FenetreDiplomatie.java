package ihm;

import data.unites.Faction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import process.MoteurJeu;

public class FenetreDiplomatie extends JDialog {

    private MoteurJeu moteur;

    public FenetreDiplomatie(JFrame parent, MoteurJeu moteur) {
        super(parent, "Diplomatie", true);
        this.moteur = moteur;
        this.setSize(500, 400);
        this.setLocationRelativeTo(parent);
        this.setLayout(new BorderLayout());

        JLabel titre = new JLabel("Relations diplomatiques", SwingConstants.CENTER);
        titre.setFont(new Font("Serif", Font.BOLD, 18));

        JPanel pFactions = new JPanel(new GridLayout(3, 1, 10, 10));

        List<Faction> factions = moteur.getFactions();
        for (Faction f : factions) {
            if (f.getNom().equals("JOUEUR")) continue;
            if (!f.isEliminee()) {
                pFactions.add(creerLigneFaction(f));
            } else {
                JLabel eliminee = new JLabel(f.getNom() + " — ELIMINEE");
                eliminee.setForeground(Color.GRAY);
                pFactions.add(eliminee);
            }
        }

        JButton btnFermer = new JButton("Fermer");
        btnFermer.addActionListener(new FermerAction());

        this.add(titre, BorderLayout.NORTH);
        this.add(pFactions, BorderLayout.CENTER);
        this.add(btnFermer, BorderLayout.SOUTH);
    }

    private JPanel creerLigneFaction(Faction f) {
        JPanel p = new JPanel(new BorderLayout(10, 0));

        boolean allie = moteur.getDiplomatieManager()
            .sontAllies("JOUEUR", f.getNom());

        String statut = allie ? "[ALLIE]" : "[ENNEMI]";
        Color couleur = allie ? new Color(0, 140, 0) : Color.RED;

        JLabel lblNom = new JLabel(f.getNom() + "  " + statut + "  | Or: " + f.getOr());
        lblNom.setForeground(couleur);
        lblNom.setFont(new Font("Monospaced", Font.BOLD, 13));

        JPanel pBoutons = new JPanel(new GridLayout(1, 3, 5, 0));

        if (!allie) {
            JButton btnAlliance = new JButton("Proposer alliance");
            btnAlliance.addActionListener(new ProposeAllianceAction(f));
            pBoutons.add(btnAlliance);
            pBoutons.add(new JLabel(""));
            pBoutons.add(new JLabel(""));
        } else {
            JButton btnTrahir = new JButton("Trahir");
            btnTrahir.setForeground(Color.RED);
            btnTrahir.addActionListener(new TrahirAction(f));

            JTextField champMontant = new JTextField("50");
            champMontant.setFont(new Font("Arial", Font.PLAIN, 12));

            JButton btnTransfert = new JButton("Envoyer Or");
            btnTransfert.addActionListener(new TransfertAction(f, champMontant));

            pBoutons.add(btnTrahir);
            pBoutons.add(champMontant);
            pBoutons.add(btnTransfert);
        }

        p.add(lblNom, BorderLayout.CENTER);
        p.add(pBoutons, BorderLayout.EAST);
        return p;
    }

    private void afficherMessage(String titre, String message) {
        JDialog d = new JDialog(this, titre, true);
        d.setSize(400, 130);
        d.setLocationRelativeTo(this);
        d.setLayout(new GridLayout(2, 1, 5, 5));
        JLabel lbl = new JLabel(message, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        JButton btn = new JButton("OK");
        btn.addActionListener(new FermerDialogueAction(d));
        d.add(lbl);
        d.add(btn);
        d.setVisible(true);
    }

    private int afficherConfirmation(String titre, String message) {
        JDialog d = new JDialog(this, titre, true);
        d.setSize(420, 130);
        d.setLocationRelativeTo(this);
        d.setLayout(new GridLayout(2, 1, 5, 5));
        JLabel lbl = new JLabel(message, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        JPanel pBtns = new JPanel(new GridLayout(1, 2, 10, 0));
        final int[] resultat = {1};
        JButton btnOui = new JButton("Oui");
        JButton btnNon = new JButton("Non");
        btnOui.addActionListener(new OuiAction(resultat, d));
        btnNon.addActionListener(new FermerDialogueAction(d));
        pBtns.add(btnOui);
        pBtns.add(btnNon);
        d.add(lbl);
        d.add(pBtns);
        d.setVisible(true);
        return resultat[0];
    }

    private class FermerDialogueAction implements ActionListener {
        private JDialog dialog;
        public FermerDialogueAction(JDialog dialog) { this.dialog = dialog; }
        public void actionPerformed(ActionEvent e) { dialog.dispose(); }
    }

    private class OuiAction implements ActionListener {
        private int[] resultat;
        private JDialog dialog;
        public OuiAction(int[] resultat, JDialog dialog) {
            this.resultat = resultat;
            this.dialog = dialog;
        }
        public void actionPerformed(ActionEvent e) {
            resultat[0] = 0;
            dialog.dispose();
        }
    }

    private class ProposeAllianceAction implements ActionListener {
        private Faction cible;
        public ProposeAllianceAction(Faction cible) { this.cible = cible; }
        public void actionPerformed(ActionEvent e) {
            moteur.proposerAllianceJoueur(cible);
            afficherMessage("Alliance",
                "Alliance proposee a " + cible.getNom() + " ! Vision partagee activee.");
            dispose();
        }
    }

    private class TrahirAction implements ActionListener {
        private Faction cible;
        public TrahirAction(Faction cible) { this.cible = cible; }
        public void actionPerformed(ActionEvent e) {
            int rep = afficherConfirmation("Trahison",
                "Trahir " + cible.getNom() + " ? L'etat de guerre reprend.");
            if (rep == 0) {
                moteur.trahir(cible);
                dispose();
            }
        }
    }

    private class TransfertAction implements ActionListener {
        private Faction cible;
        private JTextField champMontant;
        public TransfertAction(Faction cible, JTextField champMontant) {
            this.cible = cible;
            this.champMontant = champMontant;
        }
        public void actionPerformed(ActionEvent e) {
            int montant = 50;
            try {
                montant = Integer.parseInt(champMontant.getText().trim());
            } catch (NumberFormatException ex) {
                montant = 50;
            }
            String log = moteur.getDiplomatieManager().transfererRessources(
                moteur.getFactionJoueur(), cible, montant);
            afficherMessage("Transfert", log);
            dispose();
        }
    }

    private class FermerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) { dispose(); }
    }
}