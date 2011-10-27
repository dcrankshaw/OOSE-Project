package edu.jhu.cs.oose.biblio.model.pdf;

import org.jpedal.exception.PdfException;

/**
 * Reads the actual file from disk and makes it suitable for display. This is done with
 * a factory so that we have an isolated and clear place to store all of the 
 * pdf reading and decoding logic (some of which will use a PDF reader library).
 */

public class PDFDocumentFactory {

	/** Reads a PDF from disk and creates a PDFFileContents object from it */
	public PDFFileContents getPDF(String filename) throws PdfException
	{
		return new PDFFileContents(filename);
	}
	
	
}
