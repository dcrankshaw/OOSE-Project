package edu.jhu.cs.oose.biblio.gui;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * GUI element that houses all the components needed to import a set of files into a Biblio library.
 * It has a set of FileImportCells laid out in a grid pattern in a JScrollPane.
 */
public class ImportPanel extends JPanel {
	
	/** Contains all of the files being imported */
	List<FileImportCell> fileCellArray;
	
	/** A text box to enter in a tag to apply to multiple files at once*/
	TagsListPane tagEntryField;
	
	/** A button to signal to apply the tag entered in the TagsListPane to the selected files */
	JButton applyButton;
	
	/** A button to cancel the current import transaction discarding all changes */
	JButton cancelButton;
	
	/** A button to finish the current import transaction, applying all changes to the model */
	JButton finishButton;
	
	
	/**
	 * Applies the supplied tag to all of the files that have been selected by the user.
	 * @param tag The tag to apply to all of the selected files
	 */
	public void applyTagToMany(Tag tag) {}
	
	/**
	 * Creates a FileImportCell for each of these files and adds them to the display
	 * @param files
	 */
	public void setFiles(List<FileMetadata> files){}
	
	/**
	 * Finishes the import by committing transactions to the model and signaling to close the import window
	 */
	public void finishImport(){}
	
	/**
	 * Finishes the import by closing import window without committing changes to the model
	 */
	public void cancelImport(){}
	
	

}
