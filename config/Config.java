package config;

import java.awt.Toolkit;
import java.awt.Dimension;

public class Config {
    public static final int NB_COLONNES = 40;

    private static final Dimension SCREEN = Toolkit.getDefaultToolkit().getScreenSize();

    public static final int TAILLE_CASE = (int) (SCREEN.getWidth() / NB_COLONNES);

    public static final int NB_LIGNES = (int) ((SCREEN.getHeight() - 110) / TAILLE_CASE);

    public static final int LARGEUR_ECRAN = (int) SCREEN.getWidth();
    public static final int HAUTEUR_ECRAN = (int) SCREEN.getHeight();

    public static final int OR_PAR_CASE = 2;
    public static final int OR_PAR_FERME = 10;

    public static final int RAYON_VISION = 1;

    public static boolean BROUILLARD_INITIAL = true;
}