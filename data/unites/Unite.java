package data.unites;

public class Unite {
    private int ligne, colonne, pv, pvMax, force;
    private String type;
    private String camp;
    private boolean aBouge = false;

    public Unite(int l, int c, String type) {
        this.ligne = l;
        this.colonne = c;
        this.type = type;
        this.camp = "JOUEUR";

        switch (type) {
            case "Soldat":
                this.pvMax = 10;
                this.force = 3;
                break;
            case "Archer":
                this.pvMax = 8;
                this.force = 5;
                break;
            case "Chevalier":
                this.pvMax = 20;
                this.force = 7;
                break;
            case "Colon":
                this.pvMax = 5;
                this.force = 0;
                break;
            default:
                this.pvMax = 10;
                this.force = 2;
                break;
        }
        this.pv = this.pvMax;
    }

    public void recevoirDegats(int d) {
        this.pv = Math.max(0, this.pv - d);
    }

    public boolean estMort() {
        return this.pv <= 0;
    }


    public int getPv()       { return pv; }
    public int getPvMax()    { return pvMax; }
    public int getForce()    { return force; }
    public String getType()  { return type; }
    public String getCamp()  { return camp; }
    public int getLigne()    { return ligne; }
    public int getColonne()  { return colonne; }
    public void setLigne(int l)    { ligne = l; }
    public void setColonne(int c)  { colonne = c; }
    public boolean canMove()       { return !aBouge; }
    public void setABouge(boolean b) { aBouge = b; }
    public void setCamp(String camp) { this.camp = camp; }
}