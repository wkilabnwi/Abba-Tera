import ihm.MenuPrincipal;
import process.MoteurJeu;

public class Main {
    public static void main(String[] args) {
        MoteurJeu moteur = new MoteurJeu();
        MenuPrincipal menu = new MenuPrincipal(moteur);
        Thread gameThread = new Thread(menu);
        gameThread.start();
    }
}