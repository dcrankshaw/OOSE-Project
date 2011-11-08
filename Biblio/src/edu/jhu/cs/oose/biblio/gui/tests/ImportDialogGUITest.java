package edu.jhu.cs.oose.biblio.gui.tests;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;

import edu.jhu.cs.oose.biblio.gui.ImportDialog;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * Draws the import dialog box, for testing.
 * @author Dan Crankshaw
 */
public class ImportDialogGUITest {

	/**
	 * Displays the import dialog, putting 2 files from our
	 * repository into the preview panels
	 * @param args ignored
	 */
	public static void main(String[] args) {
		// BUG this does not quit on command-q or selecting quit from the menu
		// I think this is because it's in a dialog, not a frame. - Paul
		try
		{
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			List<FileMetadata> files = new ArrayList<FileMetadata>();
			String path1 = "testfiles/test1.pdf";
			FileMetadata file1 = new PDFFileMetadata(new Date(), 0, path1, new HashSet<Tag>());
			files.add(file1);
			String path2 = "testfiles/test2.pdf";
			FileMetadata file2 = new PDFFileMetadata(new Date(), 0, path2, new HashSet<Tag>());
			files.add(file2);
			ImportDialog testDialog = new ImportDialog(files, frame);
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

}
