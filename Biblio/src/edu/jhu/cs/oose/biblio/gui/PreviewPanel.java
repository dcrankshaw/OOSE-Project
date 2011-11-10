package edu.jhu.cs.oose.biblio.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


/**
 * Displays a preview of thumbnail of a file. Subclasses determine what exactly a preview of that
 * file type should be. For example, the first page of a PDF or a thumbnail of an image.
 */


public abstract class PreviewPanel extends FileDisplayPanel {
	
	/**
	 * Creates a new PreviewPanel and sets the border.
	 */
	public PreviewPanel() {
		this.setBorder(BorderFactory.createEtchedBorder());
		
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new JMenuItem("Show Properties"));
		this.setComponentPopupMenu(popupMenu);
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if( e.getClickCount() == 2 ) {
					FileViewManager.getViewManager().openFileView(getFile());
				}
			}
		});
	}
}
