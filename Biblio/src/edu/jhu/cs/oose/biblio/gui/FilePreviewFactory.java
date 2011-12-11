package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.epub.EpubPreviewPanel;
import edu.jhu.cs.oose.biblio.gui.pdf.PDFPreviewPanel;
import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * Factory to create FilePreviewPanels.
 */
public class FilePreviewFactory {

	/**
	 * A visitor class used to construct Preview Panels.
	 * There should be one method on this for each kind of PreviewPanel.
	 * Each FileMetadata implementation should know which one to call.
	 */
	private static class PreviewConstructor implements FilePreviewVisitor {
		@Override
		public PDFPreviewPanel makePDFPreviewPanel(PDFFileMetadata data, Bookmark bkmk) {
			return new PDFPreviewPanel(data);
		}
		@Override
		public EpubPreviewPanel makeEpubPreviewPanel(EpubFileMetadata data, Bookmark bkmk) {
			return new EpubPreviewPanel(data);
		}
	}
	
	private static FilePreviewFactory factory = new FilePreviewFactory();
	
	public static FilePreviewFactory getFactory() {
		return factory;
	}
	
	/**
	 * The visitor given to the FileMetadata to help them construct
	 * a PreviewPanel.
	 */
	private FilePreviewVisitor visitor;
	
	private FilePreviewFactory() {
		visitor = new PreviewConstructor();
	}
	
	/**
	 * Creates a FilePreviewPanel capable of displaying the contents
	 * of the given file.
	 * @param file the file to preview
	 * @return a new FilePreviewPanel that displays the file
	 */
	public PreviewPanel createPreview(FileMetadata file)
	{
		return file.createPreview(visitor, null);
	}
	/**
	 * Creates a FilePreviewPanel capable of displaying the contents
	 * of the given file at a particular Bookmark.
	 * @param file the file to preview
	 * @param bkmk the Bookmark (location in the file) to preview
	 * @return a new FilePreviewPanel that displays the file
	 */
	public PreviewPanel createPreview(FileMetadata file, Bookmark bkmk)
	{
		return file.createPreview(visitor, bkmk);
	}
}
