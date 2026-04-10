import ihm.MenuPrincipal;
import process.MoteurJeu;

public class Main {
    public static void main(String[] args) {
        MoteurJeu moteur = new MoteurJeu();
        new MenuPrincipal(moteur);
    }
}