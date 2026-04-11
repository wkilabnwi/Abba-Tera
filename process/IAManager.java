package process;

import data.architecture.Batiment;
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
    private int tourDepuisRecrutement = 0;
    private int tourTotal = 0;

    private static final int INTERVALLE_RECRUTEMENT = 8;
    private static final int TOURS_AVANT_AGRESSION  = 6;
    private static final int MAX_UNITES_PAR_TOUR    = 3;

    public IAManager(Faction faction, int targetLigne, int targetColonne) {
        this.faction = faction;
    }

    public void mettreAJourCible(int targetLigne, int targetColonne) {
    }

    public void jouerTour(List<Unite> unites, Carte carte, MoteurJeu moteur) {
        if (faction.isEliminee()) return;

        tourTotal++;
        tourDepuisRecrutement++;

        if (tourDepuisRecrutement >= INTERVALLE_RECRUTEMENT) {
            recruter(unites, carte, moteur);
            tourDepuisRecrutement = 0;
        }

        avancerProduction(moteur);

        if (tourTotal < TOURS_AVANT_AGRESSION) return;

        int unitesActees = 0;
        for (int i = 0; i < unites.size(); i++) {
            if (unitesActees >= MAX_UNITES_PAR_TOUR) break;
            Unite u = unites.get(i);
            if (!u.getCamp().equals(faction.getNom())) continue;
            if (!u.canMove()) continue;
            if (u.isEnGarnison()) continue;

            boolean attaque = attaquerSiAdjacent(u, unites, carte, moteur);
            if (!attaque) {
                deplacerVersNearestEnnemi(u, unites, carte, moteur);
            }
            unitesActees++;
        }
    }

    private void recruter(List<Unite> unites, Carte carte, MoteurJeu moteur) {
        if (faction.getOr() < 40) return;
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
        int r = random.nextInt(6);
        if (r == 0) return "Archer";
        if (r == 1) return "Chevalier";
        if (r == 2) return "Archer";
        return "Soldat";
    }

    private boolean attaquerSiAdjacent(Unite u, List<Unite> unites, Carte carte, MoteurJeu moteur) {
        int[] dl = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        int portee = u.getPortee();

        for (int dir = 0; dir < 4; dir++) {
            for (int dist = 1; dist <= portee; dist++) {
                int nl = u.getLigne()   + dl[dir] * dist;
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

        for (int dir = 0; dir < 4; dir++) {
            int nl = u.getLigne() + dl[dir];
            int nc = u.getColonne() + dc[dir];
            Batiment b = moteur.getBatimentAt(nl, nc);
            if (b == null) continue;
            if (b.getProprietaire().equals(faction.getNom())) continue;
            if (moteur.getDiplomatieManager().sontAllies(faction.getNom(), b.getProprietaire())) continue;
            moteur.attaquerBatiment(u, nl, nc);
            if (u.estMort()) unites.remove(u);
            return true;
        }

        return false;
    }

    private void deplacerVersNearestEnnemi(Unite u, List<Unite> unites, Carte carte, MoteurJeu moteur) {
        int[] cible = trouverCibleLaPlusProche(u, unites, moteur);
        if (cible == null) return;

        int targetL = cible[0];
        int targetC = cible[1];

        int[] dl = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        int bestL    = -1;
        int bestC    = -1;
        int bestDist = distance(u.getLigne(), u.getColonne(), targetL, targetC);

        for (int dir = 0; dir < 4; dir++) {
            int nl = u.getLigne()   + dl[dir];
            int nc = u.getColonne() + dc[dir];
            if (!carte.estDansLaGrille(nl, nc)) continue;
            Case dest = carte.getCase(nl, nc);
            if (dest.getTypeTerrain().equals("EAU"))      continue;
            if (dest.getTypeTerrain().equals("MONTAGNE")) continue;
            if (moteur.getUniteAt(nl, nc) != null)        continue;
            if (moteur.getBatimentAt(nl, nc) != null)     continue;
            int dist = distance(nl, nc, targetL, targetC);
            if (dist < bestDist) {
                bestDist = dist;
                bestL    = nl;
                bestC    = nc;
            }
        }

        if (bestL != -1) {
            u.setLigne(bestL);
            u.setColonne(bestC);
            u.consommerDeplacement(1);
        }
    }

    private int[] trouverCibleLaPlusProche(Unite u, List<Unite> unites, MoteurJeu moteur) {
        int bestDist   = Integer.MAX_VALUE;
        int[] bestCible = null;

        for (Unite cible : unites) {
            if (cible.getCamp().equals(faction.getNom())) continue;
            if (cible.getCamp().equals("NEUTRE")) continue;
            if (moteur.getDiplomatieManager().sontAllies(faction.getNom(), cible.getCamp())) continue;
            int d = distance(u.getLigne(), u.getColonne(), cible.getLigne(), cible.getColonne());
            if (d < bestDist) {
                bestDist   = d;
                bestCible  = new int[]{cible.getLigne(), cible.getColonne()};
            }
        }

        for (Batiment b : moteur.getBatiments()) {
            if (b.getProprietaire().equals(faction.getNom())) continue;
            if (b.getProprietaire().equals("NEUTRE")) continue;
            if (moteur.getDiplomatieManager().sontAllies(faction.getNom(), b.getProprietaire())) continue;
            int d = distance(u.getLigne(), u.getColonne(), b.getLigne(), b.getColonne());
            if (d < bestDist) {
                bestDist  = d;
                bestCible = new int[]{b.getLigne(), b.getColonne()};
            }
        }

        return bestCible;
    }

    private void avancerProduction(MoteurJeu moteur) {
        if (tourTotal < 3) return;
        for (QG qg : faction.getVilles()) {
            if (!qg.getProjetEnCours().equals("Aucun")) continue;
            if (faction.getOr() < 40) continue;

            int unitesSurCarte = 0;
            for (Batiment b : moteur.getBatiments()) {
                if (b.getProprietaire().equals(faction.getNom())) unitesSurCarte++;
            }

            String projet;
            int cout;
            int tours;
            int r = random.nextInt(5);
            if (r == 0 && faction.getOr() >= 120) {
                projet = "Chevalier"; cout = 120; tours = 5;
            } else if (r == 1 && faction.getOr() >= 60) {
                projet = "Archer"; cout = 60; tours = 3;
            } else {
                projet = "Soldat"; cout = 40; tours = 2;
            }

            if (faction.getOr() >= cout) {
                qg.setProjetEnCours(projet, tours);
                faction.retirerOr(cout);
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

    private int distance(int l1, int c1, int l2, int c2) {
        return Math.abs(l1 - l2) + Math.abs(c1 - c2);
    }

    public Faction getFaction() { return faction; }
}