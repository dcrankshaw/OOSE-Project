package edu.jhu.cs.oose.biblio.gui.epub;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import edu.jhu.cs.oose.biblio.gui.PreviewPanel;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileContents;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;

public class EpubPreviewPanel extends PreviewPanel {

	/**
	 * The contents of the file
	 */
	EpubFileContents contents;
	Image coverPage;

	/**
	 * Create a new instance with the provided file contents
	 * 
	 * @param c
	 *            the contents of the file
	 */

	public EpubPreviewPanel()
	{
		super();
		contents = null;
		coverPage = null;
	}
	
	public EpubPreviewPanel(EpubFileMetadata metadata) {
		super(metadata);
		contents = metadata.getContents();
		Book b = contents.getBook();
		Resource resourceCoverPage = b.getCoverImage();
		if (resourceCoverPage == null) {
			coverPage = null;
		} else {
			try {
				coverPage = ImageIO.read(resourceCoverPage.getInputStream());
			} catch (IOException e) {
				System.err.println("Unable to load cover image from book: "
						+ e.getLocalizedMessage());
			}
		}
	}
	
	

	public void paint(Graphics g) {
		// Make sure that we get a clean background behind us
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		try {
			// scale the image appropriately
			double widthRatio = this.getSize().getWidth()
					/ (double) coverPage.getWidth(null);
			double heightRatio = this.getSize().getHeight()
					/ (double) coverPage.getHeight(null);
			double ratio = 1;
			if (widthRatio < heightRatio) {
				ratio = widthRatio;
			} else {
				ratio = heightRatio;
			}
			ratio = Math.min(ratio, 1.0);

			int width = (int) (coverPage.getWidth(null) * ratio);
			int height = (int) (coverPage.getHeight(null) * ratio);

			int leftEdge = (this.getSize().width - width) / 2;
			int bottomEdge = (this.getSize().height - height) / 2;

			g.drawImage(coverPage, leftEdge, bottomEdge, leftEdge + width,
					bottomEdge + height, 0, 0, coverPage.getWidth(null),
					coverPage.getHeight(null), null);
		} catch (Exception e) {
			// TODO draw something that indicates what went wrong
			g.setColor(Color.RED);
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
		this.getBorder().paintBorder(this, g, 0, 0, this.getSize().width,
				this.getSize().height);
	}

}
