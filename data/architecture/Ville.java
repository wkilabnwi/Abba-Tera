package data.architecture;

import java.util.ArrayList;
import java.util.List;

public class Ville extends Batiment {
    private String nom;
    private int population;
    private int nourriture;
    private int productionAccumulee;
    private String itemEnCours;

    public Ville(int l, int c, String proprietaire, String nom) {
        super(l, c, 100, proprietaire);
        this.nom = nom;
        this.population = 1;
        this.nourriture = 0;
        this.productionAccumulee = 0;
    }

    public void simulerTour() {

        this.nourriture += 2;
        if (this.nourriture >= 10 * population) {
            population++;
            nourriture = 0;
        }
    }


    public String getNom() { return nom; }
    public int getPopulation() { return population; }
    public void ajouterProduction(int p) { productionAccumulee += p; }
}