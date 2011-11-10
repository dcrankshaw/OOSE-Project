package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * An object that can create new views of a given file,
 * i.e. a tab containing the full view or a properties window.
 */
public interface FileViewFactory {
	/**
	 * Creates a view of the given file.
	 * @param file the file to display info on.
	 * @return a view displaying info on that file
	 */
	public FileView newView(FileMetadata file);
}
