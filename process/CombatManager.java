package process;

import data.architecture.Case;
import data.unites.Faction;
import data.unites.Unite;
import java.util.Random;

public class CombatManager {
    private Random random = new Random();

public String executerCombat(Unite attaquant, Unite cible, Case caseCible) {
        int forceAttaque = attaquant.getForce();
        int forceDefense = cible.getForce();


        int bonusTerrain = caseCible.getBonusDefense();
        int defenseTotale = forceDefense + bonusTerrain;


        int jetDeDes = random.nextInt(forceAttaque + defenseTotale);

        if (jetDeDes < forceAttaque) {

            cible.recevoirDegats(cible.getPvMax());
            return attaquant.getType() + " a vaincu " + cible.getType() + "!";
        } else {

            attaquant.recevoirDegats(attaquant.getPvMax());
            return cible.getType() + " a repoussé l'assaut!";
        }
    }

    public int calculerDegatsFaction(Faction attaquante, Faction cible) {
        int forceTotale = (attaquante.getNbSoldats() * 3)
                        + (attaquante.getNbArchers() * 5)
                        + (attaquante.getNbChevaliers() * 8);
        double multiplicateur = 0.8 + (Math.random() * 0.4);
        return (int) (forceTotale * multiplicateur);
    }
}
