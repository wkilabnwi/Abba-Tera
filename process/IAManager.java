package process;

import data.architecture.Carte;
import data.architecture.Case;
import data.architecture.QG;
import data.unites.Faction;
import data.unites.Unite;
import java.util.List;
import java.util.Random;

public class IAManager {

    private Faction faction;
    private Random random = new Random();
    private int targetLigne;
    private int targetColonne;
    private int tourDepuisRecrutement = 0;
    private int tourTotal = 0;

    private static final int INTERVALLE_RECRUTEMENT = 10;
    private static final int TOURS_AVANT_AGRESSION  = 8;
    private static final int MAX_UNITES_PAR_TOUR    = 2;

    public IAManager(Faction faction, int targetLigne, int targetColonne) {
        this.faction       = faction;
        this.targetLigne   = targetLigne;
        this.targetColonne = targetColonne;
    }

    public void jouerTour(List<Unite> unites, Carte carte, MoteurJeu moteur) {
        if (faction.isEliminee()) return;

        tourTotal++;
        tourDepuisRecrutement++;

        if (tourDepuisRecrutement >= INTERVALLE_RECRUTEMENT) {
            recruter(unites, carte, moteur);
            tourDepuisRecrutement = 0;
        }

        int unitesActees = 0;
        for (int i = 0; i < unites.size(); i++) {
            if (unitesActees >= MAX_UNITES_PAR_TOUR) break;
            Unite u = unites.get(i);
            if (!u.getCamp().equals(faction.getNom())) continue;
            if (!u.canMove()) continue;
            if (u.isEnGarnison()) continue;

            boolean attaque = false;
            if (tourTotal >= TOURS_AVANT_AGRESSION) {
                attaque = attaquerSiAdjacent(u, unites, carte, moteur);
            }
            if (!attaque) {
                deplacer(u, carte, moteur);
            }
            unitesActees++;
        }

        avancerProduction(moteur);
    }

    private void recruter(List<Unite> unites, Carte carte, MoteurJeu moteur) {
        if (faction.getOr() < 60) return;
        for (QG qg : faction.getVilles()) {
            int[] pos = trouverCaseLibreAdjacente(qg.getLigne(), qg.getColonne(), carte, moteur);
            if (pos != null) {
                Unite u = new Unite(pos[0], pos[1], choisirType());
                u.setCamp(faction.getNom());
                unites.add(u);
                faction.retirerOr(40);
                return;
            }
        }
    }

    private String choisirType() {
        int r = random.nextInt(4);
        if (r == 0) return "Archer";
        if (r == 1) return "Chevalier";
        return "Soldat";
    }

    private boolean attaquerSiAdjacent(Unite u, List<Unite> unites, Carte carte, MoteurJeu moteur) {
        int[] dl = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        int portee = u.getPortee();

        for (int dir = 0; dir < 4; dir++) {
            for (int dist = 1; dist <= portee; dist++) {
                int nl = u.getLigne() + dl[dir] * dist;
                int nc = u.getColonne() + dc[dir] * dist;
                Unite cible = moteur.getUniteAt(nl, nc);
                if (cible == null) continue;
                if (cible.getCamp().equals(faction.getNom())) continue;
                if (moteur.getDiplomatieManager().sontAllies(faction.getNom(), cible.getCamp())) continue;
                Case caseCible = carte.getCase(nl, nc);
                if (caseCible == null) continue;
                String log = moteur.getCombatManager().executerCombat(u, cible, caseCible);
                moteur.setDernierMouvement("[" + faction.getNom() + "] " + log);
                if (cible.estMort()) unites.remove(cible);
                if (u.estMort())     { unites.remove(u); return true; }
                return true;
            }
        }
        return false;
    }

    private void deplacer(Unite u, Carte carte, MoteurJeu moteur) {
        if (tourTotal < TOURS_AVANT_AGRESSION) {
            if (random.nextInt(3) == 0) return;
        }

        int[] dl = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        int bestL = -1, bestC = -1;
        int bestDist = distanceVersCible(u.getLigne(), u.getColonne());

        for (int dir = 0; dir < 4; dir++) {
            int nl = u.getLigne() + dl[dir];
            int nc = u.getColonne() + dc[dir];
            if (!carte.estDansLaGrille(nl, nc)) continue;
            Case dest = carte.getCase(nl, nc);
            if (dest.getTypeTerrain().equals("EAU"))      continue;
            if (dest.getTypeTerrain().equals("MONTAGNE")) continue;
            if (moteur.getUniteAt(nl, nc) != null)        continue;
            if (moteur.getBatimentAt(nl, nc) != null)     continue;
            if (!moteur.getDiplomatieManager().aLeDroitDePassage(faction.getNom(), dest.getProprietaire())) continue;
            int dist = distanceVersCible(nl, nc);
            if (dist < bestDist) {
                bestDist = dist;
                bestL = nl;
                bestC = nc;
            }
        }

        if (bestL != -1) {
            Case dest = carte.getCase(bestL, bestC);
            if (!dest.getProprietaire().equals(faction.getNom())
                && !moteur.getDiplomatieManager().sontAllies(faction.getNom(), dest.getProprietaire())) {
                dest.setProprietaire(faction.getNom());
            }
            u.setLigne(bestL);
            u.setColonne(bestC);
            u.consommerDeplacement(1);
        }
    }

    private void avancerProduction(MoteurJeu moteur) {
        if (tourTotal < 5) return;
        for (QG qg : faction.getVilles()) {
            if (qg.getProjetEnCours().equals("Aucun") && faction.getOr() >= 60) {
                qg.setProjetEnCours("Soldat", 3);
                faction.retirerOr(40);
            }
        }
    }

    private int[] trouverCaseLibreAdjacente(int l, int c, Carte carte, MoteurJeu moteur) {
        int[] dl = {0, 1, 0, -1};
        int[] dc = {1, 0, -1, 0};
        for (int dir = 0; dir < 4; dir++) {
            int nl = l + dl[dir];
            int nc = c + dc[dir];
            if (!carte.estDansLaGrille(nl, nc)) continue;
            Case laCase = carte.getCase(nl, nc);
            if (laCase.getTypeTerrain().equals("EAU"))      continue;
            if (laCase.getTypeTerrain().equals("MONTAGNE")) continue;
            if (moteur.getUniteAt(nl, nc) != null)          continue;
            if (moteur.getBatimentAt(nl, nc) != null)       continue;
            return new int[]{nl, nc};
        }
        return null;
    }

    private int distanceVersCible(int l, int c) {
        return Math.abs(l - targetLigne) + Math.abs(c - targetColonne);
    }

    public Faction getFaction() { return faction; }
}