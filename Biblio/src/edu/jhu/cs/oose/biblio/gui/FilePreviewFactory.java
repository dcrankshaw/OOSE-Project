package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.pdf.PDFPreviewPanel;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.UnsupportedFiletypeException;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * Factory to create FilePreviewPanels.
 */
public class FilePreviewFactory {

	/**
	 * Creates a FilePreviewPanel capable of displaying the contents
	 * of the given file.
	 * @param file the file to preview
	 * @return a new FilePreviewPanel that displays the file
	 * @throws UnsupportedFiletypeException if the factory does not know how to handle
	 *  the file
	 */
	public static PreviewPanel createPreview(FileMetadata file) throws UnsupportedFiletypeException
	{
		// Zach says to use the visitor pattern here
		if( file instanceof PDFFileMetadata ) {
			return new PDFPreviewPanel((PDFFileMetadata)file);
		}
		throw new UnsupportedFiletypeException("This filetype is unsupported");
	}
}
