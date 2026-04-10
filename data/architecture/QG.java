package data.architecture;

import java.util.ArrayList;
import java.util.List;

public class QG extends Batiment {
    private String nomVille;
    private int population = 1;
    private int nourriture = 0;
    private int culture = 0;
    private int rayonCulture = 1;

    private java.util.List<data.unites.Unite> garnison = new java.util.ArrayList<data.unites.Unite>();

    private String projetEnCours = "Aucun";
    private int toursRestants = 0;

    private List<String> batimentsConstruits = new ArrayList<String>();

    /**
     * Each worker is assigned to a tile "l,c".
     */
    private List<String> casesAssignees = new ArrayList<String>();

    private static final String[] NOMS_VILLES = {
        "Lutece", "Massilia", "Burdigala", "Lugdunum", "Aureliani",
        "Caesaraugusta", "Carthago", "Tingis", "Hippo", "Leptis"
    };
    private static int compteurNom = 0;

    public QG(int l, int c, String prop) {
        super(l, c, prop);
        this.setPvMax(100);
        this.setPv(100);
        this.nomVille = NOMS_VILLES[compteurNom % NOMS_VILLES.length];
        compteurNom++;
        this.nourriture = 5;
    }

    public void gererTour(Carte carte) {
        int nourritureGagnee = 1;
        for (String cle : casesAssignees) {
            String[] parts = cle.split(",");
            int tl = Integer.parseInt(parts[0]);
            int tc = Integer.parseInt(parts[1]);
            Case laCase = carte.getCase(tl, tc);
            if (laCase != null) {
                nourritureGagnee += laCase.getRendementNourriture();
            }
        }
        if (hasBuilding("Grenier")) nourritureGagnee += 2;

        int conso = population * 2;
        nourriture += (nourritureGagnee - conso);

        int seuilCroissance = 10 + population * 10;
        if (nourriture >= seuilCroissance) {
            population++;
            nourriture = 0;
            assignerMeilleureCaseLibre(carte);
        } else if (nourriture < 0) {
            if (population > 1) {
                population--;
                if (!casesAssignees.isEmpty()) casesAssignees.remove(casesAssignees.size() - 1);
            }
            nourriture = 0;
        }

        culture += 1 + batimentsConstruits.size();
        int seuilCulture = 10 * rayonCulture * rayonCulture;
        if (culture >= seuilCulture) {
            rayonCulture++;
            culture = 0;
        }
    }

    private void assignerMeilleureCaseLibre(Carte carte) {
        int bestScore = -1;
        String bestCle = null;
        for (int dl = -rayonCulture; dl <= rayonCulture; dl++) {
            for (int dc = -rayonCulture; dc <= rayonCulture; dc++) {
                if (dl == 0 && dc == 0) continue;
                int tl = getLigne() + dl;
                int tc = getColonne() + dc;
                String cle = tl + "," + tc;
                if (casesAssignees.contains(cle)) continue;
                if (!carte.estDansLaGrille(tl, tc)) continue;
                Case laCase = carte.getCase(tl, tc);
                int score = laCase.getRendementNourriture() + laCase.getRendementProduction();
                if (score > bestScore) { bestScore = score; bestCle = cle; }
            }
        }
        if (bestCle != null) casesAssignees.add(bestCle);
    }

    
    public void gererTour() {
        int nourritureGagnee = 2;
        if (hasBuilding("Grenier")) nourritureGagnee += 2;
        int conso = population * 2;
        nourriture += (nourritureGagnee - conso);
        int seuil = 10 + population * 10;
        if (nourriture >= seuil) { population++; nourriture = 0; }
        else if (nourriture < 0) { if (population > 1) population--; nourriture = 0; }
        culture += 1;
        int seuilCulture = 10 * rayonCulture * rayonCulture;
        if (culture >= seuilCulture) { rayonCulture++; culture = 0; }
    }

    public boolean hasBuilding(String nom)          { return batimentsConstruits.contains(nom); }
    public void ajouterBatimentEffectue(String nom) { if (!batimentsConstruits.contains(nom)) batimentsConstruits.add(nom); }

    public void setProjetEnCours(String nom, int tours) { this.projetEnCours = nom; this.toursRestants = tours; }
    public boolean avancerProduction() {
        if (!projetEnCours.equals("Aucun")) { toursRestants--; return toursRestants <= 0; }
        return false;
    }
    public void resetProjet() { this.projetEnCours = "Aucun"; this.toursRestants = 0; }

    public void assignerCase(String cle)   { if (!casesAssignees.contains(cle)) casesAssignees.add(cle); }
    public void desassignerCase(String cle){ casesAssignees.remove(cle); }
    public boolean estAssignee(String cle) { return casesAssignees.contains(cle); }
    public int getNbTravailleurs()         { return casesAssignees.size(); }

    public java.util.List<data.unites.Unite> getGarnison()     { return garnison; }
    public void ajouterGarnison(data.unites.Unite u)           { if (!garnison.contains(u)) garnison.add(u); }
    public void retirerGarnison(data.unites.Unite u)           { garnison.remove(u); }
    public boolean aUneGarnison()                              { return !garnison.isEmpty(); }
    public data.unites.Unite getPremierDefenseur()             { return garnison.isEmpty() ? null : garnison.get(0); }

    public String getNomVille()     { return nomVille; }
    public int getPopulation()      { return population; }
    public int getNourriture()      { return nourriture; }
    public int getCulture()         { return culture; }
    public int getRayonCulture()    { return rayonCulture; }
    public String getProjetEnCours(){ return projetEnCours; }
    public int getToursRestants()   { return toursRestants; }
    public List<String> getCasesAssignees()      { return casesAssignees; }
    public List<String> getBatimentsConstruits() { return batimentsConstruits; }

    
    public void setFermiers(int f)  {}
    public void setPecheurs(int p)  {}
    public int getNbFermiers()      { return casesAssignees.size(); }
    public int getNbPecheurs()      { return 0; }
    public void ajouterFermier()    {}
}