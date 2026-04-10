package process;

import data.architecture.Case;
import data.unites.Faction;
import data.unites.Unite;
import java.util.Random;

public class CombatManager {
    private Random random = new Random();

    /**
     * Ranged attack: attacker hits but does not move into the tile.
     * Used when portee > 1.
     */
    public String executerAttaqueDistance(Unite attaquant, Unite cible, Case caseCible) {
        int forceAttaque = attaquant.getForce();
        int bonusTerrain = caseCible.getBonusDefense();
        int defenseTotale = cible.getForce() + bonusTerrain;

        int jet = random.nextInt(forceAttaque + defenseTotale);
        if (jet < forceAttaque) {
            cible.recevoirDegats(cible.getPvMax() / 2); 
            if (cible.estMort()) {
                return attaquant.getType() + " a abattu " + cible.getType() + " a distance !";
            }
            return attaquant.getType() + " blesse " + cible.getType() + " de loin !";
        } else {
            return cible.getType() + " resiste au tir !";
        }
    }

    public String executerCombat(Unite attaquant, Unite cible, Case caseCible) {
        
        if (attaquant.getPortee() >= 2) {
            attaquant.consommerDeplacement(attaquant.getPointsDeplacementMax());
            return executerAttaqueDistance(attaquant, cible, caseCible);
        }

        
        int forceAttaque = attaquant.getForce();
        int bonusTerrain = caseCible.getBonusDefense();
        int defenseTotale = cible.getForce() + bonusTerrain;

        int jet = random.nextInt(forceAttaque + defenseTotale);
        if (jet < forceAttaque) {
            cible.recevoirDegats(cible.getPvMax());
            attaquant.consommerDeplacement(1);
            return attaquant.getType() + " a vaincu " + cible.getType() + " !";
        } else {
            attaquant.recevoirDegats(attaquant.getPvMax());
            attaquant.consommerDeplacement(attaquant.getPointsDeplacementMax());
            return cible.getType() + " a repousse l'assaut !";
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