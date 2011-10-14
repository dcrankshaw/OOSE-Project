package edu.jhu.cs.oose.biblio.gui;

import javax.swing.JRadioButton;

/**
 * These are laid out in a grid on the ImportPanel. They each display
 * a preview of the file, allow the user to add tags, and decide
 * what to do with the underlying file on disk on import.
 */
public class FileImportCell {
	
	/** the preview of the file to display to the user */
	public PreviewPanel preview;
	/** The list of tags this file has already been tagged with */
	public TagsListPanel tagsPanel;
	/** All of the associated meta data that accompanies this file */
	public FileMetadata file;
	/** Whether this file is currently selected to add tags to */
	public boolean isSelected;
	/** Value determines what the user wants to do with the file. */
	public CopyStatus copyStatus;
	private JRadioButton copyStatusButton;
	
	/**
	 * Adds a tag to the FileMetadata for the file this cell is previewing
	 * @param tag The tag the user wants to add to the FileMetadata
	 */
	public void addTag(Tag tag){}
	

}
