package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

public interface FileViewFactory {
	public FileView newView(FileMetadata file);
}
