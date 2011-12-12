package edu.jhu.cs.oose.biblio.model.tests;

import javax.persistence.Entity;
import javax.persistence.Table;

import edu.jhu.cs.oose.biblio.gui.FilePreviewVisitor;
import edu.jhu.cs.oose.biblio.gui.PreviewPanel;
import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.FileContents;
import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * An empty implementation of FileMetadata so that we
 * can actually test things.  This has default visibility
 * so that it is only visible in the tests package
 */
@Entity
@Table( name = "TEST_FILEMETADATA" )
class TestMetadata extends FileMetadata {
	
	public TestMetadata(String name) {
		super(name);
	}
	
	public TestMetadata() {
		super();
	}
	
	@Override
	public int searchText(String searchTerm) throws Exception {
		return 0;
	}

	@Override
	public FileContents getContents() {
		return null;
	}
	
	@Override
	public PreviewPanel createPreview(FilePreviewVisitor visitor, Bookmark bkmk) {
		return null;
	}
}