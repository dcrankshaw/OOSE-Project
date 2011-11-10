package edu.jhu.cs.oose.biblio.gui;

/**
 * The choices a user has for how to store a file when importing
 */
public enum CopyStatus {
	/** Specifies that the file should be copied into our library. */
	COPYFILE,
	/** Specifies that the file should be moved into our library. */
	MOVEFILE,
	/** Specifies that the file should be referenced
	 * and not moved into our library */
	LEAVEINPLACE
}
