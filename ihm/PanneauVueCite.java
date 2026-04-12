package ihm;

import data.architecture.Case;
import data.architecture.QG;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import process.MoteurInterface;

public class PanneauVueCite extends JPanel {
    private MoteurInterface moteur;
    private QG qg;

    public PanneauVueCite(MoteurInterface moteur, QG qg) {
        this.moteur = moteur;
        this.qg = qg;
        this.setPreferredSize(new Dimension(200, 200));
        this.setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int lCenter = qg.getLigne();
        int cCenter = qg.getColonne();
        int size = 60; 

        
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int l = lCenter + i;
                int c = cCenter + j;
                
                
                Case laCase = moteur.getCarte().getCase(l, c);
                if (laCase != null) {
                    
                    if (laCase.getTypeTerrain().equals("EAU")) g.setColor(Color.BLUE);
                    else if (laCase.getTypeTerrain().equals("MONTAGNE")) g.setColor(Color.GRAY);
                    else g.setColor(new Color(34, 139, 34)); 
                    
                    g.fillRect((j+1)*size, (i+1)*size, size, size);
                    g.setColor(Color.DARK_GRAY);
                    g.drawRect((j+1)*size, (i+1)*size, size, size);
                }
                
                
                if (i == 0 && j == 0) {
                    g.setColor(Color.WHITE);
                    g.fillRect((j+1)*size + 15, (i+1)*size + 15, 30, 30);
                    g.setColor(Color.BLACK);
                    g.drawString("CITY", (j+1)*size + 18, (i+1)*size + 35);
                }
            }
        }
    }
}