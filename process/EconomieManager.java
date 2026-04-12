package process;

import config.Config;
import data.architecture.Batiment;
import data.architecture.Carte;
import data.architecture.Case;
import data.architecture.Ferme;
import data.architecture.Mine;
import data.unites.Faction;
import data.unites.Unite;
import java.util.List;

public class EconomieManager {

    private static final int COUT_ENTRETIEN_SOLDAT    = 1;
    private static final int COUT_ENTRETIEN_ARCHER    = 1;
    private static final int COUT_ENTRETIEN_CHEVALIER = 2;
    private static final int COUT_ENTRETIEN_FERME     = 2;

    public int calculerRevenuDuTour(Faction faction, Carte carte, List<Batiment> batiments, List<Unite> unites) {
        int gain = faction.getCacheOr();

        for (Batiment b : batiments) {
            if (b instanceof Ferme && b.getProprietaire().equals(faction.getNom())) gain += Config.OR_PAR_FERME;
            if (b instanceof Mine  && b.getProprietaire().equals(faction.getNom())) gain += 15;
        }

        int couts = 0;
        for (Unite u : unites) {
            if (!u.getCamp().equals(faction.getNom())) continue;
            if (u.getType().equals("Soldat"))    couts += COUT_ENTRETIEN_SOLDAT;
            if (u.getType().equals("Archer"))    couts += COUT_ENTRETIEN_ARCHER;
            if (u.getType().equals("Chevalier")) couts += COUT_ENTRETIEN_CHEVALIER;
        }

        for (Batiment b : batiments) {
            if (!b.getProprietaire().equals(faction.getNom())) continue;
            if (b instanceof Ferme) couts += COUT_ENTRETIEN_FERME;
            if (b instanceof Mine)  couts += 3;
        }

        int net = gain - couts;
        if (faction.estMalheureuse()) net = net / 2;
        faction.ajouterOr(net);
        return net;
    }

    public void precalculerOrTerritoire(List<Faction> factions, Carte carte) {
        java.util.Map<String, Integer> orParFaction = new java.util.HashMap<String, Integer>();
        for (Faction f : factions) orParFaction.put(f.getNom(), 0);

        for (int l = 0; l < carte.getHauteur(); l++) {
            for (int c = 0; c < carte.getLargeur(); c++) {
                Case laCase = carte.getCase(l, c);
                String prop = laCase.getProprietaire();
                if (orParFaction.containsKey(prop)) {
                    orParFaction.put(prop, orParFaction.get(prop) + laCase.getRendementOr());
                }
            }
        }

        for (Faction f : factions) {
            f.setCacheOr(orParFaction.getOrDefault(f.getNom(), 0));
        }
    }

}