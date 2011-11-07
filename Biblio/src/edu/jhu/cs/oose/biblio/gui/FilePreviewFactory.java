package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.pdf.PDFPreviewPanel;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.UnsupportedFiletypeException;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

public class FilePreviewFactory {

	public static PreviewPanel createPreview(FileMetadata file) throws UnsupportedFiletypeException
	{
		if( file instanceof PDFFileMetadata ) {
			return new PDFPreviewPanel((PDFFileContents)file.getContents());
		}
		throw new UnsupportedFiletypeException("This filetype is unsupported");
	}
}
