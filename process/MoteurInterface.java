package process;

import data.architecture.Batiment;
import data.unites.Faction;
import data.unites.Unite;
import java.util.List;


public interface MoteurInterface {

    void passerTour();
    int getTourActuel();

    void deplacerUniteSelectionnee(int l, int c);
    void deplacerSoldatInventaire(int l, int c);
    void placerBatiment(int l, int c);
    void fonderVille();

    Unite getUniteSelectionneeSurMap();
    void setUniteSelectionneeSurMap(Unite u);
    void cycleUniteSuivante();
    void ajouterUniteAuCombat(Unite u);

    Unite getUniteAt(int l, int c);
    Batiment getBatimentAt(int l, int c);
    List<Batiment> getBatiments();
    boolean aUniteEnMain();
    boolean estEnModeConstruction();

    Faction getFactionJoueur();
    List<Faction> getFactions();

    DiplomatieManager getDiplomatieManager();
    List<Faction> getPropositionsAlliance();
    void accepterAlliance(Faction f);
    void refuserAlliance(Faction f);
    void proposerAllianceJoueur(Faction f);
    void trahir(Faction f);

    String getDernierMouvement();
    boolean isPartieTerminee();
    void preparerConstruction(String nomBatiment);
    String getMessageFinPartie();
    data.architecture.Carte getCarte();
List<data.unites.Unite> getUnites();
}