package edu.jhu.cs.oose.biblio.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.jhu.cs.oose.biblio.model.FileMetadata;


/**
 * Displays a preview of thumbnail of a file. Subclasses determine what exactly a preview of that
 * file type should be. For example, the first page of a PDF or a thumbnail of an image.
 */


public abstract class PreviewPanel extends FileDisplayPanel {
	/**
	 * Creates a new PreviewPanel and sets the border.
	 */
	public PreviewPanel() {
		this(null);
	}
	
	public PreviewPanel(FileMetadata f) {
		super(f);
		this.setBorder(BorderFactory.createEtchedBorder());
		
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Show Properties");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileViewManager.getPropertiesManager().openFileView(getFile());
			}
		});
		popupMenu.add(item);
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
