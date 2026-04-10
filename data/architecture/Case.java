package data.architecture;

public class Case {
    private int ligne;
    private int colonne;
    private String typeTerrain;
    private String proprietaire;
    private String ressource;   
    private boolean riviere;

    public Case(int ligne, int colonne, String type) {
        this.ligne = ligne;
        this.colonne = colonne;
        this.typeTerrain = type;
        this.proprietaire = "NEUTRE";
        this.ressource = null;
        this.riviere = false;
    }

    
    public int getRendementNourriture() {
        int base = 0;
        if (typeTerrain.equals("PLAINE"))   base = 2;
        if (typeTerrain.equals("FORET"))    base = 1;
        if (typeTerrain.equals("EAU"))      base = 3;
        if (typeTerrain.equals("MONTAGNE")) base = 0;
        if (riviere)                        base += 1;
        if ("BLE".equals(ressource))        base += 2;
        if ("POISSON".equals(ressource))    base += 2;
        return base;
    }

    public int getRendementProduction() {
        int base = 0;
        if (typeTerrain.equals("PLAINE"))   base = 1;
        if (typeTerrain.equals("FORET"))    base = 2;
        if (typeTerrain.equals("MONTAGNE")) base = 3;
        if ("FER".equals(ressource))        base += 2;
        if ("CHEVAUX".equals(ressource))    base += 1;
        return base;
    }

    public int getRendementOr() {
        int base = 0;
        if (typeTerrain.equals("PLAINE"))   base = 1;
        if (typeTerrain.equals("FORET"))    base = 0;
        if (typeTerrain.equals("EAU"))      base = 1;
        if (riviere)                        base += 1;
        return base;
    }

    public int getBonusDefense() {
        if (typeTerrain.equals("FORET"))    return 2;
        if (typeTerrain.equals("MONTAGNE")) return 4;
        return 0;
    }

    
    public int getLigne()               { return ligne; }
    public int getColonne()             { return colonne; }
    public String getTypeTerrain()      { return typeTerrain; }
    public String getProprietaire()     { return proprietaire; }
    public void setProprietaire(String p) { this.proprietaire = p; }
    public String getRessource()        { return ressource; }
    public void setRessource(String r)  { this.ressource = r; }
    public boolean hasRiviere()         { return riviere; }
    public void setRiviere(boolean r)   { this.riviere = r; }
}