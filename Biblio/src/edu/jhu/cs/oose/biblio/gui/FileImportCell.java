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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
	private static final CopyStatus DEFAULT_COPY_STATUS = CopyStatus.MOVEFILE;
	private JRadioButton copyStatusButton;
	private JRadioButton moveStatusButton;
	private JRadioButton leaveStatusButton;
	private static String moveString = "move";
	private static String leaveInPlaceString = "In Place";
	private static String copyString = "Copy";
	
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
		
		this.copyStatusButton = new JRadioButton(copyString);
		this.copyStatusButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				FileImportCell.this.setCopyStatus(CopyStatus.COPYFILE);
			}	
		});
		
		this.moveStatusButton = new JRadioButton(moveString);
		this.moveStatusButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				FileImportCell.this.setCopyStatus(CopyStatus.MOVEFILE);
			}	
		});
		this.moveStatusButton.setSelected(true);
		copyStatus = DEFAULT_COPY_STATUS;
		this.leaveStatusButton = new JRadioButton(leaveInPlaceString);
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
			this.add(preview, BorderLayout.WEST);
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

	private void setCopyStatus(CopyStatus copyStatus) {
		this.copyStatus = copyStatus;
	}
	

}
