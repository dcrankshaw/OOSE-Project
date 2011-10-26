import org.jpedal.PdfDecoder;
import javax.swing.*;

import org.jpedal.exception.PdfException;

public class jpedal_test {
    
    PdfDecoder decoder;

    public jpedal_test() {
	decoder = new PdfDecoder(true);
    }
    
    JFrame frame;
    pdfpanel display;

    private void read_pdf(String filename) throws PdfException{
	decoder.openPdfFile(filename);
	//for( int i = 0; i < 1<<30; i++ ) ;
	display.img = decoder.getPageAsImage(1);
	//for( int i = 0; i < 1<<30; i++ ) ;
    }
    
    public void run(String filename) throws PdfException {
	    frame = new JFrame("JPedal test");
	    display = new pdfpanel();
	    frame.getContentPane().add(display);
	    
	    read_pdf(filename);
	    
	    frame.pack();
	    frame.setVisible(true);
    }

    public static void main(String[] args) throws PdfException {
	if( args.length != 1 ) {
	    System.out.println("You must pass a file to open!");
	}
	jpedal_test test = new jpedal_test();
	test.run(args[0]);
    }
}