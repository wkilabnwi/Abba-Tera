package process;

import data.architecture.Carte;
import data.architecture.Case;
import data.unites.Unite;

public class DeplacementManager {

    public void gererDeplacement(Unite u, int nL, int nC, Carte carte, MoteurJeu moteur) {
        if (!u.canMove()) {
            moteur.setDernierMouvement("Cette unite a deja bouge ce tour !");
            return;
        }

        int distance = Math.abs(nL - u.getLigne()) + Math.abs(nC - u.getColonne());
        if (distance != 1) {
            moteur.setDernierMouvement("Deplacement impossible : une case a la fois !");
            return;
        }

        if (!carte.estDansLaGrille(nL, nC)) {
            moteur.setDernierMouvement("Hors de la carte !");
            return;
        }

        if (moteur.getBatimentAt(nL, nC) != null) {
            moteur.setDernierMouvement("Impossible : un batiment bloque le passage !");
            return;
        }

        if (moteur.getUniteAt(nL, nC) != null) {
            moteur.setDernierMouvement("Case occupee par une autre unite !");
            return;
        }

        Case cible = carte.getCase(nL, nC);
        String terrain = cible.getTypeTerrain();

        if (terrain.equals("EAU") || terrain.equals("MONTAGNE")) {
            moteur.setDernierMouvement("Terrain impraticable !");
            return;
        }

        int cout = 1;
        if (terrain.equals("FORET") && !u.getType().equals("Chevalier")) cout = 2;

        String proprietaire = cible.getProprietaire();
        if (!proprietaire.equals(u.getCamp()) && !moteur.getDiplomatieManager().sontAllies(u.getCamp(), proprietaire)) {
            data.architecture.Batiment b = moteur.getBatimentAt(nL, nC);
            if (b instanceof data.architecture.QG) {
                data.architecture.QG qgCible = (data.architecture.QG) b;
                if (qgCible.aUneGarnison()) {
                    data.unites.Unite defenseur = qgCible.getPremierDefenseur();
                    String log = moteur.getCombatManager().executerCombat(u, defenseur, cible);
                    moteur.setDernierMouvement("Assaut sur " + qgCible.getNomVille() + " ! " + log);
                    if (defenseur.estMort()) {
                        moteur.getUnites().remove(defenseur);
                        qgCible.retirerGarnison(defenseur);
                        if (!qgCible.aUneGarnison()) {
                            cible.setProprietaire(u.getCamp());
                            u.setLigne(nL);
                            u.setColonne(nC);
                        }
                    }
                    if (u.estMort()) moteur.getUnites().remove(u);
                    u.consommerDeplacement(u.getPointsDeplacement());
                    return;
                }
            }
            cible.setProprietaire(u.getCamp());
        }

        u.setLigne(nL);
        u.setColonne(nC);
        u.consommerDeplacement(Math.min(cout, u.getPointsDeplacement()));
        moteur.setDernierMouvement(u.getType() + " -> (" + nL + "," + nC + ") PM:" + u.getPointsDeplacement());
    }
}