package ihm;

import config.Config;
import data.architecture.Batiment;
import data.architecture.Carte;
import data.architecture.Case;
import data.architecture.Caserne;
import data.architecture.Ferme;
import data.architecture.QG;
import data.unites.Faction;
import data.unites.Unite;
import process.MoteurJeu;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class PanneauJeu extends JPanel {

    private MoteurJeu moteur;
    private Image eauImg, foretImg, montagneImg, plaineImg;
    private Image joueurImg, ennemiImg;
    private Image qgImg, caserneImg, fermeImg;
    private TexturePaint ditherFog;

    private static final Color COULEUR_JOUEUR = new Color(0,   0,   255, 55);
    private static final Color COULEUR_IA1    = new Color(220, 0,   0,   70);
    private static final Color COULEUR_IA2    = new Color(220, 120, 0,   70);
    private static final Color COULEUR_IA3    = new Color(140, 0,   200, 70);
    private static final Color COULEUR_ALLIE  = new Color(0,   200, 200, 40);

    public PanneauJeu(MoteurJeu moteur) {
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
        this.ditherFog = createDitherTexture();
    }


private TexturePaint createDitherTexture() {
    BufferedImage pattern = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
    Color dot = new Color(0, 0, 0, 160);
    Color trans = new Color(0, 0, 0, 0);

    pattern.setRGB(0, 0, dot.getRGB());
    pattern.setRGB(1, 1, dot.getRGB());
    pattern.setRGB(1, 0, trans.getRGB());
    pattern.setRGB(0, 1, trans.getRGB());
    
    return new TexturePaint(pattern, new Rectangle2D.Double(0, 0, 2, 2));
}
    public static Image lireImage(String filePath) {
        try {
            return ImageIO.read(new File(filePath));
        } catch (IOException e) {
            System.err.println("Image introuvable : " + filePath);
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        dessinerTerrain(g2);
        dessinerBatiments(g2);
        dessinerUnites(g2);
        dessinerSelection(g2);
        dessinerGrille(g2);
    }

private void dessinerTerrain(Graphics2D g2) {
    Carte carte = moteur.getCarte();
    Faction joueur = moteur.getFactionJoueur();
    List<String> visionsPartagees = moteur.getDiplomatieManager()
        .getVisionsPartagees("JOUEUR", moteur.getFactions());

    for (int l = 0; l < Config.NB_LIGNES; l++) {
        for (int c = 0; c < Config.NB_COLONNES; c++) {
            int x = c * Config.TAILLE_CASE;
            int y = l * Config.TAILLE_CASE;
            Case laCase = carte.getCase(l, c);

            Image imgTerrain = plaineImg;
            String terrain = laCase.getTypeTerrain();
            if (terrain.equals("MONTAGNE"))     imgTerrain = montagneImg;
            else if (terrain.equals("EAU"))     imgTerrain = eauImg;
            else if (terrain.equals("FORET"))   imgTerrain = foretImg;

            if (imgTerrain != null) {
                g2.drawImage(imgTerrain, x, y, Config.TAILLE_CASE, Config.TAILLE_CASE, null);
            }

            String proprio = laCase.getProprietaire();
            Color overlay = null;
            if (proprio.equals("JOUEUR")) {
                overlay = COULEUR_JOUEUR;
            } else if (proprio.equals("IA_1")) {
                overlay = COULEUR_IA1;
            } else if (proprio.equals("IA_2")) {
                overlay = COULEUR_IA2;
            } else if (proprio.equals("IA_3")) {
                overlay = COULEUR_IA3;
            }

            if (overlay != null) {
                g2.setColor(overlay);
                g2.fillRect(x, y, Config.TAILLE_CASE, Config.TAILLE_CASE);
            }
        }
    }
}



 private void dessinerBatiments(Graphics2D g2) {
    for (Batiment b : moteur.getBatiments()) {
        int x = b.getColonne() * Config.TAILLE_CASE;
        int y = b.getLigne()   * Config.TAILLE_CASE;
        Image img = (b instanceof QG) ? qgImg : (b instanceof Caserne) ? caserneImg : fermeImg;
        if (img != null) g2.drawImage(img, x, y, Config.TAILLE_CASE, Config.TAILLE_CASE, null);
        
        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        g2.drawString("PV:" + b.getPv(), x + 2, y + Config.TAILLE_CASE - 2);
    }
}

    private void dessinerUnites(Graphics2D g2) {
       for (Unite u : moteur.getUnites()) {
        int x = u.getColonne() * Config.TAILLE_CASE;
        int y = u.getLigne()   * Config.TAILLE_CASE;

        Image img = "JOUEUR".equals(u.getCamp()) ? joueurImg : ennemiImg;
        if (img != null) g2.drawImage(img, x, y, Config.TAILLE_CASE, Config.TAILLE_CASE, null);

        g2.setColor(couleurFaction(u.getCamp()));
        g2.fillOval(x + Config.TAILLE_CASE - 8, y + 2, 6, 6);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 9));
                g2.drawString(u.getType().substring(0, 1), x + 2, y + 10);

                float ratio = (float) u.getPv() / u.getPvMax();
                g2.setColor(ratio > 0.5f ? Color.GREEN : Color.RED);
                g2.fillRect(x, y + Config.TAILLE_CASE - 4, (int) (Config.TAILLE_CASE * ratio), 4);
            }
        }

    private void dessinerSelection(Graphics2D g2) {
        Unite sel = moteur.getUniteSelectionneeSurMap();
        if (sel != null && moteur.getFactionJoueur().aExplore(sel.getLigne(), sel.getColonne())) {
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3));
            g2.drawRect(sel.getColonne() * Config.TAILLE_CASE, sel.getLigne() * Config.TAILLE_CASE, Config.TAILLE_CASE, Config.TAILLE_CASE);
        }
    }

    private void dessinerGrille(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 25));
        g2.setStroke(new BasicStroke(1));
        for (int l = 0; l <= Config.NB_LIGNES; l++) {
            g2.drawLine(0, l * Config.TAILLE_CASE, Config.NB_COLONNES * Config.TAILLE_CASE, l * Config.TAILLE_CASE);
        }
        for (int c = 0; c <= Config.NB_COLONNES; c++) {
            g2.drawLine(c * Config.TAILLE_CASE, 0, c * Config.TAILLE_CASE, Config.NB_LIGNES * Config.TAILLE_CASE);
        }
    }

    private Color couleurFaction(String camp) {
        if (camp.equals("JOUEUR")) return Color.BLUE;
        if (camp.equals("IA_1"))   return Color.RED;
        if (camp.equals("IA_2"))   return Color.ORANGE;
        if (camp.equals("IA_3"))   return new Color(140, 0, 200);
        return Color.GRAY;
    }
}