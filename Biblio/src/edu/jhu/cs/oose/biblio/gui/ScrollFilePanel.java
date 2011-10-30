package edu.jhu.cs.oose.biblio.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class ScrollFilePanel extends JPanel {
	
	private JScrollPane scrollPane;
	
	public ScrollFilePanel() {
		scrollPane = new JScrollPane();
		add(scrollPane);
		setLayout(new GridLayout(1, 1));
	}
	
	public void setContents(FullFilePanel panel) {
		scrollPane.getViewport().setView(panel);
		panel.setViewport(scrollPane.getViewport());
	}
}
