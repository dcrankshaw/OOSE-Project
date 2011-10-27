package edu.jhu.cs.oose.biblio.model.pdf;

import java.util.Date;
import java.util.Set;

import org.jpedal.exception.PdfException;

import edu.jhu.cs.oose.biblio.model.FileContents;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.FileTypes;
import edu.jhu.cs.oose.biblio.model.Tag;


/**
* Contains PDF specific metadata on top of that stored in FileMetadata
*/
public class PDFFileMetadata extends FileMetadata {
	
	PDFFileContents contents;
	
	public PDFFileMetadata(Date date, int timesOpened, String path,
			Set<Tag> fileTags) {
		super(date, timesOpened, path, fileTags, FileTypes.PDF);
		// TODO Auto-generated constructor stub
	}

	@Override
	public FileContents getContents() {
		if( null == contents ) {
			try {
				contents = new PDFFileContents(getPathToFile());
			}
			catch(PdfException e) {
				// TODO there should be a better way to handle this,
				return null;
			}
		}
		return contents;
	}

}
