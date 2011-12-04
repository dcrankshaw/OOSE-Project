package edu.jhu.cs.oose.biblio.model;

import java.io.File;
import java.util.Set;

public interface WatcherEventListener {
	
	public void directoryModified(Set<File> addedFiles, Set<File> deletedFiles);

}
