package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.epub.EpubPreviewPanel;
import edu.jhu.cs.oose.biblio.gui.pdf.PDFPreviewPanel;
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
		public PDFPreviewPanel makePDFPreviewPanel(PDFFileMetadata data) {
			return new PDFPreviewPanel(data);
		}
		@Override
		public EpubPreviewPanel makeEpubPreviewPanel(EpubFileMetadata data) {
			return new EpubPreviewPanel(data);
		}
	}
	
	/**
	 * The visitor given to the FileMetadata to help them construct
	 * a PreviewPanel.
	 */
	private static FilePreviewVisitor visitor = new PreviewConstructor();
	
	/**
	 * Creates a FilePreviewPanel capable of displaying the contents
	 * of the given file.
	 * @param file the file to preview
	 * @return a new FilePreviewPanel that displays the file
	 */
	public static PreviewPanel createPreview(FileMetadata file)
	{
		return file.createPreview(visitor);
	}

}
