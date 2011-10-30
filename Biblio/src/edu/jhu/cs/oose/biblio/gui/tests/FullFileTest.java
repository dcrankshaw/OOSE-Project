package edu.jhu.cs.oose.biblio.gui.tests;

import javax.swing.JFrame;

import org.jpedal.exception.PdfException;

import edu.jhu.cs.oose.biblio.gui.ScrollFilePanel;
import edu.jhu.cs.oose.biblio.gui.pdf.PDFFullFilePanel;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;

public class FullFileTest {

	public static void main(String[] args) throws PdfException {
		if( args.length == 0 ) {
			System.err.println("Please specify a filename on the command line.");
			return;
		}
		JFrame frame = new JFrame("Full File Test");
		ScrollFilePanel scrollPanel = new ScrollFilePanel();
		PDFFileContents contents = new PDFFileContents(args[0]);
		PDFFullFilePanel pdfPanel = new PDFFullFilePanel(contents);
		
		frame.setContentPane(scrollPanel);
		frame.pack();
		frame.setVisible(true);
		scrollPanel.setContents(pdfPanel);
		frame.pack();
	}
}
