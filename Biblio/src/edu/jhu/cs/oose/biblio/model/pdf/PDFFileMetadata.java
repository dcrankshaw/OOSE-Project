package edu.jhu.cs.oose.biblio.model.pdf;

import java.awt.Rectangle;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.grouping.SearchType;
import org.jpedal.objects.PdfPageData;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileContents;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
* Contains PDF specific metadata on top of that stored in FileMetadata
*/
@Entity
@Table( name = "PDF_FILEMETADATA" )
public class PDFFileMetadata extends FileMetadata {
	
	/**
	 * Creates a new empty object.  This is here so that
	 * Hibernate can fill in all the data.
	 * Use the other constructor instead
	 */
	@SuppressWarnings("unused")
	private PDFFileMetadata() {
		super();
	}
	
	/**
	 * Creates a new for the contents on disk with default initialization for the other fields.
	 * This gets a primary key for this object, so there should be an open transaction
	 * @param path the path to the file contents residing on disk
	 */
	public PDFFileMetadata(String path) {
		super(path);
	} 
	
    /**
	 * The contents of this file, if they have been read
	 */
	@Transient
	private PDFFileContents contents;

    /**
	 * Creates a new PDFFileMetadata, initialized with the given arguments
	 * @param date the last time file was opened
	 * @param timesOpened the number of times it has been opened
	 * @param path the path to the contents on disk
	 * @param fileTags the tags that should be applied already (NOT copied)
	 */
	public PDFFileMetadata(Date date, int timesOpened, String path,
			Set<Tag> fileTags) {
		super(date, timesOpened, path, fileTags);
		//this.contents = null;
	}

	@Override
	public FileContents getContents() {
		if (null == contents) {
			try {
				contents = new PDFFileContents(getPathToFile());
			} catch (PdfException e) {
				// TODO there should be a better way to handle this,
				return null;
			}
		}
		return contents;
	}

	/**
	 * Some source taken from:
	 * http://www.jpedal.org/gplSrc/org/jpedal/examples/text/FindTextInRectangle.java.html
	 * Searches a document page by page for the number of a times a string appears
	 * in the document.
	 * @param searchTerm The string to search for
	 * @return the number of times the string occurs
	 */
	@Override 
	//TODO Zach suggested not to throw Exception
	public int searchText(String searchTerm) throws Exception {

		PdfDecoder decodePdf;
		//String[] searchAllWords = searchTerm.split(" ");
		/** The number of times the search term has been found */
		int num_results = 0;
		try {
			decodePdf = new PdfDecoder(false);
			decodePdf.setExtractionMode(PdfDecoder.TEXT);
			PdfDecoder.init(true);

			decodePdf.openPdfFile(this.getPathToFile());
		}
		catch (Exception e) {
			// Log error somewhere
			throw new Exception("Error decoding PDF");
		}
		/*checks whether the PDF is encrypted */
		if ((decodePdf.isEncrypted() && (!decodePdf.isPasswordSupplied()))
				&& (!decodePdf.isExtractionAllowed())) {
			throw new PdfException("Encrypted PDF");
		}
		else {
			int start = 1;
			int end = decodePdf.getPageCount();
			try {
				/*Iterate through all pages, searching each page for the search term*/
				for(int page = start; page <= end; page++)
				{
					decodePdf.decodePage(page);
					PdfGroupingAlgorithms grouping = decodePdf.getGroupingObject();
					if(grouping != null)
					{
						/* search entire page (rectangle covering entire page) for the search term
						 * This bases the search geographically on the page instead of on position in the file*/
						PdfPageData currentPageData = decodePdf.getPdfPageData();
						int x1 = currentPageData.getMediaBoxX(page);
						int x2 = currentPageData.getMediaBoxWidth(page) + x1;
						
						int y2 = currentPageData.getMediaBoxY(page);
			            int y1 = currentPageData.getMediaBoxHeight(page) + y2;
			            float[] results = null;
			            try
			            {
			            	/*Allow for search term to span multiple lines */
			            	results = grouping.findText(
			            			new Rectangle(x1, y2, x2-x1, y1-y2), 
			            			page, new String[] {searchTerm},
			            			SearchType.MUTLI_LINE_RESULTS);
			            }
			            catch (PdfException e)
			            {
			            	decodePdf.closePdfFile();
			            	throw new Exception("Searching error");
			            }
			            
			            if(results != null)
			            {
			            	/* Divide by 5 because the results are 4 coordinates for each search match
			            	   taken from example code */
			            	num_results += results.length / 5;
			            }		
					}
				}	
			}
			catch (Exception e)
			{
				throw new PdfException("Error searching PDF");
			}
		}
		return num_results;
	}
	
//	//TODO need to discuss how to normalize the 
//	public float normalize(int freq){
//		
//		return density;
//		
//	}
	
}
