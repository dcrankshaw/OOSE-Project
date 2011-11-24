package edu.jhu.cs.oose.biblio.gui.epub;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import edu.jhu.cs.oose.biblio.gui.FullFilePanel;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileContents;

public class EpubFullFilePanel extends FullFilePanel {

	private EpubFileContents contents;
	
	public EpubFullFilePanel(EpubFileContents c)
	{
		contents = c;
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void paint(Graphics g, Rectangle region) {
		// TODO Auto-generated method stub

	}

}
