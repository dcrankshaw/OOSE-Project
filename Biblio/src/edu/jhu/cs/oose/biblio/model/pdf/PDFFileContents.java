package edu.jhu.cs.oose.biblio.model.pdf;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import edu.jhu.cs.oose.biblio.model.FileContents;

/**
 * Contains the contents of a PDF file
 * 
 */

public class PDFFileContents implements FileContents {

	// This variable controls which page of the PDF document is
	// used to generate the thumbnail
	private static final int THUMBNAIL_PAGE = 1;
	
	// This object is from the JPedal library
	// we use it to read the PDF file from disk
	private PdfDecoder decoder;
	
	// All the pages that have been read into memory so far
	private Map<Integer, Image> pages;
	
	// The preview of this document
	private Image thumbnail;
	
	public PDFFileContents(String filename) throws PdfException
	{
		pages = new HashMap<Integer, Image>();
		thumbnail = null;
		decoder = new PdfDecoder();
		decoder.openPdfFile(filename);
	}
	
	/**
	* Get the contents of the PDF file.
	* @return contents The contents.
	*/
	public FileContents getPDFData()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int search(String searchTerm) {
		// TODO implement this method
		return 0;
	}
	
	/**
	 * Gets the thumbnail, or preview, for this document.
	 * This may create it if necessary, which may take nontrivial time.
	 * @return the preview of the document
	 * @throws PdfException if there were errors reading or parsing
	 * the pdf document
	 */
	Image getThumbnail() throws PdfException {
		// if the thumbnail already exists, then just return it
		if( thumbnail != null ) {
			return thumbnail;
		}
		
		// We're going to generate the thumbnail from the first page
		// of the PDF document.  If that page is not already loaded,
		// then load it.
		if( !pages.containsKey(THUMBNAIL_PAGE) ) {
			Image firstPage = decoder.getPageAsImage(THUMBNAIL_PAGE);
			pages.put(THUMBNAIL_PAGE, firstPage);
		}
		
		// Create the thumbnail from the proper page of the PDF document
		Image firstPage = pages.get(THUMBNAIL_PAGE);
		// TODO we should agree on some method of determining the preview /
		// thumbnail size, so I'm just using magic numbers for now.
		thumbnail = firstPage.getScaledInstance(850/4, -1100/4, Image.SCALE_DEFAULT);
		return thumbnail;
	}

}
