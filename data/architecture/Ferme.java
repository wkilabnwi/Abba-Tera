package data.architecture;

public class Ferme extends Batiment {
    private int productionOr;

    public Ferme(int l, int c, String proprietaire) {
        super(l, c, 30, proprietaire);
        this.productionOr = 20;
    }

    public int getProductionOr() { return productionOr; }
}
