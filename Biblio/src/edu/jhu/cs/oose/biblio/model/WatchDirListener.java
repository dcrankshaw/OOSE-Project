package edu.jhu.cs.oose.biblio.model;

import java.io.File;
import java.util.Set;

public interface WatchDirListener {
	public void toImport(Set<File> unImported);

}
