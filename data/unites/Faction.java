package data.unites;

import config.Config;
import data.architecture.Carte;
import data.architecture.QG;
import java.util.HashSet;
import java.util.Set;

public class Faction {
    private String nom;
    private int or = 300;
    private boolean estHumain;
    private boolean eliminee = false;
    private QG qg;

    private int nbSoldats = 0;
    private int nbArchers = 0;
    private int nbChevaliers = 0;

    private Set<String> casesExplorees = new HashSet<String>();

    public Faction(String nom, boolean estHumain) {
        this.nom = nom;
        this.estHumain = estHumain;
    }

    public void explorer(int centreL, int centreC, int maxL, int maxC) {
        int rayon = Config.RAYON_VISION;
        for (int l = centreL - rayon; l <= centreL + rayon; l++) {
            for (int c = centreC - rayon; c <= centreC + rayon; c++) {
                if (l >= 0 && l < maxL && c >= 0 && c < maxC) {
                    casesExplorees.add(l + "," + c);
                }
            }
        }
    }

    public boolean aExplore(int l, int c) {
        return casesExplorees.contains(l + "," + c);
    }

    public int getNombreDeCases(Carte carte) {
        int compteur = 0;
        for (int l = 0; l < carte.getHauteur(); l++) {
            for (int c = 0; c < carte.getLargeur(); c++) {
                if (carte.getCase(l, c).getProprietaire().equals(this.nom)) compteur++;
            }
        }
        return compteur;
    }

    public int calculerEnduranceTotale() {
        int hpQG = (qg != null) ? qg.getPv() : 0;
        return hpQG + (nbSoldats * 10) + (nbArchers * 8) + (nbChevaliers * 20);
    }

    public void subirDegats(int degats) {
        if (nbSoldats > 0)          nbSoldats--;
        else if (nbArchers > 0)     nbArchers--;
        else if (nbChevaliers > 0)  nbChevaliers--;
        else if (qg != null)        qg.setPv(Math.max(0, qg.getPv() - degats));
        if (calculerEnduranceTotale() <= 0) this.eliminee = true;
    }

    public void setEliminee(boolean b)  { this.eliminee = b; }
    public boolean isEliminee()         { return eliminee; }
    public boolean estHumain()          { return estHumain; }
    public String getNom()              { return nom; }
    public int getOr()                  { return or; }
    public void ajouterOr(int montant)  { this.or += montant; }
    public void retirerOr(int montant)  { this.or -= montant; }
    public QG getQG()                   { return qg; }
    public void setQG(QG qg)           { this.qg = qg; }
    public int getNbSoldats()           { return nbSoldats; }
    public void ajouterSoldat()         { nbSoldats++; }
    public void retirerSoldat()         { if (nbSoldats > 0) nbSoldats--; }
    public int getNbArchers()           { return nbArchers; }
    public void ajouterArcher()         { nbArchers++; }
    public void retirerArcher()         { if (nbArchers > 0) nbArchers--; }
    public int getNbChevaliers()        { return nbChevaliers; }
    public void ajouterChevalier()      { nbChevaliers++; }
    public void retirerChevalier()      { if (nbChevaliers > 0) nbChevaliers--; }
}