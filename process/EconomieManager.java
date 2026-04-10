package process;

import config.Config;
import data.architecture.Batiment;
import data.architecture.Carte;
import data.architecture.Case;
import data.architecture.Ferme;
import data.architecture.QG;
import data.unites.Faction;
import data.unites.Unite;
import java.util.List;

public class EconomieManager {

    private static final int COUT_ENTRETIEN_SOLDAT    = 1;
    private static final int COUT_ENTRETIEN_ARCHER    = 1;
    private static final int COUT_ENTRETIEN_CHEVALIER = 2;
    private static final int COUT_ENTRETIEN_FERME     = 2;

    public int calculerRevenuDuTour(Faction faction, Carte carte, List<Batiment> batiments, List<Unite> unites) {
        int gain = 0;

        
        for (int l = 0; l < carte.getHauteur(); l++) {
            for (int c = 0; c < carte.getLargeur(); c++) {
                Case laCase = carte.getCase(l, c);
                if (laCase.getProprietaire().equals(faction.getNom())) {
                    gain += laCase.getRendementOr();
                }
            }
        }

        
        for (Batiment b : batiments) {
            if (b instanceof Ferme && b.getProprietaire().equals(faction.getNom())) {
                gain += Config.OR_PAR_FERME;
            }
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
        }

        
        int net = gain - couts;
        if (faction.estMalheureuse()) net = net / 2;

        faction.ajouterOr(net);
        return net;
    }

    
    public int calculerRevenuDuTour(Faction faction, Carte carte, List<Batiment> batiments) {
        int gain = 0;
        for (int l = 0; l < carte.getHauteur(); l++) {
            for (int c = 0; c < carte.getLargeur(); c++) {
                Case laCase = carte.getCase(l, c);
                if (laCase.getProprietaire().equals(faction.getNom())) {
                    gain += laCase.getRendementOr();
                }
            }
        }
        for (Batiment b : batiments) {
            if (b instanceof Ferme && b.getProprietaire().equals(faction.getNom())) {
                gain += Config.OR_PAR_FERME;
            }
        }
        faction.ajouterOr(gain);
        return gain;
    }
}