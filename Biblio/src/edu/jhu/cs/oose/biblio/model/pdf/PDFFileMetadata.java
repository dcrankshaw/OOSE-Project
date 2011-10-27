package edu.jhu.cs.oose.biblio.model.pdf;

import java.util.Date;
import java.util.Set;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.FileTypes;
import edu.jhu.cs.oose.biblio.model.Tag;


/**
* Contains PDF specific metadata on top of that stored in FileMetadata
*/
public class PDFFileMetadata extends FileMetadata {

	public PDFFileMetadata(Date date, int timesOpened, String path,
			Set<Tag> fileTags) {
		super(date, timesOpened, path, fileTags, FileTypes.PDF);
		// TODO Auto-generated constructor stub
	}

	

}
