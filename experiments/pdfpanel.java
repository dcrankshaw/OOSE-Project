import javax.swing.*;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Graphics;

public class pdfpanel extends JPanel {
    
    public Image img;
    
    public void paint(Graphics g) {
	g.drawImage(img, 0, 0, null);
    }
    
    public Dimension getPreferredSize() {
	if( img == null ) {
	    return new Dimension(100, 100);
	}
	else {
	    return new Dimension(img.getWidth(null), img.getHeight(null));
	}
    }
}