package data.architecture;

import java.util.Random;
import config.Config;

public class Carte {
    private Case[][] grille;

    public Carte() {
        grille = new Case[Config.NB_LIGNES][Config.NB_COLONNES];
        initialiserCarte();
    }

    public int getLargeur() { return Config.NB_COLONNES; }
    public int getHauteur() { return Config.NB_LIGNES; }

private void initialiserCarte() {
    Random rand = new Random();
    for (int l = 0; l < Config.NB_LIGNES; l++) {
        for (int c = 0; c < Config.NB_COLONNES; c++) {
            grille[l][c] = new Case(l, c, "PLAINE");
        }
    }
    int nbCases = Config.NB_LIGNES * Config.NB_COLONNES;
    peindreZones("FORET",    nbCases / 8,  5, rand);
    peindreZones("EAU",      nbCases / 10, 4, rand);
    peindreZones("MONTAGNE", nbCases / 12, 3, rand);


    protegerCoin(0,                    0);
    protegerCoin(0,                    Config.NB_COLONNES - 1);
    protegerCoin(Config.NB_LIGNES - 1, 0);
    protegerCoin(Config.NB_LIGNES - 1, Config.NB_COLONNES - 1);

    assignerTerritoireDemarrage();
}

private void protegerCoin(int coinL, int coinC) {
    int rayon = 4;
    int startL = Math.max(0, coinL - rayon);
    int endL   = Math.min(Config.NB_LIGNES - 1, coinL + rayon);
    int startC = Math.max(0, coinC - rayon);
    int endC   = Math.min(Config.NB_COLONNES - 1, coinC + rayon);

    for (int l = startL; l <= endL; l++) {
        for (int c = startC; c <= endC; c++) {
            grille[l][c] = new Case(l, c, "PLAINE");
        }
    }
}

    private void peindreZones(String type, int totalCells, int nbBlobs, Random rand) {
        int cellsParBlob = totalCells / nbBlobs;
        for (int b = 0; b < nbBlobs; b++) {
            int seedL = rand.nextInt(Config.NB_LIGNES);
            int seedC = rand.nextInt(Config.NB_COLONNES);
            java.util.List<int[]> frontier = new java.util.ArrayList<int[]>();
            frontier.add(new int[]{seedL, seedC});
            int painted = 0;
            while (painted < cellsParBlob && !frontier.isEmpty()) {
                int idx = rand.nextInt(frontier.size());
                int[] cell = frontier.remove(idx);
                int l = cell[0];
                int c = cell[1];
                if (!estDansLaGrille(l, c)) continue;
                if (!grille[l][c].getTypeTerrain().equals("PLAINE")) continue;
                grille[l][c] = new Case(l, c, type);
                painted++;
                int[] dl = {-1, 1, 0, 0};
                int[] dc = {0, 0, -1, 1};
                for (int dir = 0; dir < 4; dir++) {
                    int nl = l + dl[dir];
                    int nc = c + dc[dir];
                    if (estDansLaGrille(nl, nc) && grille[nl][nc].getTypeTerrain().equals("PLAINE")) {
                        frontier.add(new int[]{nl, nc});
                    }
                }
            }
        }
    }

    private void assignerTerritoireDemarrage() {
        for (int l = 0; l < 3; l++) {
            for (int c = 0; c < 3; c++) {
                if (estDansLaGrille(l, c)) grille[l][c].setProprietaire("JOUEUR");
            }
        }
        for (int l = 0; l < 3; l++) {
            for (int c = Config.NB_COLONNES - 3; c < Config.NB_COLONNES; c++) {
                if (estDansLaGrille(l, c)) grille[l][c].setProprietaire("IA_1");
            }
        }
        for (int l = Config.NB_LIGNES - 3; l < Config.NB_LIGNES; l++) {
            for (int c = 0; c < 3; c++) {
                if (estDansLaGrille(l, c)) grille[l][c].setProprietaire("IA_2");
            }
        }
        for (int l = Config.NB_LIGNES - 3; l < Config.NB_LIGNES; l++) {
            for (int c = Config.NB_COLONNES - 3; c < Config.NB_COLONNES; c++) {
                if (estDansLaGrille(l, c)) grille[l][c].setProprietaire("IA_3");
            }
        }
    }

    public boolean estDansLaGrille(int ligne, int colonne) {
        return ligne >= 0 && ligne < Config.NB_LIGNES && colonne >= 0 && colonne < Config.NB_COLONNES;
    }

    public Case getCase(int ligne, int colonne) {
        if (estDansLaGrille(ligne, colonne)) return grille[ligne][colonne];
        return null;
    }
}