package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.UnsupportedFiletypeException;

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
	public TagsListPanel tagsPanel;
	/** All of the associated meta data that accompanies this file */
	private FileMetadata file;
	/** Whether this file is currently selected to add tags to */
	private boolean isSelected;
	/** Value determines what the user wants to do with the file. */
	private CopyStatus copyStatus;
	/** Our preferred choice for copying/moving/leaving files */
	private static final CopyStatus DEFAULT_COPY_STATUS = CopyStatus.MOVEFILE;
	/** The button that tells us to copy the file into our repository. */
	private JRadioButton copyStatusButton;
	/** The button that tells us to move the file into our repository. */
	private JRadioButton moveStatusButton;
	/** The button that tells us to reference the existing file.
	 * There will not be a copy in our repository. */
	private JRadioButton leaveStatusButton;
	/** The string telling the user that the file will be moved into the repository. */
	private static final String MOVE_STRING = "Move";
	/** The string telling the user that the file will be left along and not copied to the repository. */
	private static final String LEAVE_IN_PLACE_STRING = "In Place";
	/** The string telling the user that the file will be copied into the repository. */
	private static final String COPY_STRING = "Copy";
	
	/**
	 * Creates a cell that allows the user to describe how this file
	 * should be imported.  This panel gets used to apply tags
	 * and select how the file should be copied.
	 * @param fileMetadata the file to handle
	 */
	public FileImportCell(FileMetadata fileMetadata)
	{
		this.file = fileMetadata;
		copyStatus = DEFAULT_COPY_STATUS;
		isSelected = false;
		tagsPanel = new TagsListPanel(file);
		try
		{
			preview = FilePreviewFactory.createPreview(file);
		}
		catch(UnsupportedFiletypeException e)
		{
			preview = null; //means we can't display a preview of the file, which is okay
		}
		
		this.copyStatusButton = new JRadioButton(COPY_STRING);
		this.copyStatusButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				FileImportCell.this.setCopyStatus(CopyStatus.COPYFILE);
			}	
		});
		
		this.moveStatusButton = new JRadioButton(MOVE_STRING);
		this.moveStatusButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				FileImportCell.this.setCopyStatus(CopyStatus.MOVEFILE);
			}	
		});
		this.moveStatusButton.setSelected(true);
		copyStatus = DEFAULT_COPY_STATUS;
		this.leaveStatusButton = new JRadioButton(LEAVE_IN_PLACE_STRING);
		this.leaveStatusButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				FileImportCell.this.setCopyStatus(CopyStatus.LEAVEINPLACE);
			}
		});
		ButtonGroup copyButtonGroup = new ButtonGroup();
		copyButtonGroup.add(copyStatusButton);
		copyButtonGroup.add(moveStatusButton);
		copyButtonGroup.add(leaveStatusButton);
		
		
		this.setLayout(new BorderLayout());
		JPanel copyButtonsPanel = new JPanel(new GridLayout(1, CopyStatus.values().length));
		copyButtonsPanel.add(moveStatusButton);
		copyButtonsPanel.add(copyStatusButton);
		copyButtonsPanel.add(leaveStatusButton);
		this.add(copyButtonsPanel, BorderLayout.SOUTH);
		if(preview != null) {
			this.add(preview, BorderLayout.CENTER);
		}
		this.add(tagsPanel, BorderLayout.EAST);
		
	}
	
	/**
	 * Adds a tag to the FileMetadata for the file this cell is previewing
	 * @param tag The tag the user wants to add to the FileMetadata
	 */
	public void addTag(Tag tag)
	{
		this.tagsPanel.addTag(tag);
	}
	
	/**
	 * Gets the file being tagged, etc. by this cell.
	 * @return the file being tagged, etc. by this cell.
	 */
	public FileMetadata getFileMetadata() {
		return file;
	}

	/**
	 * Sets the file being tagged, etc. by this cell.
	 * @param file the file being tagged, etc. by this cell.
	 */
	public void setFileMetadata(FileMetadata file) {
		this.file = file;
	}

	/**
	 * Whether or not this cell is highlighted.  If the cell
	 * selected, then tags applied to multiple files at once
	 * will be applied to this file.
	 * @return true if the cell is selected, false otherwise
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Sets whether or not this cell is highlighted.  If the cell
	 * selected, then tags applied to multiple files at once
	 * will be applied to this file.
	 * @param isSelected the selection state of the cell
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	/**
	 * Returns whether this file should be copied or moved
	 * into the repository, or just referenced in place.
	 * @return how to reference the file in our repository
	 */
	public CopyStatus getCopyStatus() {
		return copyStatus;
	}

	/**
	 * Sets whether this file should be copied or moved
	 * into the repository, or just referenced in place.
	 * @param copyStatus how to reference the file in our repository
	 */
	private void setCopyStatus(CopyStatus copyStatus) {
		this.copyStatus = copyStatus;
	}
	

}
