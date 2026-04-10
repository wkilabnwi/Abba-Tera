package data.architecture;

public abstract class Batiment {
    private int ligne, colonne;
    private int pv, pvMax;
    private String proprietaire;

public Batiment(int l, int c, String proprietaire) {
        this.ligne = l;
        this.colonne = c;
        this.proprietaire = proprietaire;
        this.pv = 10; 
        this.pvMax = 10;
    }

    
    public Batiment(int l, int c, int pv, String proprietaire) {
        this.ligne = l;
        this.colonne = c;
        this.pv = pv;
        this.pvMax = pv;
        this.proprietaire = proprietaire;
    }

    public int getPv()                      { return pv; }
    public void setPv(int pv)               { this.pv = pv; }
    public String getProprietaire()         { return proprietaire; }
    public void setProprietaire(String p)   { this.proprietaire = p; }
    public int getLigne()                   { return ligne; }
    public int getColonne()                 { return colonne; }
    public int getPvMax() { return pvMax; }
    public void setPvMax(int pvMax) { this.pvMax = pvMax; }
}
