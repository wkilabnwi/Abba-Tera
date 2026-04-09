package data.architecture;

public abstract class Batiment {
    private int ligne, colonne;
    private int pv;
    private String proprietaire;

    public Batiment(int l, int c, int pv, String proprietaire) {
        this.ligne = l;
        this.colonne = c;
        this.pv = pv;
        this.proprietaire = proprietaire;
    }

    public int getPv()                      { return pv; }
    public void setPv(int pv)               { this.pv = pv; }
    public String getProprietaire()         { return proprietaire; }
    public void setProprietaire(String p)   { this.proprietaire = p; }
    public int getLigne()                   { return ligne; }
    public int getColonne()                 { return colonne; }
}
