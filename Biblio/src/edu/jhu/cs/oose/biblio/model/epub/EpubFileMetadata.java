package edu.jhu.cs.oose.biblio.model.epub;

import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import edu.jhu.cs.oose.biblio.model.FileContents;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;


@Entity
@Table( name = "EPUB_FILEMETADATA" )
public class EpubFileMetadata extends FileMetadata {
	
	public EpubFileMetadata()
	{
		super();
	}
	
	public EpubFileMetadata(String path)
	{
		super(path);
	}
	
	@Transient
	private EpubFileContents contents;
	
	/**
	 * Creates a new EpubFileMetadata, initialized with the given arguments
	 * @param date the last time file was opened
	 * @param timesOpened the number of times it has been opened
	 * @param path the path to the contents on disk
	 * @param fileTags the tags that should be applied already (NOT copied)
	 */
	public EpubFileMetadata(Date date, int timesOpened, String path,
			Set<Tag> fileTags) {
		super(date, timesOpened, path, fileTags);
	}
	
	@Override
	public int searchText(String searchTerm) throws Exception {
		// TODO unimplemented
		return 0;
	}

	@Override
	public FileContents getContents() {
		if(contents == null)
		{
			try {
				contents = new EpubFileContents(getPathToFile());
			}
			catch(Exception e)
			{
				return null;
			}
		}
		return contents;
	}

}