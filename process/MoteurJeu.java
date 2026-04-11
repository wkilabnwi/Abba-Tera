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
    private List<IAManager> iaManagers;

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

    private static final int SOIN_PAR_TOUR = 2;

    public MoteurJeu() {
        this.carte              = new Carte();
        this.unites             = new ArrayList<Unite>();
        this.batiments          = new ArrayList<Batiment>();
        this.factions           = new ArrayList<Faction>();
        this.deplacementManager = new DeplacementManager();
        this.economieManager    = new EconomieManager();
        this.victoryManager     = new VictoryManager();
        this.combatManager      = new CombatManager();
        this.diplomatieManager  = new DiplomatieManager();
        this.iaManagers         = new ArrayList<IAManager>();
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

        int derniereLigne   = Config.NB_LIGNES   - 2;
        int derniereColonne = Config.NB_COLONNES - 2;

        QG qgJoueur = new QG(1,            1,               "JOUEUR");
        QG qgIA1    = new QG(1,            derniereColonne,  "IA_1");
        QG qgIA2    = new QG(derniereLigne, 1,               "IA_2");
        QG qgIA3    = new QG(derniereLigne, derniereColonne,  "IA_3");

        joueur.setQG(qgJoueur);
        ia1.setQG(qgIA1);
        ia2.setQG(qgIA2);
        ia3.setQG(qgIA3);

        batiments.add(qgJoueur);
        batiments.add(qgIA1);
        batiments.add(qgIA2);
        batiments.add(qgIA3);

        iaManagers.add(new IAManager(ia1, qgJoueur.getLigne(), qgJoueur.getColonne()));
        iaManagers.add(new IAManager(ia2, qgJoueur.getLigne(), qgJoueur.getColonne()));
        iaManagers.add(new IAManager(ia3, qgJoueur.getLigne(), qgJoueur.getColonne()));

        Unite colon = new Unite(2, 1, "Colon");
        colon.setCamp("JOUEUR");
        unites.add(colon);
        uniteSelectionneeSurMap = colon;

        placerCreeps();
        mettreAJourBrouillard();
        setDernierMouvement("Bienvenue ! Deplacez votre Colon et fondez une ville avec S.");
    }

    private void placerCreeps() {
        java.util.Random rand = new java.util.Random();
        int placed   = 0;
        int attempts = 0;
        while (placed < 15 && attempts < 500) {
            attempts++;
            int l = rand.nextInt(carte.getHauteur());
            int c = rand.nextInt(carte.getLargeur());
            Case laCase = carte.getCase(l, c);
            if (laCase.getTypeTerrain().equals("EAU"))      continue;
            if (laCase.getTypeTerrain().equals("MONTAGNE")) continue;
            if (!laCase.getProprietaire().equals("NEUTRE")) continue;
            if (getUniteAt(l, c) != null)                   continue;
            if (getBatimentAt(l, c) != null)                continue;
            Unite creep = new Unite(l, c, "Creep");
            creep.setCamp("NEUTRE");
            unites.add(creep);
            placed++;
        }
    }

    private int[] trouverCaseLibreAdjacente(int l, int c) {
        int[] dl = {0, 1, 0, -1};
        int[] dc = {1, 0, -1, 0};
        for (int dir = 0; dir < 4; dir++) {
            int nl = l + dl[dir];
            int nc = c + dc[dir];
            if (!carte.estDansLaGrille(nl, nc)) continue;
            Case laCase = carte.getCase(nl, nc);
            if (laCase.getTypeTerrain().equals("EAU"))      continue;
            if (laCase.getTypeTerrain().equals("MONTAGNE")) continue;
            if (getUniteAt(nl, nc) != null)                 continue;
            if (getBatimentAt(nl, nc) != null)              continue;
            return new int[]{nl, nc};
        }
        return null;
    }

    public void passerTour() {
        for (Batiment b : batiments) {
            if (!(b instanceof QG)) continue;
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

        for (Batiment b : batiments) {
            if (!(b instanceof QG)) continue;
            QG qg = (QG) b;
            int rayon = qg.getRayonCulture();
            for (int dl = -rayon; dl <= rayon; dl++) {
                for (int dc = -rayon; dc <= rayon; dc++) {
                    int nl = qg.getLigne()   + dl;
                    int nc = qg.getColonne() + dc;
                    if (!carte.estDansLaGrille(nl, nc)) continue;
                    Case laCase = carte.getCase(nl, nc);
                    if (laCase.getProprietaire().equals("NEUTRE")) {
                        laCase.setProprietaire(qg.getProprietaire());
                    }
                }
            }
        }

        for (Unite u : unites) {
            u.resetDeplacement();
            if (!u.estMort() && !u.getType().equals("Creep")) {
                u.soigner(SOIN_PAR_TOUR);
            }
        }

        economieManager.precalculerOrTerritoire(factions, carte);
        for (Faction f : factions) {
            int gain = economieManager.calculerRevenuDuTour(f, carte, batiments, unites);
            setDernierMouvement(f.getNom() + " recoit " + gain + " or.");
        }

        for (IAManager ia : iaManagers) {
            QG qgJoueur = getFactionJoueur().getQG();
            if (qgJoueur != null) {
                ia.mettreAJourCible(qgJoueur.getLigne(), qgJoueur.getColonne());
            }
            ia.jouerTour(unites, carte, this);
        }

        mettreAJourBrouillard();

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
        int cibleL = cible.getLigne();
        int cibleC = cible.getColonne();

        String log = combatManager.executerCombat(attaquant, cible, caseCible);
        setDernierMouvement(log);

        if (cible.estMort()) {
            unites.remove(cible);
            if (!attaquant.estMort()) {
                attaquant.setLigne(cibleL);
                attaquant.setColonne(cibleC);
                caseCible.setProprietaire(attaquant.getCamp());
                attaquant.consommerDeplacement(attaquant.getPointsDeplacement());
            }
        }
        if (attaquant.estMort()) unites.remove(attaquant);
        mettreAJourBrouillard();
    }

    public void attaquerBatiment(Unite attaquant, int nL, int nC) {
        Batiment b = getBatimentAt(nL, nC);
        if (b == null) return;

        int dist = Math.abs(nL - attaquant.getLigne()) + Math.abs(nC - attaquant.getColonne());
        if (dist != 1) {
            setDernierMouvement("Trop loin pour attaquer !");
            return;
        }

        Case caseCible = carte.getCase(nL, nC);

        if (b instanceof QG) {
            QG qgCible = (QG) b;

            if (qgCible.aUneGarnison()) {
                Unite defenseur = qgCible.getPremierDefenseur();
                String log = combatManager.executerCombat(attaquant, defenseur, caseCible);
                setDernierMouvement("Garnison de " + qgCible.getNomVille() + " ! " + log);
                if (defenseur.estMort()) {
                    unites.remove(defenseur);
                    qgCible.retirerGarnison(defenseur);
                    if (!qgCible.aUneGarnison()) {
                        setDernierMouvement("Garnison eliminee ! Attaquez encore pour prendre " + qgCible.getNomVille() + " !");
                    }
                }
                if (attaquant.estMort()) {
                    unites.remove(attaquant);
                    uniteSelectionneeSurMap = null;
                }
                attaquant.consommerDeplacement(attaquant.getPointsDeplacement());

            } else {
                int degats = Math.max(1, attaquant.getForce() + new java.util.Random().nextInt(3) - 1);
                int bonusTerrain = caseCible.getBonusDefense();
                degats = Math.max(1, degats - bonusTerrain / 2);
                qgCible.setPv(Math.max(0, qgCible.getPv() - degats));
                setDernierMouvement("Assaut sur " + qgCible.getNomVille() + " ! -" + degats + " PV (" + qgCible.getPv() + "/" + qgCible.getPvMax() + ")");
                attaquant.consommerDeplacement(attaquant.getPointsDeplacement());

                if (qgCible.getPv() <= 0) {
                    String ancienCamp = qgCible.getProprietaire();
                    Faction ancienProprietaire = getFactionParNom(ancienCamp);
                    if (ancienProprietaire != null) {
                        ancienProprietaire.retirerVille(qgCible);
                    }
                    qgCible.setProprietaire(attaquant.getCamp());
                    qgCible.setPv(qgCible.getPvMax() / 2);
                    caseCible.setProprietaire(attaquant.getCamp());
                    getFactionParNom(attaquant.getCamp()).ajouterVille(qgCible);
                    attaquant.setLigne(nL);
                    attaquant.setColonne(nC);

                    for (int l = 0; l < carte.getHauteur(); l++) {
                        for (int c = 0; c < carte.getLargeur(); c++) {
                            Case laCase = carte.getCase(l, c);
                            if (laCase.getProprietaire().equals(ancienCamp)) {
                                laCase.setProprietaire(attaquant.getCamp());
                            }
                        }
                    }

                    for (Unite u : unites) {
                        if (u.getCamp().equals(ancienCamp)) {
                            u.setCamp(attaquant.getCamp());
                        }
                    }

                    for (Batiment bat : batiments) {
                        if (bat.getProprietaire().equals(ancienCamp)) {
                            bat.setProprietaire(attaquant.getCamp());
                        }
                    }

                    setDernierMouvement(attaquant.getType() + " capture " + qgCible.getNomVille() + " ! Tout le territoire de " + ancienCamp + " est annexe !");
                }

                if (attaquant.estMort()) {
                    unites.remove(attaquant);
                    uniteSelectionneeSurMap = null;
                }
            }

        } else {
            b.setProprietaire(attaquant.getCamp());
            caseCible.setProprietaire(attaquant.getCamp());
            attaquant.setLigne(nL);
            attaquant.setColonne(nC);
            attaquant.consommerDeplacement(1);
            setDernierMouvement(attaquant.getType() + " capture un batiment ennemi !");
        }

        mettreAJourBrouillard();
    }

    public void deplacerUniteSelectionnee(int nL, int nC) {
        if (uniteSelectionneeSurMap == null) return;

        Unite cibleUnite = getUniteAt(nL, nC);
        if (cibleUnite != null && !cibleUnite.getCamp().equals(uniteSelectionneeSurMap.getCamp())) {
            if (!diplomatieManager.sontAllies(uniteSelectionneeSurMap.getCamp(), cibleUnite.getCamp())) {
                int dist = Math.abs(nL - uniteSelectionneeSurMap.getLigne())
                         + Math.abs(nC - uniteSelectionneeSurMap.getColonne());
                if (dist <= uniteSelectionneeSurMap.getPortee()) {
                    lancerCombat(uniteSelectionneeSurMap, cibleUnite);
                } else {
                    setDernierMouvement("Cible hors de portee !");
                }
                return;
            }
        }

        Batiment batimentCible = getBatimentAt(nL, nC);
        if (batimentCible != null) {
            boolean estEnnemi = !batimentCible.getProprietaire().equals(uniteSelectionneeSurMap.getCamp())
                && !diplomatieManager.sontAllies(uniteSelectionneeSurMap.getCamp(), batimentCible.getProprietaire());
            if (estEnnemi) {
                attaquerBatiment(uniteSelectionneeSurMap, nL, nC);
            } else {
                setDernierMouvement("Un batiment allie bloque le passage !");
            }
            return;
        }

        deplacementManager.gererDeplacement(uniteSelectionneeSurMap, nL, nC, carte, this);
        mettreAJourBrouillard();
    }

    public void deplacerSoldatInventaire(int lig, int col) {
        if (typeUniteEnMain == null) return;
        Faction f = getFactionJoueur();
        Case laCase = carte.getCase(lig, col);
        boolean estMonTerritoire = laCase.getProprietaire().equals("JOUEUR");
        boolean pasDUnite        = (getUniteAt(lig, col) == null);
        boolean pasDeBatiment    = (getBatimentAt(lig, col) == null);
        boolean terrainValide    = !laCase.getTypeTerrain().equals("EAU")
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
            if (batimentEnAttente.equals("Mine"))    nouveau = new data.architecture.Mine(lig, col, "JOUEUR");
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

    public void mettreAJourBrouillard() {
        Faction joueur = getFactionJoueur();
        int maxL = carte.getHauteur();
        int maxC = carte.getLargeur();

        for (Unite u : unites) {
            if (u.getCamp().equals("JOUEUR")) {
                joueur.explorer(u.getLigne(), u.getColonne(), maxL, maxC);
            }
        }
        for (Batiment b : batiments) {
            if (b.getProprietaire().equals("JOUEUR")) {
                joueur.explorer(b.getLigne(), b.getColonne(), maxL, maxC);
            }
        }

        List<String> allies = diplomatieManager.getVisionsPartagees("JOUEUR", factions);
        for (Faction f : factions) {
            if (!allies.contains(f.getNom())) continue;
            for (Unite u : unites) {
                if (u.getCamp().equals(f.getNom())) {
                    joueur.explorer(u.getLigne(), u.getColonne(), maxL, maxC);
                }
            }
            for (Batiment b : batiments) {
                if (b.getProprietaire().equals(f.getNom())) {
                    joueur.explorer(b.getLigne(), b.getColonne(), maxL, maxC);
                }
            }
        }
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

    public Faction getFactionJoueur()                       { return factions.get(0); }
    public Faction getFactionIA()                           { return factions.get(1); }
    public List<Faction> getFactions()                      { return factions; }
    public Carte getCarte()                                 { return carte; }
    public List<Unite> getUnites()                          { return unites; }
    public List<Batiment> getBatiments()                    { return batiments; }
    public DiplomatieManager getDiplomatieManager()         { return diplomatieManager; }
    public CombatManager getCombatManager()                 { return combatManager; }
    public boolean isPartieTerminee()                       { return partieTerminee; }
    public String getMessageFinPartie()                     { return messageFinPartie; }

    public String getDernierMouvement() {
        String msg = dernierMouvement;
        this.dernierMouvement = "";
        return msg;
    }

    public void setDernierMouvement(String msg)             { this.dernierMouvement = msg; }
    public Unite getUniteSelectionneeSurMap()               { return uniteSelectionneeSurMap; }
    public void setUniteSelectionneeSurMap(Unite u)         { this.uniteSelectionneeSurMap = u; }
    public boolean aUniteEnMain()                           { return typeUniteEnMain != null; }
    public void setUniteSelectionnee(String t)              { this.typeUniteEnMain = t; }
    public List<Unite> getArmeeSelectionnee()               { return armeeSelectionnee; }
    public boolean estEnModeConstruction()                  { return batimentEnAttente != null; }
    public DeplacementManager getDeplacementManager()       { return deplacementManager; }
}