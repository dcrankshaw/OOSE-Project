package iTag;

import java.util.Set;

public class Tag {
	public Set<Tag> children;
	public String name;
	public Set<Bookmark> taggedBookmarks;
	public Set<FileMetadata> taggedFiles;
	
	
}