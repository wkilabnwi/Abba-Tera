package process;

import config.Config;
import data.architecture.Batiment;
import data.architecture.Carte;
import data.architecture.Case;
import data.architecture.Caserne;
import data.architecture.Ferme;
import data.architecture.QG;
import data.unites.Faction;
import data.unites.Unite;
import java.util.ArrayList;
import java.util.List;

public class MoteurJeu {

    private Carte carte;
    private List<Unite> unites;
    private List<Batiment> batiments;
    private List<Faction> factions;

    private DeplacementManager deplacementManager;
    private EconomieManager economieManager;
    private VictoryManager victoryManager;
    private CombatManager combatManager;
    private DiplomatieManager diplomatieManager;

    private String dernierMouvement = "";
    private Unite uniteSelectionneeSurMap = null;
    private List<Unite> armeeSelectionnee = new ArrayList<Unite>();
    private String typeUniteEnMain = null;
    private String batimentEnAttente = null;

    private List<Faction> propositionsAlliance = new ArrayList<Faction>();
    private boolean partieTerminee = false;
    private int tourActuel = 1;
    private String messageFinPartie = "";

    public MoteurJeu() {
        this.carte = new Carte();
        this.unites = new ArrayList<Unite>();
        this.batiments = new ArrayList<Batiment>();
        this.factions = new ArrayList<Faction>();
        this.deplacementManager = new DeplacementManager();
        this.economieManager = new EconomieManager();
        this.victoryManager = new VictoryManager();
        this.combatManager = new CombatManager();
        this.diplomatieManager = new DiplomatieManager();
        initialiserJeu();
    }

    private void initialiserJeu() {
        Faction joueur = new Faction("JOUEUR", true);
        Faction ia1    = new Faction("IA_1",   false);
        Faction ia2    = new Faction("IA_2",   false);
        Faction ia3    = new Faction("IA_3",   false);
        factions.add(joueur);
        factions.add(ia1);
        factions.add(ia2);
        factions.add(ia3);

        
        QG qgJoueur = new QG(1,                    1,                      "JOUEUR");
        QG qgIA1    = new QG(1,                    Config.NB_COLONNES - 2, "IA_1");
        QG qgIA2    = new QG(Config.NB_LIGNES - 2, 1,                      "IA_2");
        QG qgIA3    = new QG(Config.NB_LIGNES - 2, Config.NB_COLONNES - 2, "IA_3");

        joueur.setQG(qgJoueur);
        ia1.setQG(qgIA1);
        ia2.setQG(qgIA2);
        ia3.setQG(qgIA3);

        batiments.add(qgJoueur);
        batiments.add(qgIA1);
        batiments.add(qgIA2);
        batiments.add(qgIA3);

        
        Unite colon = new Unite(2, 1, "Colon");
        colon.setCamp("JOUEUR");
        unites.add(colon);
        uniteSelectionneeSurMap = colon;

        setDernierMouvement("Bienvenue ! Deplacez votre Colon et fondez une ville avec S.");
    }

    private int[] trouverCaseLibreAdjacente(int l, int c) {
        int[] dl = {0, 1, 0, -1};
        int[] dc = {1, 0, -1, 0};
        for (int dir = 0; dir < 4; dir++) {
            int nl = l + dl[dir];
            int nc = c + dc[dir];
            if (!carte.estDansLaGrille(nl, nc)) continue;
            Case laCase = carte.getCase(nl, nc);
            if (laCase.getTypeTerrain().equals("EAU")) continue;
            if (laCase.getTypeTerrain().equals("MONTAGNE")) continue;
            if (getUniteAt(nl, nc) != null) continue;
            if (getBatimentAt(nl, nc) != null) continue;
            return new int[]{nl, nc};
        }
        return null;
    }

    public void passerTour() {
        
        for (Batiment b : batiments) {
            if (b instanceof QG) {
                QG qg = (QG) b;
                qg.gererTour(carte);

                if (qg.avancerProduction()) {
                    String projet = qg.getProjetEnCours();
                    if (projet.equals("Grenier") || projet.equals("Marche")) {
                        qg.ajouterBatimentEffectue(projet);
                        setDernierMouvement(qg.getNomVille() + ": " + projet + " termine !");
                    } else {
                        int[] pos = trouverCaseLibreAdjacente(qg.getLigne(), qg.getColonne());
                        if (pos != null) {
                            Unite n = new Unite(pos[0], pos[1], projet);
                            n.setCamp(qg.getProprietaire());
                            unites.add(n);
                            setDernierMouvement(projet + " produit par " + qg.getNomVille() + " !");
                        } else {
                            setDernierMouvement("Pas de place pour " + projet + " !");
                        }
                    }
                    qg.resetProjet();
                }
            }
        }

        
        for (Batiment b : batiments) {
            if (b instanceof QG) {
                QG qg = (QG) b;
                int rayon = qg.getRayonCulture();
                for (int dl = -rayon; dl <= rayon; dl++) {
                    for (int dc = -rayon; dc <= rayon; dc++) {
                        int nl = qg.getLigne() + dl;
                        int nc = qg.getColonne() + dc;
                        if (!carte.estDansLaGrille(nl, nc)) continue;
                        Case laCase = carte.getCase(nl, nc);
                        if (laCase.getProprietaire().equals("NEUTRE")) {
                            laCase.setProprietaire(qg.getProprietaire());
                        }
                    }
                }
            }
        }

        
        for (Unite u : unites) {
            u.resetDeplacement();
        }

        
        for (Faction f : factions) {
            int gain = economieManager.calculerRevenuDuTour(f, carte, batiments, unites);
            setDernierMouvement(f.getNom() + " recoit " + gain + " or.");
        }

        
        List<Unite> morts = new ArrayList<Unite>();
        for (Unite u : unites) {
            if (u.estMort()) morts.add(u);
        }
        unites.removeAll(morts);

        
        for (Faction f : factions) {
            victoryManager.verifierElimination(f);
        }

        if (victoryManager.verifierDefaite(getFactionJoueur())) {
            partieTerminee = true;
            messageFinPartie = "DEFAITE - Toutes vos villes sont tombees !";
        } else if (victoryManager.verifierDominationMilitaire(factions, getFactionJoueur())) {
            partieTerminee = true;
            messageFinPartie = "VICTOIRE MILITAIRE - Tous les ennemis sont elimines !";
        } else if (victoryManager.verifierDominationTerritoriale(carte, getFactionJoueur())) {
            partieTerminee = true;
            messageFinPartie = "VICTOIRE TERRITORIALE - Vous controlez 75% de la carte !";
        }

        tourActuel++;
        setDernierMouvement("--- Tour " + tourActuel + " ---");
        cycleUniteSuivante();
    }

    public int getTourActuel() { return tourActuel; }

    public void lancerCombat(Unite attaquant, Unite cible) {
        Case caseCible = carte.getCase(cible.getLigne(), cible.getColonne());
        String log = combatManager.executerCombat(attaquant, cible, caseCible);
        setDernierMouvement(log);
        if (cible.estMort())     unites.remove(cible);
        if (attaquant.estMort()) unites.remove(attaquant);
    }

    public void lancerAssautGroupe(int nL, int nC) {
        if (armeeSelectionnee.isEmpty()) return;
        Faction cible = getFactionEnnemieNonAlliee();
        if (cible == null) {
            setDernierMouvement("Aucun ennemi a portee !");
            return;
        }
        ihm.FenetreCombat fc = new ihm.FenetreCombat(null, getFactionJoueur(), cible, combatManager, this);
        fc.setVisible(true);
        armeeSelectionnee.clear();
    }

    public Faction getFactionEnnemieNonAlliee() {
        for (Faction f : factions) {
            if (f.getNom().equals("JOUEUR")) continue;
            if (f.isEliminee()) continue;
            if (diplomatieManager.sontAllies("JOUEUR", f.getNom())) continue;
            return f;
        }
        return null;
    }

    public void deplacerUniteSelectionnee(int nL, int nC) {
        if (uniteSelectionneeSurMap == null) return;

        Unite cible = getUniteAt(nL, nC);
        if (cible != null && !cible.getCamp().equals(uniteSelectionneeSurMap.getCamp())) {
            if (!diplomatieManager.sontAllies(uniteSelectionneeSurMap.getCamp(), cible.getCamp())) {
                
                int dist = Math.abs(nL - uniteSelectionneeSurMap.getLigne())
                         + Math.abs(nC - uniteSelectionneeSurMap.getColonne());
                if (dist <= uniteSelectionneeSurMap.getPortee()) {
                    lancerCombat(uniteSelectionneeSurMap, cible);
                } else {
                    setDernierMouvement("Cible hors de portee !");
                }
                return;
            }
        }

        deplacementManager.gererDeplacement(uniteSelectionneeSurMap, nL, nC, carte, this);
    }

    public void deplacerSoldatInventaire(int lig, int col) {
        if (typeUniteEnMain == null) return;
        Faction f = getFactionJoueur();
        Case laCase = carte.getCase(lig, col);
        boolean estMonTerritoire = laCase.getProprietaire().equals("JOUEUR");
        boolean pasDUnite = (getUniteAt(lig, col) == null);
        boolean pasDeBatiment = (getBatimentAt(lig, col) == null);
        boolean terrainValide = !laCase.getTypeTerrain().equals("EAU")
                             && !laCase.getTypeTerrain().equals("MONTAGNE");
        if (estMonTerritoire && pasDUnite && pasDeBatiment && terrainValide) {
            boolean ok = false;
            if (typeUniteEnMain.equals("Soldat")    && f.getNbSoldats()    > 0) ok = true;
            if (typeUniteEnMain.equals("Archer")    && f.getNbArchers()    > 0) ok = true;
            if (typeUniteEnMain.equals("Chevalier") && f.getNbChevaliers() > 0) ok = true;
            if (ok) {
                Unite u = new Unite(lig, col, typeUniteEnMain);
                u.setCamp("JOUEUR");
                unites.add(u);
                if (typeUniteEnMain.equals("Soldat"))    f.retirerSoldat();
                if (typeUniteEnMain.equals("Archer"))    f.retirerArcher();
                if (typeUniteEnMain.equals("Chevalier")) f.retirerChevalier();
                setDernierMouvement(typeUniteEnMain + " deploye !");
            } else {
                setDernierMouvement("Plus de " + typeUniteEnMain + " en reserve !");
            }
        } else {
            if (!estMonTerritoire)   setDernierMouvement("Ce n'est pas votre territoire !");
            else if (!pasDeBatiment) setDernierMouvement("Case occupee par un batiment !");
            else                     setDernierMouvement("Placement impossible ici.");
        }
        typeUniteEnMain = null;
    }

    public void placerBatiment(int lig, int col) {
        if (batimentEnAttente == null) return;
        Case laCase = carte.getCase(lig, col);
        if (laCase.getProprietaire().equals("JOUEUR")
                && !laCase.getTypeTerrain().equals("EAU")
                && !laCase.getTypeTerrain().equals("MONTAGNE")
                && getUniteAt(lig, col) == null
                && getBatimentAt(lig, col) == null) {
            Batiment nouveau = null;
            if (batimentEnAttente.equals("Caserne")) nouveau = new Caserne(lig, col, "JOUEUR");
            if (batimentEnAttente.equals("Ferme"))   nouveau = new Ferme(lig, col, "JOUEUR");
            if (nouveau != null) {
                batiments.add(nouveau);
                setDernierMouvement(batimentEnAttente + " construit !");
                batimentEnAttente = null;
            }
        } else {
            setDernierMouvement("Impossible de construire ici !");
        }
    }

    public void preparerConstruction(String nom) {
        this.batimentEnAttente = nom;
        setDernierMouvement("Mode construction : cliquez sur une zone bleue.");
    }

    public void ajouterUniteInventaire(String type) {
        Faction f = getFactionJoueur();
        if (type.equals("Soldat"))         f.ajouterSoldat();
        else if (type.equals("Archer"))    f.ajouterArcher();
        else if (type.equals("Chevalier")) f.ajouterChevalier();
        else { System.err.println("Type inconnu : " + type); return; }
        setDernierMouvement(type + " achete ! Appuyez sur A pour deployer.");
    }

    public void ajouterUniteAuCombat(Unite u) {
        if (!armeeSelectionnee.contains(u)) {
            armeeSelectionnee.add(u);
            setDernierMouvement("Unite prete pour l'assaut !");
        }
    }

    public void ajouterPropositionAlliance(Faction ia) {
        if (!propositionsAlliance.contains(ia)) propositionsAlliance.add(ia);
    }

    public List<Faction> getPropositionsAlliance() { return propositionsAlliance; }

    public void accepterAlliance(Faction ia) {
        diplomatieManager.proposerAlliance(getFactionJoueur(), ia);
        propositionsAlliance.remove(ia);
        setDernierMouvement("Alliance acceptee avec " + ia.getNom() + " !");
    }

    public void refuserAlliance(Faction ia) {
        propositionsAlliance.remove(ia);
        setDernierMouvement("Alliance refusee avec " + ia.getNom() + ".");
    }

    public void proposerAllianceJoueur(Faction ia) {
        diplomatieManager.proposerAlliance(getFactionJoueur(), ia);
        setDernierMouvement("Alliance proposee a " + ia.getNom() + ".");
    }

    public void trahir(Faction ia) {
        diplomatieManager.trahir(getFactionJoueur(), ia);
        setDernierMouvement("Vous trahissez " + ia.getNom() + " !");
    }

    public void fonderVille() {
        Unite sel = getUniteSelectionneeSurMap();

        if (sel == null || !sel.getType().equals("Colon")) {
            setDernierMouvement("Seul un Colon peut fonder une ville !");
            return;
        }

        int l = sel.getLigne();
        int c = sel.getColonne();
        Case laCase = carte.getCase(l, c);

        if (laCase.getTypeTerrain().equals("EAU") || laCase.getTypeTerrain().equals("MONTAGNE")) {
            setDernierMouvement("Terrain impraticable !");
            return;
        }

        if (getBatimentAt(l, c) != null) {
            setDernierMouvement("Un batiment existe deja ici !");
            return;
        }

        unites.remove(sel);
        uniteSelectionneeSurMap = null;

        QG nouvelleVille = new QG(l, c, sel.getCamp());
        getFactionParNom(sel.getCamp()).ajouterVille(nouvelleVille);
        batiments.add(nouvelleVille);

        laCase.setProprietaire(sel.getCamp());
        setDernierMouvement("Ville " + nouvelleVille.getNomVille() + " fondee ! (Q pour gerer)");
    }

    public int getPuissanceTotale(String camp, String type) {
        int total = 0;
        for (Unite u : unites) {
            if (u.getCamp().equals(camp) && u.getType().equals(type)) total++;
        }
        for (Faction f : factions) {
            if (f.getNom().equals(camp)) {
                if (type.equals("Soldat"))    total += f.getNbSoldats();
                if (type.equals("Archer"))    total += f.getNbArchers();
                if (type.equals("Chevalier")) total += f.getNbChevaliers();
                break;
            }
        }
        return total;
    }

    public Unite getUniteAt(int l, int c) {
        for (Unite u : unites) {
            if (u.isEnGarnison()) continue;
            if (u.getLigne() == l && u.getColonne() == c) return u;
        }
        return null;
    }

    public Batiment getBatimentAt(int l, int c) {
        for (Batiment b : batiments) {
            if (b.getLigne() == l && b.getColonne() == c) return b;
        }
        return null;
    }

    public Faction getFactionParNom(String nom) {
        for (Faction f : factions) {
            if (f.getNom().equals(nom)) return f;
        }
        return null;
    }

    public void cycleUniteSuivante() {
        this.uniteSelectionneeSurMap = null;
        if (unites.isEmpty()) return;
        for (Unite u : unites) {
            if (u.isEnGarnison()) continue;
            if (u.getCamp().equals("JOUEUR") && u.canMove()) {
                this.uniteSelectionneeSurMap = u;
                setDernierMouvement("Au tour de : " + u.getType() + " en (" + u.getLigne() + "," + u.getColonne() + ")");
                return;
            }
        }
        setDernierMouvement("Toutes les unites ont agi. Appuyez ENTREE pour finir le tour.");
    }

    public Faction getFactionJoueur()        { return factions.get(0); }
    public Faction getFactionIA()            { return factions.get(1); }
    public List<Faction> getFactions()       { return factions; }
    public Carte getCarte()                  { return carte; }
    public List<Unite> getUnites()           { return unites; }
    public List<Batiment> getBatiments()     { return batiments; }
    public DiplomatieManager getDiplomatieManager() { return diplomatieManager; }
    public CombatManager getCombatManager()  { return combatManager; }
    public boolean isPartieTerminee()        { return partieTerminee; }
    public String getMessageFinPartie()      { return messageFinPartie; }

    public String getDernierMouvement() {
        String msg = dernierMouvement;
        this.dernierMouvement = "";
        return msg;
    }

    public void setDernierMouvement(String msg) { this.dernierMouvement = msg; }
    public Unite getUniteSelectionneeSurMap()       { return uniteSelectionneeSurMap; }
    public void setUniteSelectionneeSurMap(Unite u) { this.uniteSelectionneeSurMap = u; }
    public boolean aUniteEnMain()                   { return typeUniteEnMain != null; }
    public void setUniteSelectionnee(String t)      { this.typeUniteEnMain = t; }
    public List<Unite> getArmeeSelectionnee()       { return armeeSelectionnee; }
    public boolean estEnModeConstruction()          { return batimentEnAttente != null; }
    public DeplacementManager getDeplacementManager() { return deplacementManager; }
}