package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.pdf.PDFPreviewPanel;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.FileTypes;
import edu.jhu.cs.oose.biblio.model.UnsupportedFiletypeException;

public class FilePreviewFactory {

	public static PreviewPanel createPreview(FileMetadata file) throws UnsupportedFiletypeException
	{
		//TODO: comment justifying use of switch block
		switch(file.getType())
		{
		case PDF: return new PDFPreviewPanel();
		default: throw new UnsupportedFiletypeException("This filetype is unsupported");
		
		
		}
			
	}
	
	
	
}
