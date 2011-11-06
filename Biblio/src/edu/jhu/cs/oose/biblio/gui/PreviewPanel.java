package edu.jhu.cs.oose.biblio.gui;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;


/**
 * Displays a preview of thumbnail of a file. Subclasses determine what exactly a preview of that
 * file type should be. For example, the first page of a PDF or a thumbnail of an image.
 */


public abstract class PreviewPanel extends FileDisplayPanel {
	public PreviewPanel() {
		this.setBorder(BorderFactory.createEtchedBorder());
	}
}
