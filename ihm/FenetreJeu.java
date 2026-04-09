package ihm;

import config.Config;
import data.architecture.Batiment;
import data.architecture.Caserne;
import data.architecture.QG;
import data.unites.Faction;
import data.unites.Unite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import process.MoteurJeu;

public class FenetreJeu extends JFrame {

    private MoteurJeu moteur;
    private PanneauJeu panneau;
    private LogPanel logs;
    private FenetreGestion gestion;
    private JLabel lblInfo;

    public FenetreJeu(MoteurJeu moteurRecu) {
        this.moteur = moteurRecu;
        this.setUndecorated(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.panneau = new PanneauJeu(this.moteur);
        this.logs    = new LogPanel();
        this.gestion = new FenetreGestion();

        this.logs.setPreferredSize(new java.awt.Dimension(Config.LARGEUR_ECRAN, 50));

        Timer timerEco = new Timer(15000, new TimerEcoAction());
        timerEco.start();


        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(25, 25, 25));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        lblInfo = new JLabel("", SwingConstants.LEFT);
        lblInfo.setForeground(Color.YELLOW);
        lblInfo.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lblControls = new JLabel(
            "ESC=Pause  A=Armee  Q=QG  C=Caserne  D=Diplomatie  ENTER=Fin de tour  "
            + "Clic=Unite  Clic Droit=Assaut",
            SwingConstants.RIGHT);
        lblControls.setForeground(Color.LIGHT_GRAY);
        lblControls.setFont(new Font("Arial", Font.PLAIN, 11));

        infoPanel.add(lblInfo, BorderLayout.WEST);
        infoPanel.add(lblControls, BorderLayout.EAST);

        this.setTitle("Abat-Terra - Conquete");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(infoPanel, BorderLayout.NORTH);
        this.add(this.panneau, BorderLayout.CENTER);
        this.add(this.logs, BorderLayout.SOUTH);

        this.addKeyListener(new ClavierJeu());
        this.panneau.addMouseListener(new SourisJeu());

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setFocusable(true);
        this.requestFocusInWindow();

        rafraichir();
    }

    public void rafraichir() {
        Faction f = moteur.getFactionJoueur();
        if (f == null) return;


        StringBuilder sb = new StringBuilder();
        sb.append("  OR: ").append(f.getOr());
        sb.append("  | S:").append(f.getNbSoldats());
        sb.append(" A:").append(f.getNbArchers());
        sb.append(" C:").append(f.getNbChevaliers());

        for (Faction faction : moteur.getFactions()) {
            if (faction.getNom().equals("JOUEUR")) continue;
            sb.append("  | ").append(faction.getNom());
            if (faction.isEliminee()) {
                sb.append("[X]");
            } else if (moteur.getDiplomatieManager().sontAllies("JOUEUR", faction.getNom())) {
                sb.append("[ALLIE]");
            } else {
                sb.append("[ENNEMI]");
            }
        }
        lblInfo.setText(sb.toString());

        String msg = moteur.getDernierMouvement();
        if (msg != null && !msg.isEmpty()) {
            logs.ajouterLog(msg);
        }

        panneau.repaint();


        traiterPropositionsAlliance();


        if (moteur.isPartieTerminee()) {
            JOptionPane.showMessageDialog(this,
                moteur.getMessageFinPartie(), "Fin de partie",
                JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }

    private void traiterPropositionsAlliance() {
        List<Faction> propositions = new ArrayList<Faction>(moteur.getPropositionsAlliance());
        for (Faction ia : propositions) {
            int rep = JOptionPane.showConfirmDialog(this,
                ia.getNom() + " vous propose une alliance.\n"
                + "Accepter ? (Droit de passage + vision + transferts actives)",
                "Proposition d'alliance", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if (rep == JOptionPane.YES_OPTION) {
                moteur.accepterAlliance(ia);
            } else {
                moteur.refuserAlliance(ia);
            }
        }
    }





    private class TimerEcoAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            rafraichir();
        }
    }

    private class ClavierJeu extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();
            Unite sel = moteur.getUniteSelectionneeSurMap();

            if (sel != null && sel.getCamp().equals("JOUEUR")) {
                int nouvelleLig = sel.getLigne();
                int nouvelleCol = sel.getColonne();
                boolean moveAttempted = false;
               
                if (code == KeyEvent.VK_UP) {
                    nouvelleLig--;
                    moveAttempted = true;
                } else if (code == KeyEvent.VK_DOWN) {
                    nouvelleLig++;
                    moveAttempted = true;
                } else if (code == KeyEvent.VK_LEFT) {
                    nouvelleCol--;
                    moveAttempted = true;
                } else if (code == KeyEvent.VK_RIGHT) {
                    nouvelleCol++;
                    moveAttempted = true;
                }

                if (moveAttempted) {

                    moteur.deplacerUniteSelectionnee(nouvelleLig, nouvelleCol);
                    

                    if (!sel.canMove()) {
                        moteur.cycleUniteSuivante();
                    }
                }
            }

            if (code == KeyEvent.VK_ESCAPE) {
                gestion.setVisible(true);
            }

            if (code == KeyEvent.VK_A) {
                new FenetreArmee(FenetreJeu.this, moteur).setVisible(true);
            }

            if (code == KeyEvent.VK_D) {
                new FenetreDiplomatie(FenetreJeu.this, moteur).setVisible(true);
            }

            if (code == KeyEvent.VK_Q) {
                boolean trouve = false;
                for (Batiment b : moteur.getBatiments()) {
                    if (b instanceof QG && b.getProprietaire().equals("JOUEUR")) {
                        new FenetreAchat(FenetreJeu.this, moteur, "QG").setVisible(true);
                        trouve = true;
                        break;
                    }
                }
                if (!trouve) moteur.setDernierMouvement("Vous n'avez pas de QG !");
            }

            if (code == KeyEvent.VK_C) {
                boolean trouve = false;
                for (Batiment b : moteur.getBatiments()) {
                    if (b instanceof Caserne && b.getProprietaire().equals("JOUEUR")) {
                        new FenetreAchat(FenetreJeu.this, moteur, "Caserne").setVisible(true);
                        trouve = true;
                        break;
                    }
                }
                if (!trouve) moteur.setDernierMouvement("Construisez d'abord une Caserne !");
            }
             if (code == KeyEvent.VK_S) {
                    moteur.fonderVille();
                    rafraichir();
                }

            if (code == KeyEvent.VK_ENTER) {
                moteur.passerTour();
            }

            requestFocusInWindow();
            rafraichir();
        }
    }

    private class SourisJeu extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            int col = e.getX() / Config.TAILLE_CASE;
            int lig = e.getY() / Config.TAILLE_CASE;

             if (SwingUtilities.isRightMouseButton(e)) {
                Unite u = moteur.getUniteAt(lig, col);
                if (u != null && "JOUEUR".equals(u.getCamp())) {
                    moteur.ajouterUniteAuCombat(u);
                }
            } else {
                if (moteur.aUniteEnMain()) {
                    moteur.deplacerSoldatInventaire(lig, col);

                } else if (moteur.estEnModeConstruction()) {
                    moteur.placerBatiment(lig, col);

                } else if (moteur.getUniteSelectionneeSurMap() != null) {
                    moteur.deplacerUniteSelectionnee(lig, col);
                    moteur.cycleUniteSuivante();

                } else {
                    Unite u = moteur.getUniteAt(lig, col);
                    Batiment b = moteur.getBatimentAt(lig, col);

                    if (u != null && "JOUEUR".equals(u.getCamp())) {
                        moteur.setUniteSelectionneeSurMap(u);
                        moteur.setDernierMouvement(
                            u.getType() + " selectionne. Cliquez pour deplacer.");

                    } else if (u != null && !"JOUEUR".equals(u.getCamp())) {
                        if (!moteur.getArmeeSelectionnee().isEmpty()) {
                            moteur.lancerAssautGroupe(lig, col);
                        } else {
                            moteur.setDernierMouvement(
                                "Clic droit sur vos unites pour preparer l'assaut !");
                        }

                    } else if (b != null && "JOUEUR".equals(b.getProprietaire())) {
                        if (b instanceof QG) {
                            new FenetreAchat(FenetreJeu.this, moteur, "QG").setVisible(true);
                        } else if (b instanceof Caserne) {
                            new FenetreAchat(FenetreJeu.this, moteur, "Caserne").setVisible(true);
                        }

                    } else if (b != null && !"JOUEUR".equals(b.getProprietaire())) {
                        if (!moteur.getArmeeSelectionnee().isEmpty()) {
                            moteur.lancerAssautGroupe(lig, col);
                        } else {
                            moteur.setDernierMouvement(
                                "Preparez un assaut (clic droit sur vos unites) !");
                        }
                    }
                }
            }

            requestFocusInWindow();
            rafraichir();
        }
    }
}
