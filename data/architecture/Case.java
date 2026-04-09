package data.architecture;

public class Case {
    private int ligne;
    private int colonne;
    private String typeTerrain;
    private String proprietaire;

    public Case(int ligne, int colonne, String type) {
        this.ligne = ligne;
        this.colonne = colonne;
        this.typeTerrain = type;
        this.proprietaire = "NEUTRE";
    }

    public int getBonusDefense() {
        if (typeTerrain.equals("FORET")) {
            return 2;
        } else if (typeTerrain.equals("MONTAGNE")) {
            return 4;
        }
        return 0;
    }

    public int getLigne()       { return ligne; }
    public int getColonne()     { return colonne; }
    public String getTypeTerrain()  { return typeTerrain; }
    public String getProprietaire() { return proprietaire; }
    public void setProprietaire(String p) { this.proprietaire = p; }
}
