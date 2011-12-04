package edu.jhu.cs.oose.biblio.gui.epub;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collections;

import javax.swing.JPanel;

import nl.siegmann.epublib.browsersupport.Navigator;
import nl.siegmann.epublib.epub.BookProcessor;
import nl.siegmann.epublib.epub.BookProcessorPipeline;
import edu.jhu.cs.oose.biblio.gui.FullFilePanel;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileContents;

public class EpubFullFilePanel extends FullFilePanel {

	/**
	 * The contents of the file
	 */
	private EpubFileContents contents;
	
	private ContentPane mainContentsPane;
	private TableOfContentsPane tocPane;
	private Navigator navigator;
	private BookProcessorPipeline epubCleaner;
	
	/**
	 * Creates a new FullFilePanel with the given contents
	 * @param c
	 */
	public EpubFullFilePanel(EpubFileContents c)
	{
		navigator = new Navigator();
		epubCleaner = new BookProcessorPipeline(Collections.<BookProcessor>emptyList());
		contents = c;
		navigator.gotoBook(c.getBook(), this);
		mainContentsPane = new ContentPane(navigator);
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(mainContentsPane, BorderLayout.CENTER);
		tocPane = new TableOfContentsPane(navigator);
		JPanel tocPanel = new JPanel(new BorderLayout());
		tocPanel.add(tocPane, BorderLayout.CENTER);
		this.setLayout(new BorderLayout());
		this.add(contentPanel, BorderLayout.CENTER);
		this.add(tocPanel, BorderLayout.WEST);
		this.revalidate();
	}
	
	

	@Override
	public void paint(Graphics g, Rectangle region) {
		super.paint(g);

	}


	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}



	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return 1;
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
		return 1;
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		//TODO do i need to change this? -Dan
		return super.getPreferredSize();
	}

}
