package process;

import data.architecture.Carte;
import data.architecture.Case;
import data.unites.Unite;

public class DeplacementManager {

    public void gererDeplacement(Unite u, int nL, int nC,
                                 Carte carte, MoteurJeu moteur) {
        if (!u.canMove()) {
            moteur.setDernierMouvement("Cette unite a deja bouge ce tour !");
            return;
        }

            int distance = Math.abs(nL - u.getLigne()) + Math.abs(nC - u.getColonne());
    if (distance != 1) {
        moteur.setDernierMouvement("Déplacement impossible : une case à la fois !");
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


        if (!moteur.getDiplomatieManager().aLeDroitDePassage(u.getCamp(), cible.getProprietaire())) {
            moteur.setDernierMouvement("Territoire ennemi : passage interdit !");
            return;
        }

        u.setLigne(nL);
        u.setColonne(nC);


        if (!cible.getProprietaire().equals(u.getCamp())) {
            boolean allie = moteur.getDiplomatieManager()
                .aLeDroitDePassage(u.getCamp(), cible.getProprietaire())
                && !cible.getProprietaire().equals("NEUTRE");
            if (!allie) {
                cible.setProprietaire(u.getCamp());
            }
        }

        u.setABouge(true);
        moteur.setDernierMouvement(u.getType() + " s'est deplace en (" + nL + "," + nC + ").");
    }
}
