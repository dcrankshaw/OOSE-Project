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
	/**
	 * The list of tags the file is associated with
	 */
	private TagsListPanel tagsListPane;

	/**
	 * The file whose properties are being displayed
	 */
	public FileMetadata file;
	
	/**
	 * A label displaying the date and time of the last time this file was opened in Biblio
	 */
	private JLabel lastOpenedLabel;
	private JLabel lastOpenedTitle;
	
	/**
	 * A label displaying the number of times this file has been opened through Biblio
	 */
	private JLabel openedCountLabel;
	private JLabel openedCountTitle;
	
	DateFormat formatter;
	
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
	
	public PropertiesPanel(FileMetadata f) {
		this();
		setFile(f);
	}
	
	public void setFile(FileMetadata f) {
		this.file = f;
		lastOpenedLabel.setText(formatter.format(this.file.getLastOpened()));
		openedCountLabel.setText(Integer.toString(this.file.getOpenedCount()));
		tagsListPane.setFile(this.file);
	}
	
	public FileMetadata getFile() {
		return this.file;
	}
}
