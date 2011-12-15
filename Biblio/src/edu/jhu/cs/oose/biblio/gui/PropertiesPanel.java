package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * Interface to display a list of the properties associated with a file
 */
public class PropertiesPanel extends JPanel {
	/** The list of tags the file is associated with */
	private TagsListPanel tagsListPane;

	/** The file whose properties are being displayed */
	public FileMetadata file;

	/** Creates a new panel that displays the properties of nothing in particular. */
	public PropertiesPanel() {
		this.tagsListPane = new TagsListPanel();
		
		this.setLayout(new BorderLayout());
		this.add(tagsListPane, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a panel that displays the properties of the given file
	 * @param f the file to display properties of
	 */
	public PropertiesPanel(FileMetadata f) {
		this();
		setFile(f);
	}
	
	/**
	 * Sets this panel to display the properties of the given file
	 * @param f the file to display properties of
	 */
	public void setFile(FileMetadata f) {
		this.file = f;
		tagsListPane.setTags(this.file);
	}
	
	/**
	 * Returns the file whose properties are displayed in this panel.
	 * @return the file whose properties are displayed in this panel.
	 */
	public FileMetadata getFile() {
		return this.file;
	}
	
	/** Removes all the listeners used by the TagsListPane before dying */
	public void cleanup() {
		this.tagsListPane.setTags(null);
	}
}
