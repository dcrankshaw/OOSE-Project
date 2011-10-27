package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.pdf.PDFPreviewPanel;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.UnsupportedFiletypeException;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;

public class FilePreviewFactory {

	public static PreviewPanel createPreview(FileMetadata file) throws UnsupportedFiletypeException
	{
		//TODO: comment justifying use of switch block
		switch(file.getType())
		{
		case PDF: return new PDFPreviewPanel((PDFFileContents) file.getContents());
		default: throw new UnsupportedFiletypeException("This filetype is unsupported");
		
		
		}
			
	}
	
	
	
}
