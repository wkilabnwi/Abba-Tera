package data.unites;

public class Unite {
    private int ligne, colonne, pv, pvMax, force;
    private int pointsDeplacement;
    private int pointsDeplacementMax;
    private int portee;
    private String type;
    private String camp;
    private boolean enGarnison = false;

    public Unite(int l, int c, String type) {
        this.ligne = l;
        this.colonne = c;
        this.type = type;
        this.camp = "JOUEUR";

        switch (type) {
            case "Soldat":
                this.pvMax = 10;
                this.force = 3;
                this.pointsDeplacementMax = 1;
                this.portee = 1;
                break;
            case "Archer":
                this.pvMax = 8;
                this.force = 5;
                this.pointsDeplacementMax = 1;
                this.portee = 2;
                break;
            case "Chevalier":
                this.pvMax = 20;
                this.force = 7;
                this.pointsDeplacementMax = 2;
                this.portee = 1;
                break;
            case "Colon":
                this.pvMax = 5;
                this.force = 0;
                this.pointsDeplacementMax = 1;
                this.portee = 1;
                break;
            default:
                this.pvMax = 10;
                this.force = 2;
                this.pointsDeplacementMax = 1;
                this.portee = 1;
                break;
        }
        this.pv = this.pvMax;
        this.pointsDeplacement = this.pointsDeplacementMax;
    }

    public void recevoirDegats(int d) {
        this.pv = Math.max(0, this.pv - d);
    }

    public boolean estMort() {
        return this.pv <= 0;
    }

    public boolean canMove() {
        return pointsDeplacement > 0;
    }

    public void consommerDeplacement(int cout) {
        this.pointsDeplacement = Math.max(0, this.pointsDeplacement - cout);
    }

    public void resetDeplacement() {
        this.pointsDeplacement = this.pointsDeplacementMax;
    }

    
    public void setABouge(boolean b) {
        if (b) this.pointsDeplacement = 0;
        else   this.pointsDeplacement = this.pointsDeplacementMax;
    }

    public int getPv()                      { return pv; }
    public int getPvMax()                   { return pvMax; }
    public int getForce()                   { return force; }
    public String getType()                 { return type; }
    public String getCamp()                 { return camp; }
    public int getLigne()                   { return ligne; }
    public int getColonne()                 { return colonne; }
    public void setLigne(int l)             { ligne = l; }
    public void setColonne(int c)           { colonne = c; }
    public void setCamp(String camp)        { this.camp = camp; }
    public boolean isEnGarnison()           { return enGarnison; }
    public void setEnGarnison(boolean b)    { this.enGarnison = b; }
    public int getPortee()                  { return portee; }
    public int getPointsDeplacement()       { return pointsDeplacement; }
    public int getPointsDeplacementMax()    { return pointsDeplacementMax; }
}