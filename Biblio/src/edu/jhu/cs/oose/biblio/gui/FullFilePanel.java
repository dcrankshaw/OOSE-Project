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
	 * This overrides JPanel.paint() to paint with the additional
	 * knowledge of which part of the panel is visible.
	 */
	@Override
	public final void paint(Graphics g) {
		if( null != viewport ) {
			// this was throwing a NullPointerException...
			paint(g, viewport.getViewRect());
		}
	}
	
	/**
	 * Paints only the region that is visible
	 * @param g the graphics context to draw in
	 * @param region the visible region to draw in
	 */
	public abstract void paint(Graphics g, Rectangle region);
	
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
