package iTag;

import java.util.Set;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

public class Tag {
	public Set<Tag> children;
	public String name;
	public Set<Bookmark> taggedBookmarks;
	public Set<FileMetadata> taggedFiles;
	
	
}