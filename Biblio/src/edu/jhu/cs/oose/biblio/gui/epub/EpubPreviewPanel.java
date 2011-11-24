package edu.jhu.cs.oose.biblio.gui.epub;

import edu.jhu.cs.oose.biblio.gui.PreviewPanel;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileContents;

public class EpubPreviewPanel extends PreviewPanel {
	
	/**
	 * The contents of the file
	 */
	EpubFileContents contents;
	
	/**
	 * Create a new instance with the provided file contents
	 * @param c the contents of the file
	 */
	
	EpubPreviewPanel(EpubFileContents c)
	{
		contents = c;
	}

}
