package process;

import data.unites.Faction;
import java.util.ArrayList;
import java.util.List;

public class DiplomatieManager {

    private List<String> alliances = new ArrayList<String>();





    public void proposerAlliance(Faction a, Faction b) {
        String cle = construireCle(a.getNom(), b.getNom());
        if (!alliances.contains(cle)) {
            alliances.add(cle);
        }
    }

    public void trahir(Faction a, Faction b) {
        String cle = construireCle(a.getNom(), b.getNom());
        alliances.remove(cle);
    }

    
    public boolean sontAllies(Faction a, Faction b) {
        return alliances.contains(construireCle(a.getNom(), b.getNom()));
    }

    
    public boolean sontAllies(String nomA, String nomB) {
        return alliances.contains(construireCle(nomA, nomB));
    }





    public boolean aLeDroitDePassage(String campUnite, String proprietaireCase) {
        if (proprietaireCase.equals("NEUTRE") || proprietaireCase.equals(campUnite)) {
            return true;
        }
        return sontAllies(campUnite, proprietaireCase);
    }





    public List<String> getVisionsPartagees(String camp, List<Faction> factions) {
        List<String> visibles = new ArrayList<String>();
        for (Faction f : factions) {
            if (!f.getNom().equals(camp) && sontAllies(camp, f.getNom())) {
                visibles.add(f.getNom());
            }
        }
        return visibles;
    }





    public String transfererRessources(Faction envoyeur, Faction receveur, int montant) {
        if (!sontAllies(envoyeur, receveur)) {
            return "Transfert impossible : vous n'etes pas allies !";
        }
        if (envoyeur.getOr() < montant) {
            return "Or insuffisant pour le transfert !";
        }
        envoyeur.retirerOr(montant);
        receveur.ajouterOr(montant);
        return envoyeur.getNom() + " envoie " + montant + " or a " + receveur.getNom() + ".";
    }





    private String construireCle(String nomA, String nomB) {
        if (nomA.compareTo(nomB) <= 0) {
            return nomA + ":" + nomB;
        }
        return nomB + ":" + nomA;
    }

    public List<String> getAlliances() {
        return alliances;
    }
}
