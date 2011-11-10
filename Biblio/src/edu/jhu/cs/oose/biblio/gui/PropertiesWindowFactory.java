package edu.jhu.cs.oose.biblio.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

public class PropertiesWindowFactory implements FileViewFactory {
	private class PropertiesFrame extends JFrame implements FileView
	{
		private PropertiesPanel panel;
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
					FileViewManager.getPropertiesManager().removeView(panel.getFile());
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
	}
	
	@Override
	public FileView newView(FileMetadata file) {
		PropertiesFrame result = new PropertiesFrame(file);
		result.pack();
		result.setVisible(true);
		return result;
	}
}
