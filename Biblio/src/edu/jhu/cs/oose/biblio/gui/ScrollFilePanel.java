package edu.jhu.cs.oose.biblio.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * A Panel that contains scrollbars so that entire files
 * will fit on screen.  This contains the FullFilePanel.
 */
public class ScrollFilePanel extends JPanel {
	
	/** The scroll pane containing the FullFileView */
	private JScrollPane scrollPane;
	
	/** Creates a new panel with scrollbars in it */
	public ScrollFilePanel() {
		scrollPane = new JScrollPane();
		add(scrollPane);
		setLayout(new GridLayout(1, 1));
	}
	
	/**
	 * Sets the given panel to be displayed inside of the scroll pane.
	 * @param panel the panel to put inside the scroll pane.
	 */
	public void setContents(FullFilePanel panel) {
		scrollPane.getViewport().setView(panel);
		panel.setViewport(scrollPane.getViewport());
	}
}
