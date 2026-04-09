package process;

import config.Config;
import data.architecture.Batiment;
import data.architecture.Carte;
import data.architecture.Ferme;
import data.unites.Faction;
import java.util.List;

public class EconomieManager {

    public int calculerRevenuDuTour(Faction faction, Carte carte, List<Batiment> batiments) {
        int gain = 0;


        int casesOwned = faction.getNombreDeCases(carte);
        gain += casesOwned * Config.OR_PAR_CASE;


        for (Batiment b : batiments) {
            if (b instanceof Ferme && b.getProprietaire().equals(faction.getNom())) {
                gain += Config.OR_PAR_FERME;
            }
        }

        faction.ajouterOr(gain);
        return gain;
    }
}
