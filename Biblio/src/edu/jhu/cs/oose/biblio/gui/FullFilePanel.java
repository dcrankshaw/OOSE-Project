package edu.jhu.cs.oose.biblio.gui;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Displays the entire file, not including scrollbars, etc needed to view the whole file
 * depending what type of file it is.
 */
public abstract class FullFilePanel extends JPanel implements ChangeListener, Scrollable {
	
	/**
	 * The viewport containing this panel.  We need
	 * access to this in order to find out which part
	 * of ourself to draw.
	 */
	private JViewport viewport;
	
	/**
	 * Returns the viewport containing this panel.
	 * Useful for subclasses to determine how big they should be
	 * (i.e. for triggering scrollbars).
	 * @return the viewport containing this panel
	 */
	protected final JViewport getViewport() {
		return viewport;
	}
	
	/**
	 * Sets the parent viewport of this panel
	 * @param view the parent viewport
	 */
	public void setViewport(JViewport view) {
		viewport = view;
	}
	
	@Override
	public void stateChanged(ChangeEvent o) {
		repaint();
	}
}
