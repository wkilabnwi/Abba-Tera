package data.architecture;

public class Mine extends Batiment {
    public Mine(int l, int c, String proprietaire) {
        super(l, c, proprietaire);
        this.setPvMax(40);
        this.setPv(40);
    }
}