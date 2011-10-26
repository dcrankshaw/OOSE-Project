package iTag;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.crypto.Data;

@Entity
@Table( name = "FILEMETADATA" )
public class FileMetadata {
	
	 @Temporal(TemporalType.TIMESTAMP)
	private Data lastOpened;
	private int opendedCount;
	private String pathToFile;
	private Set<Tag> tags;
	
	public FileMetadata(Data d, int o, String p, Set<Tag> t) {
		this.lastOpened = d;
		this.opendedCount = o;
		this.pathToFile = p;
		this.tags = t;
	}
	
	public FileContents getContents() {
		return null;
	}
}
