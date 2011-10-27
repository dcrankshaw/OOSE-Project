package edu.jhu.cs.oose.biblio.gui;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * These are laid out in a grid on the ImportPanel. They each display
 * a preview of the file, allow the user to add tags, and decide
 * what to do with the underlying file on disk on import.
 */
public class FileImportCell extends JPanel
{
	
	/** the preview of the file to display to the user */
	public PreviewPanel preview;
	/** The list of tags this file has already been tagged with */
	public TagsListPane tagsPanel;
	/** All of the associated meta data that accompanies this file */
	private FileMetadata file;
	/** Whether this file is currently selected to add tags to */
	private boolean isSelected;
	/** Value determines what the user wants to do with the file. */
	private CopyStatus copyStatus;
	private JRadioButton copyStatusButton;
	
	/**
	 * Adds a tag to the FileMetadata for the file this cell is previewing
	 * @param tag The tag the user wants to add to the FileMetadata
	 */
	public void addTag(Tag tag){}
	
	public FileMetadata getFileMetadata() {
		return file;
	}

	public void setFileMetadata(FileMetadata file) {
		this.file = file;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public CopyStatus getCopyStatus() {
		return copyStatus;
	}

	public void setCopyStatus(CopyStatus copyStatus) {
		this.copyStatus = copyStatus;
	}
	

}
