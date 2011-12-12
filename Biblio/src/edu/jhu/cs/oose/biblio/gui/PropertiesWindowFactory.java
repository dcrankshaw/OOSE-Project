package edu.jhu.cs.oose.biblio.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * A class that creates the Properties windows for
 * all the files.
 */
public class PropertiesWindowFactory implements FileViewFactory {
	/**
	 * A window that contains a PropertiesPanel and removes
	 * the view from the manager when it is closed.
	 */
	private static class PropertiesFrame extends JFrame implements FileView
	{
		/** The panel displaying the properties of the file */
		private PropertiesPanel panel;
		/**
		 * Creates a new window to display the properties of the given file.
		 * @param f the file whose properties should be displayed.
		 */
		public PropertiesFrame(FileMetadata f) {
			super();
			this.panel = new PropertiesPanel(f);
			this.add(panel);
			this.addWindowListener(new WindowListener() {
				@Override
				public void windowActivated(WindowEvent arg0) {
				}
				@Override
				public void windowClosed(WindowEvent e) {
				}
				
				@Override
				public void windowClosing(WindowEvent e) {
					FileViewManager.getPropertiesManager().removeView(PropertiesFrame.this);
					panel.cleanup();
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
				}
				@Override
				public void windowDeiconified(WindowEvent e) {
				}
				@Override
				public void windowIconified(WindowEvent e) {
				}
				@Override
				public void windowOpened(WindowEvent e) {
				}
				
			});
		}
		
		@Override
		public void makeVisible() {
			this.toFront();
		}
		
		@Override
		public void revalidate() {
			this.pack();
		}
		
		@Override
		public FileMetadata getFile() {
			return panel.getFile();
		}
	}
	
	@Override
	public FileView newView(FileMetadata file) {
		PropertiesFrame result = new PropertiesFrame(file);
		result.pack();
		result.setVisible(true);
		return result;
	}
}
