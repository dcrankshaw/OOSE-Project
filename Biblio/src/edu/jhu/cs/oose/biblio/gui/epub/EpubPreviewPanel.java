package edu.jhu.cs.oose.biblio.gui.epub;

import edu.jhu.cs.oose.biblio.gui.PreviewPanel;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileContents;

public class EpubPreviewPanel extends PreviewPanel {
	
	EpubFileContents contents;
	
	
	
	EpubPreviewPanel(EpubFileContents c)
	{
		contents = c;
	}

}
