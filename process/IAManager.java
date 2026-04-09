package process;

// import data.architecture.Carte;
// import data.architecture.Case;
// import data.unites.Faction;
// import data.unites.Unite;
// import java.util.List;
// import java.util.Random;

// public class IAManager {

//     private Faction faction;
//     private Random random = new Random();
//     private int targetLigne;
//     private int targetColonne;
//     private int tourDepuisDernierRecrutement = 0;
//     private static final int INTERVALLE_RECRUTEMENT = 4;

//     public IAManager(Faction faction, int targetLigne, int targetColonne) {
//         this.faction = faction;
//         this.targetLigne = targetLigne;
//         this.targetColonne = targetColonne;
//     }

//     public void jouerTour(List<Unite> unites, Carte carte, MoteurJeu moteur) {
//         if (faction.isEliminee()) return;
//         tourDepuisDernierRecrutement++;
//         if (tourDepuisDernierRecrutement >= INTERVALLE_RECRUTEMENT) {
//             recruter(unites, carte, moteur);
//             tourDepuisDernierRecrutement = 0;
//         }
//         deplacerUneUnite(unites, carte, moteur);
//         attaquerSiAdjacent(unites, moteur);
//         tenterDiplomatie(moteur);
//     }

//     private void recruter(List<Unite> unites, Carte carte, MoteurJeu moteur) {
//         if (faction.getOr() < 40) return;
//         for (int l = 0; l < carte.getHauteur(); l++) {
//             for (int c = 0; c < carte.getLargeur(); c++) {
//                 Case laCase = carte.getCase(l, c);
//                 if (laCase.getProprietaire().equals(faction.getNom())
//                         && moteur.getUniteAt(l, c) == null
//                         && moteur.getBatimentAt(l, c) == null) {
//                     Unite u = new Unite(l, c, "Soldat");
//                     u.setCamp(faction.getNom());
//                     unites.add(u);
//                     faction.retirerOr(40);
//                     return;
//                 }
//             }
//         }
//     }

//     private void deplacerUneUnite(List<Unite> unites, Carte carte, MoteurJeu moteur) {
//         int[] dl = {-1, 1, 0, 0};
//         int[] dc = {0, 0, -1, 1};
//         for (Unite u : unites) {
//             if (!u.getCamp().equals(faction.getNom())) continue;
//             if (!u.canMove()) continue;
//             int bestL = -1, bestC = -1;
//             int bestDist = distanceVersCible(u.getLigne(), u.getColonne());
//             for (int dir = 0; dir < 4; dir++) {
//                 int nl = u.getLigne() + dl[dir];
//                 int nc = u.getColonne() + dc[dir];
//                 if (!carte.estDansLaGrille(nl, nc)) continue;
//                 Case dest = carte.getCase(nl, nc);
//                 if (dest.getTypeTerrain().equals("EAU")) continue;
//                 if (dest.getTypeTerrain().equals("MONTAGNE")) continue;
//                 if (moteur.getUniteAt(nl, nc) != null) continue;
//                 if (moteur.getBatimentAt(nl, nc) != null) continue;
//                 if (!moteur.getDiplomatieManager().aLeDroitDePassage(faction.getNom(), dest.getProprietaire())) continue;
//                 int dist = distanceVersCible(nl, nc);
//                 if (dist < bestDist) {
//                     bestDist = dist;
//                     bestL = nl;
//                     bestC = nc;
//                 }
//             }
//             if (bestL != -1) {
//                 Case dest = carte.getCase(bestL, bestC);
//                 if (!moteur.getDiplomatieManager().sontAllies(faction.getNom(), dest.getProprietaire())) {
//                     dest.setProprietaire(faction.getNom());
//                 }
//                 u.setLigne(bestL);
//                 u.setColonne(bestC);
//                 u.setABouge(true);
//                 return;
//             }
//         }
//     }

//     private void attaquerSiAdjacent(List<Unite> unites, MoteurJeu moteur) {
//         int[] dl = {-1, 1, 0, 0};
//         int[] dc = {0, 0, -1, 1};
//         for (Unite attaquant : unites) {
//             if (!attaquant.getCamp().equals(faction.getNom())) continue;
//             for (int dir = 0; dir < 4; dir++) {
//                 int nl = attaquant.getLigne() + dl[dir];
//                 int nc = attaquant.getColonne() + dc[dir];
//                 Unite cible = moteur.getUniteAt(nl, nc);
//                 if (cible == null) continue;
//                 if (cible.getCamp().equals(faction.getNom())) continue;
//                 if (!cible.getCamp().equals("JOUEUR")) continue;
//                 if (moteur.getDiplomatieManager().sontAllies(faction.getNom(), cible.getCamp())) continue;
//                 moteur.lancerCombat(attaquant, cible);
//                 return;
//             }
//         }
//     }

//     private void tenterDiplomatie(MoteurJeu moteur) {
//         if (random.nextInt(10) == 0) {
//             Faction joueur = moteur.getFactionJoueur();
//             if (!moteur.getDiplomatieManager().sontAllies(faction, joueur)) {
//                 moteur.ajouterPropositionAlliance(faction);
//             }
//         }
//     }

//     private int distanceVersCible(int l, int c) {
//         return Math.abs(l - targetLigne) + Math.abs(c - targetColonne);
//     }

//     public Faction getFaction() { return faction; }
// }