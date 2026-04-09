package ihm;

import data.unites.Faction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
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
        titre.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel pFactions = new JPanel(new GridLayout(3, 1, 10, 10));
        pFactions.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

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
        p.setBorder(BorderFactory.createEtchedBorder());

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

            SpinnerNumberModel model = new SpinnerNumberModel(
                50, 10,
                Math.max(10, moteur.getFactionJoueur().getOr()), 10);
            JSpinner spinner = new JSpinner(model);

            JButton btnTransfert = new JButton("Envoyer Or");
            btnTransfert.addActionListener(new TransfertAction(f, spinner));

            pBoutons.add(btnTrahir);
            pBoutons.add(spinner);
            pBoutons.add(btnTransfert);
        }

        p.add(lblNom, BorderLayout.CENTER);
        p.add(pBoutons, BorderLayout.EAST);
        return p;
    }





    private class ProposeAllianceAction implements ActionListener {
        private Faction cible;

        public ProposeAllianceAction(Faction cible) {
            this.cible = cible;
        }

        public void actionPerformed(ActionEvent e) {
            moteur.proposerAllianceJoueur(cible);
            JOptionPane.showMessageDialog(FenetreDiplomatie.this,
                "Alliance proposee a " + cible.getNom() + " !\n"
                + "Droit de passage et vision partages actives.",
                "Alliance", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private class TrahirAction implements ActionListener {
        private Faction cible;

        public TrahirAction(Faction cible) {
            this.cible = cible;
        }

        public void actionPerformed(ActionEvent e) {
            int rep = JOptionPane.showConfirmDialog(FenetreDiplomatie.this,
                "Trahir " + cible.getNom() + " ? L'etat de guerre reprend immediatement.",
                "Trahison", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (rep == JOptionPane.YES_OPTION) {
                moteur.trahir(cible);
                dispose();
            }
        }
    }

    private class TransfertAction implements ActionListener {
        private Faction cible;
        private JSpinner spinner;

        public TransfertAction(Faction cible, JSpinner spinner) {
            this.cible = cible;
            this.spinner = spinner;
        }

        public void actionPerformed(ActionEvent e) {
            int montant = (Integer) spinner.getValue();
            String log = moteur.getDiplomatieManager().transfererRessources(
                moteur.getFactionJoueur(), cible, montant);
            JOptionPane.showMessageDialog(FenetreDiplomatie.this, log,
                "Transfert", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    private class FermerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }
}
