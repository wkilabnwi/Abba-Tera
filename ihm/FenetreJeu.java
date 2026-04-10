package ihm;

import config.Config;
import data.architecture.Batiment;
import data.architecture.QG;
import data.unites.Faction;
import data.unites.Unite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
import process.MoteurJeu;

public class FenetreJeu extends JFrame {

    private MoteurJeu moteur;
    private PanneauJeu panneau;
    private LogPanel logs;
    private FenetreGestion gestion;
    private JLabel lblInfo;
    private JLabel lblTour;
    private JLabel lblUnite;

    public FenetreJeu(MoteurJeu moteurRecu) {
        this.moteur = moteurRecu;
        this.setUndecorated(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);

        this.panneau = new PanneauJeu(this.moteur);
        this.logs    = new LogPanel();
        this.gestion = new FenetreGestion();

        this.logs.setPreferredSize(new java.awt.Dimension(Config.LARGEUR_ECRAN, 50));

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(25, 25, 25));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        lblInfo = new JLabel("", SwingConstants.LEFT);
        lblInfo.setForeground(Color.YELLOW);
        lblInfo.setFont(new Font("Arial", Font.BOLD, 13));

        lblTour = new JLabel("Tour 1", SwingConstants.CENTER);
        lblTour.setForeground(Color.CYAN);
        lblTour.setFont(new Font("Arial", Font.BOLD, 13));

        lblUnite = new JLabel("", SwingConstants.RIGHT);
        lblUnite.setForeground(new Color(180, 255, 180));
        lblUnite.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel lblControls = new JLabel(
            "Fleches=Deplacer  ESPACE=Passer  ENTREE=Fin tour  D=Diplo  Q=Cite  S=Fonder  F=Brouillard  ESC=Pause",
            SwingConstants.RIGHT);
        lblControls.setForeground(Color.DARK_GRAY);
        lblControls.setFont(new Font("Arial", Font.PLAIN, 10));

        JPanel pRight = new JPanel(new BorderLayout());
        pRight.setOpaque(false);
        pRight.add(lblUnite, BorderLayout.NORTH);
        pRight.add(lblControls, BorderLayout.SOUTH);

        infoPanel.add(lblInfo, BorderLayout.WEST);
        infoPanel.add(lblTour, BorderLayout.CENTER);
        infoPanel.add(pRight, BorderLayout.EAST);

        this.setTitle("Abat-Terra");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(infoPanel, BorderLayout.NORTH);
        this.add(this.panneau, BorderLayout.CENTER);
        this.add(this.logs, BorderLayout.SOUTH);

        this.addKeyListener(new ClavierJeu());
        this.panneau.addMouseListener(new SourisJeu());

        this.setLocationRelativeTo(null);
        this.setFocusable(true);
        this.setVisible(true);
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
        sb.append("  | Bonheur:").append(f.calculerBonheur());

        for (Faction faction : moteur.getFactions()) {
            if (faction.getNom().equals("JOUEUR")) continue;
            sb.append("  | ").append(faction.getNom());
            if (faction.isEliminee()) sb.append("[X]");
            else if (moteur.getDiplomatieManager().sontAllies("JOUEUR", faction.getNom())) sb.append("[ALLIE]");
            else sb.append("[ENNEMI]");
        }

        lblInfo.setText(sb.toString());
        lblTour.setText("Tour " + moteur.getTourActuel());

        Unite sel = moteur.getUniteSelectionneeSurMap();
        if (sel != null) {
            lblUnite.setText(sel.getType() + " Niv." + sel.getNiveau() +
                " PV:" + sel.getPv() + "/" + sel.getPvMax() +
                " XP:" + sel.getXP() +
                " PM:" + sel.getPointsDeplacement());
        } else {
            lblUnite.setText("Aucune unite selectionnee");
        }

        String msg = moteur.getDernierMouvement();
        if (msg != null && !msg.isEmpty()) logs.ajouterLog(msg);

        panneau.repaint();
        traiterPropositionsAlliance();

        if (moteur.isPartieTerminee()) {
            JOptionPane.showMessageDialog(this, moteur.getMessageFinPartie());
            System.exit(0);
        }
    }

    private void traiterPropositionsAlliance() {
        List<Faction> propositions = new ArrayList<Faction>(moteur.getPropositionsAlliance());
        for (Faction ia : propositions) {
            int rep = JOptionPane.showConfirmDialog(this,
                ia.getNom() + " propose une alliance.",
                "Diplomatie", JOptionPane.YES_NO_OPTION);
            if (rep == JOptionPane.YES_OPTION) moteur.accepterAlliance(ia);
            else moteur.refuserAlliance(ia);
        }
    }

    private class ClavierJeu extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();
            Unite sel = moteur.getUniteSelectionneeSurMap();

            if (sel != null && sel.getCamp().equals("JOUEUR")) {
                int nL = sel.getLigne();
                int nC = sel.getColonne();
                boolean move = false;

                if (code == KeyEvent.VK_UP)         { nL--; move = true; }
                else if (code == KeyEvent.VK_DOWN)  { nL++; move = true; }
                else if (code == KeyEvent.VK_LEFT)  { nC--; move = true; }
                else if (code == KeyEvent.VK_RIGHT) { nC++; move = true; }

                if (move) {
                    moteur.deplacerUniteSelectionnee(nL, nC);
                    if (!sel.canMove()) moteur.cycleUniteSuivante();
                    rafraichir();
                    return;
                }
            }

            if (code == KeyEvent.VK_SPACE) {
                if (sel != null) sel.setABouge(true);
                moteur.cycleUniteSuivante();
            }

            if (code == KeyEvent.VK_ESCAPE) {
                gestion.setVisible(true);
            }

            if (code == KeyEvent.VK_D) {
                new FenetreDiplomatie(FenetreJeu.this, moteur).setVisible(true);
                FenetreJeu.this.requestFocusInWindow();
            }

            if (code == KeyEvent.VK_S) {
                moteur.fonderVille();
            }

            if (code == KeyEvent.VK_ENTER) {
                moteur.passerTour();
            }

            if (code == KeyEvent.VK_F) {
                panneau.toggleBrouillard();
            }

            if (code == KeyEvent.VK_Q || code == KeyEvent.VK_C) {
                QG city = null;
                for (Batiment b : moteur.getBatiments()) {
                    if (b instanceof QG && b.getProprietaire().equals("JOUEUR")) {
                        city = (QG) b;
                        break;
                    }
                }
                if (city != null) {
                    new FenetreCite(FenetreJeu.this, moteur, city).setVisible(true);
                    FenetreJeu.this.requestFocusInWindow();
                }
            }

            rafraichir();
        }
    }

    private class SourisJeu extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            FenetreJeu.this.requestFocusInWindow();

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
                } else {
                    Unite u   = moteur.getUniteAt(lig, col);
                    Batiment b = moteur.getBatimentAt(lig, col);
                    Unite sel  = moteur.getUniteSelectionneeSurMap();

                    if (u != null && "JOUEUR".equals(u.getCamp())) {
                        moteur.setUniteSelectionneeSurMap(u);
                    } else if (u != null && sel != null && sel.getCamp().equals("JOUEUR")) {
                        if (!moteur.getDiplomatieManager().sontAllies(sel.getCamp(), u.getCamp())) {
                            moteur.deplacerUniteSelectionnee(lig, col);
                            Unite apres = moteur.getUniteSelectionneeSurMap();
                            if (apres == null || !apres.canMove()) moteur.cycleUniteSuivante();
                        }
                    } else if (b != null && "JOUEUR".equals(b.getProprietaire())) {
                        if (b instanceof QG) {
                            new FenetreCite(FenetreJeu.this, moteur, (QG) b).setVisible(true);
                            FenetreJeu.this.requestFocusInWindow();
                        }
                    } else if (sel != null) {
                        moteur.deplacerUniteSelectionnee(lig, col);
                        Unite apres = moteur.getUniteSelectionneeSurMap();
                        if (apres == null || !apres.canMove()) moteur.cycleUniteSuivante();
                    }
                }
            }
            rafraichir();
        }
    }
}