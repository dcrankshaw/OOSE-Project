package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.DateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * Interface to display a list of the properties associated with a file
 * 
 */
public class PropertiesPanel extends JPanel {
	/** The list of tags the file is associated with */
	private TagsListPanel tagsListPane;

	/** The file whose properties are being displayed */
	public FileMetadata file;
	
	/** A label displaying the date and time of the last time this file was opened in Biblio */
	private JLabel lastOpenedLabel;
	/** The label displaying "Last Opened:" */
	private JLabel lastOpenedTitle;
	
	/** A label displaying the number of times this file has been opened through Biblio */
	private JLabel openedCountLabel;
	/** The label displaying the text "Opened Count:" */ 
	private JLabel openedCountTitle;
	
	/** Object that turns Dates into Strings */
	private DateFormat formatter;
	
	/** Creates a new panel that displays the properties of nothing in particular. */
	public PropertiesPanel() {
		this.tagsListPane = new TagsListPanel();
		this.lastOpenedTitle = new JLabel("Last Opened:");
		this.lastOpenedLabel = new JLabel();
		this.openedCountTitle = new JLabel("Opened Count:");
		this.openedCountLabel = new JLabel();
		
		JPanel twoColumns = new JPanel();
		twoColumns.setLayout(new GridLayout(2, 2));
		twoColumns.add(this.lastOpenedTitle);
		twoColumns.add(this.lastOpenedLabel);
		twoColumns.add(this.openedCountTitle);
		twoColumns.add(this.openedCountLabel);
		
		this.setLayout(new BorderLayout());
		this.add(twoColumns, BorderLayout.NORTH);
		this.add(tagsListPane, BorderLayout.CENTER);
		
		formatter = DateFormat.getDateInstance();
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
		lastOpenedLabel.setText(formatter.format(this.file.getLastOpened()));
		openedCountLabel.setText(Integer.toString(this.file.getOpenedCount()));
		tagsListPane.setTags(this.file);
	}
	
	/**
	 * Returns the file whose properties are displayed in this panel.
	 * @return the file whose properties are displayed in this panel.
	 */
	public FileMetadata getFile() {
		return this.file;
	}
}
