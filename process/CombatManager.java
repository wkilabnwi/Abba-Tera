package process;

import data.architecture.Case;
import data.unites.Faction;
import data.unites.Unite;
import java.util.Random;

public class CombatManager {
    private Random random = new Random();

    private static final int XP_VICTOIRE  = 10;
    private static final int XP_SURVIE    = 3;
    private static final int XP_BLESSURE  = 5;

    public String executerAttaqueDistance(Unite attaquant, Unite cible, Case caseCible) {
        int forceAttaque  = attaquant.getForce();
        int bonusTerrain  = caseCible.getBonusDefense();
        int defenseTotale = cible.getForce() + bonusTerrain;

        int jet = random.nextInt(forceAttaque + defenseTotale);
        if (jet < forceAttaque) {
            int degats = Math.max(1, forceAttaque / 2 + random.nextInt(forceAttaque / 2 + 1));
            cible.recevoirDegats(degats);
            attaquant.consommerDeplacement(attaquant.getPointsDeplacementMax());
            if (cible.estMort()) {
                attaquant.gagnerXP(XP_VICTOIRE);
                return attaquant.getType() + " a abattu " + cible.getType() + " a distance !";
            }
            attaquant.gagnerXP(XP_BLESSURE);
            return attaquant.getType() + " blesse " + cible.getType() + " (-" + degats + " PV) de loin !";
        } else {
            attaquant.consommerDeplacement(attaquant.getPointsDeplacementMax());
            cible.gagnerXP(XP_SURVIE);
            return cible.getType() + " resiste au tir !";
        }
    }

    public String executerCombat(Unite attaquant, Unite cible, Case caseCible) {
        if (attaquant.getPortee() >= 2) {
            return executerAttaqueDistance(attaquant, cible, caseCible);
        }

        int forceAttaque  = attaquant.getForce();
        int bonusTerrain  = caseCible.getBonusDefense();
        int defenseTotale = cible.getForce() + bonusTerrain;

        int jet = random.nextInt(forceAttaque + defenseTotale);
        if (jet < forceAttaque) {
            int degats = Math.max(1, forceAttaque + random.nextInt(3) - 1);
            cible.recevoirDegats(degats);
            attaquant.consommerDeplacement(1);
            if (cible.estMort()) {
                attaquant.gagnerXP(XP_VICTOIRE);
                return attaquant.getType() + " a vaincu " + cible.getType() + " !";
            }
            attaquant.gagnerXP(XP_BLESSURE);
            return attaquant.getType() + " blesse " + cible.getType() + " (-" + degats + " PV) !";
        } else {
            int riposte = Math.max(1, cible.getForce() / 2 + random.nextInt(cible.getForce() / 2 + 1));
            attaquant.recevoirDegats(riposte);
            attaquant.consommerDeplacement(attaquant.getPointsDeplacementMax());
            cible.gagnerXP(XP_SURVIE);
            if (attaquant.estMort()) {
                cible.gagnerXP(XP_VICTOIRE - XP_SURVIE);
                return cible.getType() + " a repousse et tue l'assaillant !";
            }
            return cible.getType() + " repousse " + attaquant.getType() + " (-" + riposte + " PV) !";
        }
    }

    public int calculerDegatsFaction(Faction attaquante, Faction cible) {
        int forceTotale = (attaquante.getNbSoldats()    * 3)
                        + (attaquante.getNbArchers()    * 5)
                        + (attaquante.getNbChevaliers() * 8);
        double multiplicateur = 0.8 + (Math.random() * 0.4);
        return (int) (forceTotale * multiplicateur);
    }
}