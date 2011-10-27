package edu.jhu.cs.oose.biblio.gui.tests;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.JFrame;

import edu.jhu.cs.oose.biblio.gui.ImportDialog;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

public class ImportDialogGUITest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			List<FileMetadata> files = new ArrayList<FileMetadata>();
			String path1 = "/Users/Daniel/oose_proj/OOSE-Project/testfiles/test1.pdf";
			FileMetadata file1 = new FileMetadata(new Date(), 0, path1, new HashSet<Tag>());
			files.add(file1);
			String path2 = "/Users/Daniel/oose_proj/OOSE-Project/testfiles/test2.pdf";
			FileMetadata file2 = new FileMetadata(new Date(), 0, path2, new HashSet<Tag>());
			files.add(file2);
			ImportDialog testDialog = new ImportDialog(files, frame);
			testDialog.
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
		}

	}

}
