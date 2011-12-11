package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.hibernate.Session;

import edu.jhu.cs.oose.biblio.model.Database;
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
	/**
	 * The widths of the text fields used for entering tags
	 */
	private static final int TEXT_FIELD_WIDTH = 30;

	/**
	 * The number of columns of documents displayed in the import panel.
	 */
	private static final int PREVIEW_PANEL_COLUMNS = 2;

	/** Contains all of the files being imported */
	private List<FileImportCell> fileCellArray;

	/** A text box to enter in a tag to apply to multiple files at once */
	private JTextField tagEntryField;

	/**
	 * A button to signal to apply the tag entered in the TagsListPane to the
	 * selected files
	 */
	private JButton applyButton;

	/** A button to cancel the current import transaction discarding all changes */
	private JButton cancelButton;

	/**
	 * A button to finish the current import transaction, applying all changes
	 * to the model
	 */
	private JButton finishButton;

	/**
	 * Tags that have been created during the current import session.
	 */
	private Collection<Tag> newTags;

	/**
	 * The parent dialog box containing this import panel.
	 */
	JDialog owner;

	/**
	 * Creates a new ImportPanel initialized with the list of files and
	 * belonging to the given dialog.
	 * 
	 * @param files
	 *            the files to be imported
	 * @param currentOwner
	 *            the dialog enclosing this panel
	 */
	public ImportPanel(List<FileMetadata> files, JDialog currentOwner) {
		// Initialize components
		if (Database.isSessionOpen()) {
			System.out
					.println(">>>>>>>>> ERROR: PREVIOUS DATABASE TRANSACTION NOT CLOSED");
		}
		Database.getNewSession();
		applyButton = new JButton("Apply");
		this.owner = currentOwner;
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyTagToMany(tagEntryField.getText());
				tagEntryField.setText("");
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

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
		finishButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				finishImport();
			}
		});
		tagEntryField = new JTextField(TEXT_FIELD_WIDTH);
		tagEntryField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyTagToMany(tagEntryField.getText());
				tagEntryField.setText("");
			}
		});
		newTags = new ArrayList<Tag>();

		fileCellArray = new ArrayList<FileImportCell>();
		setFiles(files);

		// Configure Pane
		this.setLayout(new BorderLayout());
		// JScrollPane fileCellsPanel = new JScrollPane();
		JPanel fileCellsPanel = new JPanel();

		int rows = files.size() / PREVIEW_PANEL_COLUMNS;
		if (files.size() % PREVIEW_PANEL_COLUMNS > 0) {
			rows++;
		}
		// rows, PREVIEW_PANEL_COLUMNS)
		// fileCellsPanel.setLayout(new ScrollPaneLayout());
		fileCellsPanel.setLayout(new GridLayout(rows, PREVIEW_PANEL_COLUMNS));
		JPanel globalOptionsPanel = new JPanel();
		globalOptionsPanel.setLayout(new GridLayout());
		this.add(globalOptionsPanel, BorderLayout.SOUTH);

		// Add components to the Pane
		globalOptionsPanel.add(tagEntryField);
		globalOptionsPanel.add(applyButton);
		globalOptionsPanel.add(cancelButton);
		globalOptionsPanel.add(finishButton);
		globalOptionsPanel.setPreferredSize(new Dimension(400, 50));
		for (FileImportCell cell : fileCellArray) {
			fileCellsPanel.add(cell);
		}
		this.add(fileCellsPanel, BorderLayout.CENTER);

	}

	/**
	 * Applies the tag with the given name to all of the files that have been
	 * selected by the user.
	 * 
	 * @param tagName The name of the tag to apply to all of the selected files
	 */
	public void applyTagToMany(String tagName) {
		Tag tag = Database.getTag(tagName);
		if (null == tag) {
			try {
				tag = new Tag(tagName);
			} catch (Exception e) {
				System.out.println("Cannot create a tag with a colon in its name");
			}
			if (tag != null)
				newTags.add(tag);
		}
		if (tag != null) {
			for (FileImportCell cell : fileCellArray) {
				if (cell.isSelected()) {
					cell.addTag(tag);
				}
			}
		}
	}

	/**
	 * Creates a FileImportCell for each file
	 * 
	 * @param files the files to place in the import cells
	 */
	public void setFiles(List<FileMetadata> files) {
		for (FileMetadata file : files) {
			FileImportCell current = new FileImportCell(file);
			current.addMouseListener(new ImportCellListener(current));
			fileCellArray.add(current);
		}

	}

	/**
	 * A private mouse listener class that allows the ImportPanel to detect when
	 * a particular cell has been clicked on
	 * 
	 * @author Daniel
	 */
	private class ImportCellListener extends MouseAdapter {
		/**
		 * The cell associated with this listener; the one that got clicked.
		 */
		private FileImportCell cell;

		/**
		 * Creates a new listener for the given cell
		 * 
		 * @param c
		 *            the cell to listen for clicks on
		 */
		public ImportCellListener(FileImportCell c) {
			cell = c;
		}

		public void mouseClicked(MouseEvent e) {
			if (!e.isShiftDown()) {
				ImportPanel.this.unselectAllCells();
			}
			cell.setSelected(true);
		}
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		super.paint(g);
	}

	/**
	 * Deselects all the cells in the import panel.
	 */
	public void unselectAllCells() {
		for (FileImportCell file : fileCellArray) {
			file.setSelected(false);
		}
	}

	/**
	 * Finishes the import by committing transactions to the model and signaling
	 * to close the import window
	 */
	public void finishImport() {
		Database.commit();
		owner.dispose();
	}

	/**
	 * Finishes the import by closing import window without committing changes
	 * to the model
	 */
	public void cancelImport() {
		Database.rollback();
		owner.dispose();
	}

}
