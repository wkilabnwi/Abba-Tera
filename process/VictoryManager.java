package process;

import data.architecture.Carte;
import data.architecture.QG;
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
        
        if (faction.getVilles().isEmpty()) return;
        boolean toutesDetruites = true;
        for (QG qg : faction.getVilles()) {
            if (qg.getPv() > 0) {
                toutesDetruites = false;
                break;
            }
        }
        if (toutesDetruites) faction.setEliminee(true);
    }

    public boolean verifierDefaite(Faction joueur) {
        return joueur.isEliminee();
    }
}