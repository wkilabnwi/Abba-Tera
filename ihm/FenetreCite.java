package ihm;

import config.Config;
import data.architecture.Batiment;
import data.architecture.Case;
import data.architecture.QG;
import data.unites.Unite;
import process.MoteurJeu;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FenetreCite extends JDialog {

    private MoteurJeu moteur;
    private QG qg;

    public FenetreCite(JFrame parent, MoteurJeu moteur, QG qg) {
        super(parent, "Cite : " + qg.getNomVille() + " [" + qg.getProprietaire() + "]", true);
        this.moteur = moteur;
        this.qg = qg;

        this.setSize(900, 620);
        this.setLocationRelativeTo(parent);
        this.setLayout(new BorderLayout(10, 10));
        this.getContentPane().setBackground(new Color(15, 15, 20));

        add(construireHeader(), BorderLayout.NORTH);
        add(construireCentre(), BorderLayout.CENTER);
        add(construireSud(), BorderLayout.SOUTH);
    }

    private JPanel construireHeader() {
        JPanel p = new JPanel(new GridLayout(1, 5));
        p.setBackground(new Color(30, 30, 40));

        int totalNourriture = 1;
        int totalProduction = 1;
        int totalOr = 0;
        for (String cle : qg.getCasesAssignees()) {
            String[] parts = cle.split(",");
            int tl = Integer.parseInt(parts[0]);
            int tc = Integer.parseInt(parts[1]);
            Case laCase = moteur.getCarte().getCase(tl, tc);
            if (laCase != null) {
                totalNourriture += laCase.getRendementNourriture();
                totalProduction += laCase.getRendementProduction();
                totalOr        += laCase.getRendementOr();
            }
        }
        if (qg.hasBuilding("Grenier")) totalNourriture += 2;

        int seuil = 10 + qg.getPopulation() * 10;
        p.add(createLabel("POP: " + qg.getPopulation(), Color.CYAN));
        p.add(createLabel("FOOD: " + qg.getNourriture() + "/" + seuil + " (+" + totalNourriture + ")", Color.GREEN));
        p.add(createLabel("PROD: +" + totalProduction, Color.ORANGE));
        p.add(createLabel("OR: +" + totalOr, Color.YELLOW));
        p.add(createLabel("CULTURE R:" + qg.getRayonCulture(), Color.MAGENTA));
        return p;
    }

    private JPanel construireCentre() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        p.add(new CarteMiniatrue(moteur, qg, this), BorderLayout.WEST);

        JPanel pInfo = new JPanel(new GridLayout(5, 1, 5, 5));
        pInfo.setOpaque(false);

        String projetAffiche = qg.getProjetEnCours();
        if (!projetAffiche.equals("Aucun")) {
            projetAffiche = projetAffiche + " (" + qg.getToursRestants() + " tours)";
        }

        pInfo.add(createLabel("Projet: " + projetAffiche, Color.WHITE));
        pInfo.add(createLabel("Travailleurs: " + qg.getNbTravailleurs() + "/" + qg.getPopulation(), Color.CYAN));
        pInfo.add(createLabel("Bonheur: " + moteur.getFactionJoueur().calculerBonheur(), Color.YELLOW));
        pInfo.add(createLabel("QG PV: " + qg.getPv() + "/" + qg.getPvMax(), Color.RED));

        JPanel pGarnison = new JPanel(new BorderLayout(5, 0));
        pGarnison.setOpaque(false);
        StringBuilder garnisonTxt = new StringBuilder("Garnison: ");
        if (qg.aUneGarnison()) {
            for (data.unites.Unite u : qg.getGarnison()) {
                garnisonTxt.append(u.getType().substring(0, 1)).append(" ");
            }
        } else {
            garnisonTxt.append("aucune");
        }
        JLabel lblG = new JLabel(garnisonTxt.toString(), SwingConstants.CENTER);
        lblG.setForeground(qg.aUneGarnison() ? new Color(100, 220, 100) : Color.GRAY);
        lblG.setFont(new Font("Monospaced", Font.BOLD, 12));
        JPanel pGarBtns = new JPanel(new GridLayout(1, 2, 3, 0));
        pGarBtns.setOpaque(false);
        JButton btnAssigner = new JButton("+");
        btnAssigner.addActionListener(new AssignerGarnisonAction());
        JButton btnRetirer = new JButton("-");
        btnRetirer.addActionListener(new RetirerGarnisonAction());
        btnRetirer.setEnabled(qg.aUneGarnison());
        pGarBtns.add(btnAssigner);
        pGarBtns.add(btnRetirer);
        pGarnison.add(lblG, BorderLayout.CENTER);
        pGarnison.add(pGarBtns, BorderLayout.EAST);
        pInfo.add(pGarnison);

        p.add(pInfo, BorderLayout.CENTER);
        return p;
    }

    private JPanel construireSud() {
        JPanel pBottom = new JPanel(new GridLayout(1, 2, 10, 0));
        pBottom.setOpaque(false);
        pBottom.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JPanel pUnits = new JPanel(new GridLayout(2, 2, 5, 5));
        pUnits.setBorder(BorderFactory.createTitledBorder(null, "UNITES", 0, 0, null, Color.WHITE));
        pUnits.add(creerBoutonProd("Soldat",    40,  2));
        pUnits.add(creerBoutonProd("Archer",    60,  3));
        pUnits.add(creerBoutonProd("Chevalier", 120, 5));
        pUnits.add(creerBoutonProd("Colon",     200, 8));

        JPanel pBuilds = new JPanel(new GridLayout(2, 2, 5, 5));
        pBuilds.setBorder(BorderFactory.createTitledBorder(null, "BATIMENTS", 0, 0, null, Color.YELLOW));

        if (!qg.hasBuilding("Grenier")) {
            pBuilds.add(creerBoutonProd("Grenier", 80, 3));
        } else {
            pBuilds.add(makeBuiltLabel("Grenier"));
        }
        if (!qg.hasBuilding("Marche")) {
            pBuilds.add(creerBoutonProd("Marche", 120, 4));
        } else {
            pBuilds.add(makeBuiltLabel("Marche"));
        }

        pBottom.add(pUnits);
        pBottom.add(pBuilds);
        return pBottom;
    }

    private JLabel makeBuiltLabel(String nom) {
        JLabel l = new JLabel(nom + ": OK", SwingConstants.CENTER);
        l.setForeground(Color.GREEN);
        return l;
    }

    private JButton creerBoutonProd(String nom, int prix, int tours) {
        JButton b = new JButton(nom + " (" + prix + "or/" + tours + "t)");
        b.addActionListener(new LancerProductionAction(nom, prix, tours));
        return b;
    }

    private JLabel createLabel(String t, Color c) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setForeground(c);
        l.setFont(new Font("Monospaced", Font.BOLD, 13));
        return l;
    }

    void rafraichirFenetre() {
        dispose();
        new FenetreCite((JFrame) getOwner(), moteur, qg).setVisible(true);
    }

    private class LancerProductionAction implements ActionListener {
        private String nom;
        private int prix;
        private int tours;

        public LancerProductionAction(String nom, int prix, int tours) {
            this.nom   = nom;
            this.prix  = prix;
            this.tours = tours;
        }

        public void actionPerformed(ActionEvent e) {
            if (!qg.getProjetEnCours().equals("Aucun")) {
                JOptionPane.showMessageDialog(FenetreCite.this,
                    "Projet en cours : " + qg.getProjetEnCours());
                return;
            }
            if (moteur.getFactionJoueur().getOr() >= prix) {
                moteur.getFactionJoueur().retirerOr(prix);
                qg.setProjetEnCours(nom, tours);
                rafraichirFenetre();
            } else {
                JOptionPane.showMessageDialog(FenetreCite.this, "Or insuffisant !");
            }
        }
    }

    private class AssignerGarnisonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            data.unites.Unite sel = moteur.getUniteSelectionneeSurMap();
            if (sel == null || !sel.getCamp().equals("JOUEUR")) {
                JOptionPane.showMessageDialog(FenetreCite.this,
                    "Selectionnez d'abord une unite sur la carte !");
                return;
            }
            if (sel.getType().equals("Colon")) {
                JOptionPane.showMessageDialog(FenetreCite.this,
                    "Un Colon ne peut pas tenir garnison !");
                return;
            }
            int dist = Math.abs(sel.getLigne() - qg.getLigne()) + Math.abs(sel.getColonne() - qg.getColonne());
            if (dist > qg.getRayonCulture() + 1) {
                JOptionPane.showMessageDialog(FenetreCite.this,
                    "L'unite doit etre proche de la cite !");
                return;
            }
            qg.ajouterGarnison(sel);
            sel.setEnGarnison(true);
            sel.setLigne(-1);
            sel.setColonne(-1);
            sel.setABouge(true);
            moteur.setUniteSelectionneeSurMap(null);
            rafraichirFenetre();
        }
    }

    private class RetirerGarnisonAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!qg.aUneGarnison()) return;
            data.unites.Unite dernier = qg.getGarnison().get(qg.getGarnison().size() - 1);
            int[] pos = trouverCaseLibre();
            if (pos == null) {
                JOptionPane.showMessageDialog(FenetreCite.this,
                    "Pas de place adjacente pour deployer l'unite !");
                return;
            }
            dernier.setLigne(pos[0]);
            dernier.setColonne(pos[1]);
            dernier.setEnGarnison(false);
            dernier.setABouge(false);
            qg.retirerGarnison(dernier);
            rafraichirFenetre();
        }

        private int[] trouverCaseLibre() {
            int[] dl = {0, 1, 0, -1};
            int[] dc = {1, 0, -1, 0};
            for (int dir = 0; dir < 4; dir++) {
                int nl = qg.getLigne()   + dl[dir];
                int nc = qg.getColonne() + dc[dir];
                if (!moteur.getCarte().estDansLaGrille(nl, nc)) continue;
                data.architecture.Case laCase = moteur.getCarte().getCase(nl, nc);
                if (laCase.getTypeTerrain().equals("EAU"))      continue;
                if (laCase.getTypeTerrain().equals("MONTAGNE")) continue;
                if (moteur.getUniteAt(nl, nc) != null)          continue;
                if (moteur.getBatimentAt(nl, nc) != null)       continue;
                return new int[]{nl, nc};
            }
            return null;
        }
    }

    private class CarteMiniatrue extends JPanel {
        private MoteurJeu moteur;
        private QG qg;
        private FenetreCite parent;

        private static final int RAYON_AFFICHE = 3;
        private static final int TILE = 64;
        private static final Color COULEUR_RIVIERE = new Color(24, 69, 150, 220);

        private Image imgEau;
        private Image imgMontagne;
        private Image imgGrass;
        private Image imgQG;
        private Image imgCaserne;
        private Image imgFerme;

        public CarteMiniatrue(MoteurJeu moteur, QG qg, FenetreCite parent) {
            this.moteur = moteur;
            this.qg = qg;
            this.parent = parent;
            imgEau      = PanneauJeu.lireImage("res/Eau.png");
            imgMontagne = PanneauJeu.lireImage("res/Montagne.png");
            imgGrass    = PanneauJeu.lireImage("res/Grass.png");
            imgQG       = PanneauJeu.lireImage("res/QG.png");
            imgCaserne  = PanneauJeu.lireImage("res/Caserne.png");
            imgFerme    = PanneauJeu.lireImage("res/Ferme.png");
            int size = (RAYON_AFFICHE * 2 + 1) * TILE;
            this.setPreferredSize(new Dimension(size, size));
            this.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            this.setBackground(Color.BLACK);
            this.addMouseListener(new TileClickAction());
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diam    = RAYON_AFFICHE * 2 + 1;
            int centerL = qg.getLigne();
            int centerC = qg.getColonne();

            for (int i = 0; i < diam; i++) {
                for (int j = 0; j < diam; j++) {
                    int mapL = centerL - RAYON_AFFICHE + i;
                    int mapC = centerC - RAYON_AFFICHE + j;
                    int px = j * TILE;
                    int py = i * TILE;

                    Case laCase = moteur.getCarte().getCase(mapL, mapC);
                    if (laCase == null) {
                        g2.setColor(Color.DARK_GRAY);
                        g2.fillRect(px, py, TILE, TILE);
                        continue;
                    }

                    drawTerrain(g2, laCase, px, py);
                    drawRiver(g2, laCase, px, py);
                    drawResource(g2, laCase, px, py);
                    drawTerritoryOverlay(g2, laCase, px, py);
                    drawWorkerHighlight(g2, mapL, mapC, px, py);
                    drawBuilding(g2, mapL, mapC, px, py, centerL, centerC);
                    drawUnits(g2, mapL, mapC, px, py);
                    drawYields(g2, laCase, mapL, mapC, centerL, centerC, px, py);

                    g2.setColor(new Color(0, 0, 0, 60));
                    g2.drawRect(px, py, TILE, TILE);
                }
            }
        }

        private void drawTerrain(Graphics2D g2, Case laCase, int px, int py) {
            String terrain = laCase.getTypeTerrain();
            Image img;
            if (terrain.equals("EAU"))           img = imgEau;
            else if (terrain.equals("MONTAGNE")) img = imgMontagne;
            else                                  img = imgGrass;

            if (img != null) {
                g2.drawImage(img, px, py, TILE, TILE, null);
            } else {
                if (terrain.equals("EAU"))           g2.setColor(new Color(24, 69, 150));
                else if (terrain.equals("MONTAGNE")) g2.setColor(new Color(100, 90, 80));
                else if (terrain.equals("FORET"))    g2.setColor(new Color(20, 100, 20));
                else                                  g2.setColor(new Color(80, 160, 60));
                g2.fillRect(px, py, TILE, TILE);
            }
        }

        private void drawRiver(Graphics2D g2, Case laCase, int px, int py) {
            if (laCase.hasRiviere() && !laCase.getTypeTerrain().equals("EAU")) {
                g2.setColor(COULEUR_RIVIERE);
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(px + TILE / 2, py, px + TILE / 2, py + TILE);
                g2.setStroke(new BasicStroke(1));
            }
        }

        private void drawResource(Graphics2D g2, Case laCase, int px, int py) {
            String res = laCase.getRessource();
            if (res == null) return;
            int cx = px + TILE / 2;
            int cy = py + TILE / 2;
            int s  = TILE / 5;

            if (res.equals("BLE")) {
                g2.setColor(new Color(240, 200, 30));
                for (int k = -1; k <= 1; k++) {
                    g2.drawLine(cx + k * s / 2, cy + s, cx + k * s / 2, cy - s);
                    g2.drawLine(cx + k * s / 2, cy, cx + k * s / 2 + s / 2, cy - s / 2);
                }
            } else if (res.equals("FER")) {
                g2.setColor(new Color(180, 180, 200));
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(cx - s, cy + s, cx + s, cy - s);
                g2.fillOval(cx - s / 2 - 1, cy - s / 2 - 1, s / 2 + 2, s / 2 + 2);
                g2.fillRect(cx + s / 4, cy + s / 4, s / 2, s / 4);
                g2.setStroke(new BasicStroke(1));
            } else if (res.equals("CHEVAUX")) {
                g2.setColor(new Color(160, 100, 40));
                g2.setStroke(new BasicStroke(2));
                g2.drawArc(cx - s / 2, cy - s, s, s, 0, 180);
                g2.drawLine(cx - s / 2, cy - s / 2, cx - s / 2, cy + s / 2);
                g2.drawLine(cx + s / 2, cy - s / 2, cx + s / 2, cy + s / 2);
                g2.drawLine(cx - s / 4, cy + s / 2, cx - s / 4, cy + s);
                g2.drawLine(cx + s / 4, cy + s / 2, cx + s / 4, cy + s);
                g2.setStroke(new BasicStroke(1));
            } else if (res.equals("POISSON")) {
                g2.setColor(new Color(100, 200, 255));
                g2.setStroke(new BasicStroke(2));
                g2.drawArc(cx - s, cy - s / 2, s * 2, s, 20, 140);
                g2.drawArc(cx - s, cy - s / 2, s * 2, s, 200, 140);
                int[] xp = {cx + s, cx + s + s / 2, cx + s + s / 2};
                int[] yp = {cy, cy - s / 2, cy + s / 2};
                g2.fillPolygon(xp, yp, 3);
                g2.setStroke(new BasicStroke(1));
            }
        }

        private void drawTerritoryOverlay(Graphics2D g2, Case laCase, int px, int py) {
            String proprio = laCase.getProprietaire();
            Color overlay = null;
            if (proprio.equals("JOUEUR"))     overlay = new Color(0,   0,   255, 55);
            else if (proprio.equals("IA_1"))  overlay = new Color(220, 0,   0,   70);
            else if (proprio.equals("IA_2"))  overlay = new Color(220, 120, 0,   70);
            else if (proprio.equals("IA_3"))  overlay = new Color(140, 0,   200, 70);
            if (overlay != null) {
                g2.setColor(overlay);
                g2.fillRect(px, py, TILE, TILE);
            }
        }

        private void drawWorkerHighlight(Graphics2D g2, int mapL, int mapC, int px, int py) {
            String cle = mapL + "," + mapC;
            if (qg.estAssignee(cle)) {
                g2.setColor(new Color(255, 220, 0, 100));
                g2.fillRect(px, py, TILE, TILE);
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(px, py, TILE, TILE);
                g2.setStroke(new BasicStroke(1));
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.setColor(Color.YELLOW);
                g2.drawString("W", px + TILE / 2 - 5, py + TILE / 2 + 5);
            }
        }

        private void drawBuilding(Graphics2D g2, int mapL, int mapC, int px, int py, int centerL, int centerC) {
            Batiment b = moteur.getBatimentAt(mapL, mapC);
            if (b == null) return;
            Image img = null;
            if (b instanceof QG)                            img = imgQG;
            else if (b instanceof data.architecture.Caserne) img = imgCaserne;
            else if (b instanceof data.architecture.Ferme)   img = imgFerme;
            if (img != null) {
                g2.drawImage(img, px, py, TILE, TILE, null);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(px + 10, py + 10, TILE - 20, TILE - 20, 6, 6);
            }
            if (b instanceof QG) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 9));
                g2.drawString(((QG) b).getNomVille(), px + 2, py + TILE - 4);
            }
        }

        private void drawUnits(Graphics2D g2, int mapL, int mapC, int px, int py) {
            for (Unite u : moteur.getUnites()) {
                if (u.getLigne() == mapL && u.getColonne() == mapC) {
                    Color c = u.getCamp().equals("JOUEUR") ? new Color(0, 80, 220) : new Color(200, 30, 30);
                    g2.setColor(c);
                    g2.fillOval(px + TILE - 20, py + 4, 16, 16);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Arial", Font.BOLD, 9));
                    g2.drawString(u.getType().substring(0, 1), px + TILE - 15, py + 16);
                }
            }
        }

        private void drawYields(Graphics2D g2, Case laCase, int mapL, int mapC,
                                int centerL, int centerC, int px, int py) {
            if (mapL == centerL && mapC == centerC) return;
            boolean dansRayon = (Math.abs(mapL - centerL) <= qg.getRayonCulture()
                              && Math.abs(mapC - centerC) <= qg.getRayonCulture());
            if (!dansRayon) {
                g2.setColor(new Color(0, 0, 0, 120));
                g2.fillRect(px, py, TILE, TILE);
                return;
            }
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(px, py + TILE - 28, TILE, 28);
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.setColor(new Color(100, 255, 100));
            g2.drawString("F:" + laCase.getRendementNourriture(), px + 2, py + TILE - 18);
            g2.setColor(new Color(255, 160, 60));
            g2.drawString("P:" + laCase.getRendementProduction(), px + 2, py + TILE - 9);
            g2.setColor(new Color(255, 220, 0));
            g2.drawString("O:" + laCase.getRendementOr(), px + TILE / 2, py + TILE - 9);
        }

        private class TileClickAction extends MouseAdapter {
            public void mousePressed(MouseEvent e) {
                int j = e.getX() / TILE;
                int i = e.getY() / TILE;
                int diam = RAYON_AFFICHE * 2 + 1;
                if (i < 0 || i >= diam || j < 0 || j >= diam) return;

                int mapL = qg.getLigne()   - RAYON_AFFICHE + i;
                int mapC = qg.getColonne() - RAYON_AFFICHE + j;

                if (mapL == qg.getLigne() && mapC == qg.getColonne()) return;

                if (Math.abs(mapL - qg.getLigne())   > qg.getRayonCulture()
                 || Math.abs(mapC - qg.getColonne()) > qg.getRayonCulture()) return;

                Case laCase = moteur.getCarte().getCase(mapL, mapC);
                if (laCase == null) return;

                String cle = mapL + "," + mapC;
                int maxTravailleurs = qg.getPopulation();

                if (qg.estAssignee(cle)) {
                    qg.desassignerCase(cle);
                } else {
                    if (qg.getNbTravailleurs() >= maxTravailleurs) {
                        JOptionPane.showMessageDialog(CarteMiniatrue.this,
                            "Tous les travailleurs sont assignes !");
                        return;
                    }
                    qg.assignerCase(cle);
                }
                parent.rafraichirFenetre();
            }
        }
    }
}