package edu.jhu.cs.oose.biblio.model.pdf;

import java.util.Set;

import javax.xml.crypto.Data;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;


/**
* Contains PDF specific metadata on top of that stored in FileMetadata
*/
public class PDFFileMetadata extends FileMetadata {

	public PDFFileMetadata(Data d, int o, String p, Set<Tag> t) {
		super(d, o, p, t);
		// TODO Auto-generated constructor stub
	}

}
