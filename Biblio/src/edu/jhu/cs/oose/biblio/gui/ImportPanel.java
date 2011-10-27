package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneLayout;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * GUI element that houses all the components needed to import a set of files
 * into a Biblio library. It has a set of FileImportCells laid out in a grid
 * pattern in a JScrollPane.
 */
public class ImportPanel extends JPanel {

	// TODO set this to something intelligent, or figure out a better way to do
	// this
	private static final int TEXT_FIELD_WIDTH = 30;

	private static final int PREVIEW_PANEL_COLUMNS = 2;
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	/** Contains all of the files being imported */
	List<FileImportCell> fileCellArray;

	/** A text box to enter in a tag to apply to multiple files at once */
	JTextField tagEntryField;

	/**
	 * A button to signal to apply the tag entered in the TagsListPane to the
	 * selected files
	 */
	JButton applyButton;

	/** A button to cancel the current import transaction discarding all changes */
	JButton cancelButton;

	/**
	 * A button to finish the current import transaction, applying all changes
	 * to the model
	 */
	JButton finishButton;

	Collection<Tag> newTags;

	JDialog owner;

	public ImportPanel(List<FileMetadata> files, ImportDialog currentOwner) {

		// Initialize components

		applyButton = new JButton("Apply");
		this.owner = currentOwner;
		applyButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				String tagName = tagEntryField.getText();
				tagEntryField.setText(null);
				/*
				 * If model doesnt contains tag: Tag current = new Tag(tagName);
				 * newTags.add(current);
				 * 
				 * applyTagToMany(current);
				 */
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				int result = JOptionPane
						.showConfirmDialog(ImportPanel.this.owner,
								"Are you sure you want to cancel?", "Cancel?",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (result == JOptionPane.YES_OPTION) {
					cancelImport();
				}

			}

		});
		
		finishButton = new JButton("Finish Import");
		finishButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				finishImport();
			}
		});
		tagEntryField = new JTextField(TEXT_FIELD_WIDTH);
		newTags = new ArrayList<Tag>();

		fileCellArray = new ArrayList<FileImportCell>();
		setFiles(files);

		// Configure Pane
		this.setLayout(new BorderLayout());
		//JScrollPane fileCellsPanel = new JScrollPane();
		JPanel fileCellsPanel = new JPanel();
		
		int rows = files.size() / PREVIEW_PANEL_COLUMNS;
		if (files.size() % PREVIEW_PANEL_COLUMNS > 0) {
			rows++;
		}
//rows, PREVIEW_PANEL_COLUMNS)
		//fileCellsPanel.setLayout(new ScrollPaneLayout());
		fileCellsPanel.setLayout(new GridLayout(rows, PREVIEW_PANEL_COLUMNS));
		JPanel globalOptionsPanel = new JPanel();
		globalOptionsPanel.setLayout(new GridLayout());
		this.add(globalOptionsPanel, BorderLayout.SOUTH);

		// Add components to the Pane
		globalOptionsPanel.add(tagEntryField);
		globalOptionsPanel.add(applyButton);
		globalOptionsPanel.add(cancelButton);
		globalOptionsPanel.add(finishButton);
		for (FileImportCell cell : fileCellArray) {
			fileCellsPanel.add(cell);
		}
		this.add(fileCellsPanel, BorderLayout.NORTH);

	}

	/**
	 * Applies the supplied tag to all of the files that have been selected by
	 * the user.
	 * 
	 * @param tag
	 *            The tag to apply to all of the selected files
	 */
	public void applyTagToMany(Tag tag) {
		for (FileImportCell cell : fileCellArray) {
			if(cell.isSelected())
			{
				// add tag to file
				// cell.getFileMetadata().addTag();
			}	
		}
	}

	/**
	 * Creates a FileImportCell for each file
	 * 
	 * @param files
	 */
	public void setFiles(List<FileMetadata> files) {
		for (FileMetadata file : files) {
			FileImportCell current = new FileImportCell(file);
			current.addMouseListener(new ImportCellListener(current));
			fileCellArray.add(current);
		}

	}
	
	private class ImportCellListener extends MouseAdapter
	{
		FileImportCell cell;
		public ImportCellListener(FileImportCell c)
		{
			cell = c;
		}
		public void mouseClicked(MouseEvent e) {
			if(!e.isShiftDown())
			{
				ImportPanel.this.unselectAllCells();
			}
			cell.setSelected(true);
		}
	}

	@Override
	public void paint(Graphics g)
	{
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		super.paint(g);
	}
	
	public void unselectAllCells()
	{
		for(FileImportCell file: fileCellArray)
		{
			file.setSelected(false);
		}
	}
	
	/**
	 * Finishes the import by committing transactions to the model and signaling
	 * to close the import window
	 */
	public void finishImport() {
		for (FileImportCell file : fileCellArray) {
			// add to model

		}
		for (Tag tag : newTags) {
			// add to model
		}
	}

	/**
	 * Finishes the import by closing import window without committing changes
	 * to the model
	 */
	public void cancelImport() {
	}

}
