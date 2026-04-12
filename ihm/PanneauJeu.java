package ihm;

import config.Config;
import data.architecture.Batiment;
import data.architecture.Carte;
import data.architecture.Case;
import data.architecture.Caserne;
import data.architecture.Ferme;
import data.architecture.Mine;
import data.architecture.QG;
import data.unites.Faction;
import data.unites.Unite;
import process.MoteurInterface;
import process.MoteurInterface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class PanneauJeu extends JPanel {

    private MoteurInterface moteur;
    private boolean brouillardActif = true;

    public void toggleBrouillard() {
        brouillardActif = !brouillardActif;
        repaint();
    }

    public void setBrouillard(boolean actif) {
        brouillardActif = actif;
        repaint();
    }

    private Image eauImg, foretImg, montagneImg, plaineImg;
    private Image joueurImg, ennemiImg;
    private Image qgImg, caserneImg, fermeImg, mineImg;

    private static final Color COULEUR_JOUEUR  = new Color(0,   0,   255, 55);
    private static final Color COULEUR_IA1     = new Color(220, 0,   0,   70);
    private static final Color COULEUR_IA2     = new Color(220, 120, 0,   70);
    private static final Color COULEUR_IA3     = new Color(140, 0,   200, 70);
    private static final Color COULEUR_RIVIERE = new Color(24,  69,  150, 200);

    public PanneauJeu(MoteurInterface moteur) {
        this.moteur = moteur;
        eauImg      = lireImage("res/Eau.png");
        plaineImg   = lireImage("res/Grass.png");
        foretImg    = lireImage("res/Grass.png");
        montagneImg = lireImage("res/Montagne.png");
        joueurImg   = lireImage("res/Joueur.png");
        ennemiImg   = lireImage("res/Ennemi.png");
        qgImg       = lireImage("res/QG.png");
        caserneImg  = lireImage("res/Caserne.png");
        fermeImg    = lireImage("res/Ferme.png");
        mineImg     = lireImage("res/Mine.png");
        brouillardActif = Config.BROUILLARD_INITIAL;
    }

    public static Image lireImage(String filePath) {
        try {
            return ImageIO.read(new File(filePath));
        } catch (IOException e) {
            System.err.println("Image introuvable : " + filePath);
            return null;
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        dessinerTerrain(g2);
        dessinerBorduresCulture(g2);
        dessinerBatiments(g2);
        dessinerUnites(g2);
        if (brouillardActif) dessinerBrouillard(g2);
        dessinerSelection(g2);
        dessinerGrille(g2);
    }

    private void dessinerTerrain(Graphics2D g2) {
        Carte carte = moteur.getCarte();
        int T = Config.TAILLE_CASE;

        for (int l = 0; l < Config.NB_LIGNES; l++) {
            for (int c = 0; c < Config.NB_COLONNES; c++) {
                int x = c * T;
                int y = l * T;
                Case laCase = carte.getCase(l, c);
                String terrain = laCase.getTypeTerrain();

                Image imgTerrain = plaineImg;
                if (terrain.equals("MONTAGNE"))   imgTerrain = montagneImg;
                else if (terrain.equals("EAU"))   imgTerrain = eauImg;
                else if (terrain.equals("FORET")) imgTerrain = foretImg;

                if (imgTerrain != null) {
                    g2.drawImage(imgTerrain, x, y, T, T, null);
                } else {
                    if (terrain.equals("EAU"))           g2.setColor(new Color(24, 69, 150));
                    else if (terrain.equals("MONTAGNE")) g2.setColor(new Color(100, 90, 80));
                    else if (terrain.equals("FORET"))    g2.setColor(new Color(20, 100, 20));
                    else                                  g2.setColor(new Color(80, 160, 60));
                    g2.fillRect(x, y, T, T);
                }

                if (laCase.hasRiviere() && !terrain.equals("EAU")) {
                    g2.setColor(COULEUR_RIVIERE);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawLine(x + T / 2, y, x + T / 2, y + T);
                    g2.setStroke(new BasicStroke(1));
                }

                String proprio = laCase.getProprietaire();
                Color overlay = null;
                if (proprio.equals("JOUEUR"))     overlay = COULEUR_JOUEUR;
                else if (proprio.equals("IA_1"))  overlay = COULEUR_IA1;
                else if (proprio.equals("IA_2"))  overlay = COULEUR_IA2;
                else if (proprio.equals("IA_3"))  overlay = COULEUR_IA3;
                if (overlay != null) {
                    g2.setColor(overlay);
                    g2.fillRect(x, y, T, T);
                }

                String res = laCase.getRessource();
                if (res != null) {
                    dessinerRessource(g2, res, x, y, T);
                }
            }
        }
    }

    private void dessinerRessource(Graphics2D g2, String res, int x, int y, int T) {
        int cx = x + T / 2;
        int cy = y + T / 2;
        int s  = Math.max(4, T / 4);

        if (res.equals("BLE")) {
            g2.setColor(new Color(240, 200, 30));
            for (int i = -1; i <= 1; i++) {
                g2.drawLine(cx + i * s / 2, cy + s, cx + i * s / 2, cy - s);
                g2.drawLine(cx + i * s / 2, cy, cx + i * s / 2 + s / 2, cy - s / 2);
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

    private void dessinerBorduresCulture(Graphics2D g2) {
        int T = Config.TAILLE_CASE;
        for (Batiment b : moteur.getBatiments()) {
            if (!(b instanceof QG)) continue;
            QG qg = (QG) b;
            Color couleur = couleurBordure(qg.getProprietaire());
            g2.setColor(couleur);
            g2.setStroke(new BasicStroke(2));
            int rayon  = qg.getRayonCulture();
            int startL = qg.getLigne()   - rayon;
            int endL   = qg.getLigne()   + rayon;
            int startC = qg.getColonne() - rayon;
            int endC   = qg.getColonne() + rayon;
            g2.drawRect(startC * T, startL * T, (endC - startC + 1) * T, (endL - startL + 1) * T);
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void dessinerBatiments(Graphics2D g2) {
        int T = Config.TAILLE_CASE;
        for (Batiment b : moteur.getBatiments()) {
            int x = b.getColonne() * T;
            int y = b.getLigne()   * T;
            Image img = null;
            if (b instanceof QG)           img = qgImg;
            else if (b instanceof Caserne) img = caserneImg;
            else if (b instanceof Ferme)   img = fermeImg;
            else if (b instanceof Mine)    img = mineImg;
            if (img != null) {
                g2.drawImage(img, x, y, T, T, null);
            } else {
                if (b instanceof Mine) {
                    g2.setColor(new Color(120, 80, 40));
                    g2.fillRect(x + 4, y + 4, T - 8, T - 8);
                    g2.setColor(Color.YELLOW);
                    g2.setFont(new Font("Arial", Font.BOLD, 9));
                    g2.drawString("M", x + T / 2 - 3, y + T / 2 + 3);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRect(x + 4, y + 4, T - 8, T - 8);
                }
            }
            if (b instanceof QG) {
                QG qg = (QG) b;
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 9));
                g2.drawString(qg.getNomVille(), x, y - 2);

                float hpRatio = (float) qg.getPv() / qg.getPvMax();
                g2.setColor(hpRatio > 0.5f ? new Color(0, 200, 0) : Color.RED);
                g2.fillRect(x, y, (int)(T * hpRatio), 4);
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(x, y, T, 4);

                if (qg.aUneGarnison()) {
                    g2.setColor(new Color(100, 220, 100, 180));
                    g2.fillRect(x, y + T - 14, T, 14);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Arial", Font.BOLD, 9));
                    StringBuilder garStr = new StringBuilder("G(" + qg.getGarnison().size() + "):");
                    for (data.unites.Unite gu : qg.getGarnison()) {
                        garStr.append(gu.getType().substring(0, 1));
                        garStr.append(gu.getPv());
                        garStr.append(" ");
                    }
                    g2.drawString(garStr.toString(), x + 2, y + T - 3);
                }
            }
            g2.setColor(Color.GREEN);
            g2.setFont(new Font("Arial", Font.BOLD, 8));
            g2.drawString("" + b.getPv(), x + 2, y + T - 2);
        }
    }

    private void dessinerUnites(Graphics2D g2) {
        int T = Config.TAILLE_CASE;
        for (Unite u : moteur.getUnites()) {
            if (u.isEnGarnison()) continue;
            int x = u.getColonne() * T;
            int y = u.getLigne()   * T;

            if (u.getType().equals("Creep")) {
                g2.setColor(new Color(140, 0, 200, 200));
                g2.fillOval(x + 4, y + 4, T - 8, T - 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.drawString("C", x + T / 2 - 4, y + T / 2 + 4);
                float ratio = (float) u.getPv() / u.getPvMax();
                g2.setColor(Color.RED);
                g2.fillRect(x, y + T - 4, (int) (T * ratio), 4);
                continue;
            }

            Image img = "JOUEUR".equals(u.getCamp()) ? joueurImg : ennemiImg;
            if (img != null) {
                g2.drawImage(img, x, y, T, T, null);
            } else {
                g2.setColor(couleurFaction(u.getCamp()));
                g2.fillRect(x + 4, y + 4, T - 8, T - 8);
            }
            g2.setColor(couleurFaction(u.getCamp()));
            g2.fillOval(x + T - 8, y + 2, 6, 6);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.drawString(u.getType().substring(0, 1), x + 2, y + 10);
            float ratio = (float) u.getPv() / u.getPvMax();
            g2.setColor(ratio > 0.5f ? Color.GREEN : Color.RED);
            g2.fillRect(x, y + T - 4, (int) (T * ratio), 4);
            g2.setColor(Color.CYAN);
            g2.setFont(new Font("Arial", Font.PLAIN, 8));
            g2.drawString("" + u.getPointsDeplacement(), x + 2, y + T - 6);
            if (u.getPortee() >= 2) {
                g2.setColor(Color.ORANGE);
                g2.drawOval(x + T - 9, y + T - 9, 7, 7);
            }
        }
    }

    private void dessinerSelection(Graphics2D g2) {
        Unite sel = moteur.getUniteSelectionneeSurMap();
        if (sel != null) {
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(sel.getColonne() * Config.TAILLE_CASE,
                        sel.getLigne()   * Config.TAILLE_CASE,
                        Config.TAILLE_CASE, Config.TAILLE_CASE);
            g2.setStroke(new BasicStroke(1));
        }
    }

    private void dessinerBrouillard(Graphics2D g2) {
        int T = Config.TAILLE_CASE;
        Faction joueur = moteur.getFactionJoueur();
        for (int l = 0; l < Config.NB_LIGNES; l++) {
            for (int c = 0; c < Config.NB_COLONNES; c++) {
                if (!joueur.aExplore(l, c)) {
                    g2.setColor(new Color(0, 0, 0, 200));
                    g2.fillRect(c * T, l * T, T, T);
                }
            }
        }
    }

    private void dessinerGrille(Graphics2D g2) {
        int T = Config.TAILLE_CASE;
        g2.setColor(new Color(0, 0, 0, 25));
        g2.setStroke(new BasicStroke(1));
        for (int l = 0; l <= Config.NB_LIGNES; l++) {
            g2.drawLine(0, l * T, Config.NB_COLONNES * T, l * T);
        }
        for (int c = 0; c <= Config.NB_COLONNES; c++) {
            g2.drawLine(c * T, 0, c * T, Config.NB_LIGNES * T);
        }
    }

    private Color couleurFaction(String camp) {
        if (camp.equals("JOUEUR")) return Color.BLUE;
        if (camp.equals("IA_1"))   return Color.RED;
        if (camp.equals("IA_2"))   return Color.ORANGE;
        if (camp.equals("IA_3"))   return new Color(140, 0, 200);
        return Color.GRAY;
    }

    private Color couleurBordure(String camp) {
        if (camp.equals("JOUEUR")) return new Color(0, 80, 255, 200);
        if (camp.equals("IA_1"))   return new Color(220, 0, 0, 200);
        if (camp.equals("IA_2"))   return new Color(220, 120, 0, 200);
        if (camp.equals("IA_3"))   return new Color(140, 0, 200, 200);
        return Color.GRAY;
    }
}