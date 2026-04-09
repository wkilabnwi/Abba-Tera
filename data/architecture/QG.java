package data.architecture;

public class QG extends Batiment {
    private String projetEnCours = null;
    private int toursRestants = 0;

    public QG(int l, int c, String proprietaire) {
        super(l, c, 100, proprietaire);
    }


    public void demarrerProduction(String type, int delai) {
        this.projetEnCours = type;
        this.toursRestants = delai;
    }


    public boolean avancerTour() {
        if (projetEnCours != null) {
            toursRestants--;
            return toursRestants <= 0;
        }
        return false;
    }

    public String getProjetEnCours() { return projetEnCours; }
    
    public void resetProjet() { 
        this.projetEnCours = null; 
        this.toursRestants = 0; 
    }
}