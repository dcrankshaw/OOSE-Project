package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * A visitor class used to construct Preview Panels.
 * There should be one method on this for each kind of PreviewPanel.
 * Each FileMetadata implementation should know which one to call.
 */
public interface FilePreviewVisitor {
	
	/**
	 * Creates a PreviewPanel capable of displaying a PDF file
	 * @param data the file to display
	 * @param bkmk the place in the file to preview
	 * @return a PreviewPanel capable of displaying a PDF file
	 */
	public PreviewPanel makePDFPreviewPanel(PDFFileMetadata data, Bookmark bkmk);

	/**
	 * Creates a PreviewPanel capable of displaying an Epub file
	 * @param data the file to display
	 * @param bkmk the location in the file to preview
	 * @return a PreviewPanel capable of displaying an Epub file
	 */
	public PreviewPanel makeEpubPreviewPanel(EpubFileMetadata data, Bookmark bkmk);
}
