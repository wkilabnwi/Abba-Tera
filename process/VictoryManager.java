package process;

import data.architecture.Carte;
import data.unites.Faction;
import java.util.List;

public class VictoryManager {

    public boolean verifierDominationTerritoriale(Carte carte, Faction joueur) {
        int totalCases = carte.getLargeur() * carte.getHauteur();
        int casesPossedees = joueur.getNombreDeCases(carte);
        double pourcentage = (double) casesPossedees / totalCases;
        return pourcentage >= 0.75;
    }

    public boolean verifierDominationMilitaire(List<Faction> factions, Faction joueur) {
        for (Faction f : factions) {
            if (!f.getNom().equals(joueur.getNom()) && !f.isEliminee()) {
                return false;
            }
        }
        return true;
    }

    public void verifierElimination(Faction faction) {
        if (faction.getQG() != null && faction.getQG().getPv() <= 0) {
            faction.setEliminee(true);
        }
    }

    public boolean verifierDefaite(Faction joueur) {
        return joueur.isEliminee()
            || (joueur.getQG() != null && joueur.getQG().getPv() <= 0);
    }
}
