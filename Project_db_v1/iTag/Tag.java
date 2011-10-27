package iTag;

import java.util.Set;

public class Tag {
	private Set<Tag> children;
	private String name;
	private Set<Bookmark> taggedBookmarks;
	private Set<FileMetadata> taggedFiles;
	
	public Tag(Set<Tag> c, String n, Set<Bookmark> t, Set<FileMetadata> ta) {
		this.children = c;
		this.name = n;
		this.taggedBookmarks = t;
		this.taggedFiles = ta;
	}
	
	public void setName(String n) {
		this.name = n;
	}
	
	public String getName(){
		return this.name;
	}
	
	public boolean addChildren(Tag tag) {
		return this.children.add(tag);
	}
	
	public boolean tagBookmark(Bookmark bkmk) {
		return this.taggedBookmarks.add(bkmk);
	}
	
	public boolean tagFile(FileMetadata file) {
		return this.taggedFiles.add(file);
	}
}
