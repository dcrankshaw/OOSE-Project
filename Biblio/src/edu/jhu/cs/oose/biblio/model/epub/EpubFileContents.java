package edu.jhu.cs.oose.biblio.model.epub;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import edu.jhu.cs.oose.biblio.model.FileContents;

public class EpubFileContents implements FileContents {

	
	/**
	 * A cache of all the pages that have been read into memory so far
	 */
	private Map<Integer, Image> pages;
	
	/**
	 * The preview of this document
	 */
	private Image preview;
	
	/**
	 * Creates a new instance of the file at the provided path
	 * @param path the path to the file
	 * @throws Exception if there is an error reading the file
	 */
	public EpubFileContents(String path) throws Exception
	{
		pages = new HashMap<Integer, Image>();
		preview = null;
	}
	
	/**
	 * Reads the specified page from this Epub book
	 * @param pageNum the number of the page to read
	 * @return the page as an Image
	 * @throws Exception If there is an error reading the page
	 */
	private Image readPage(int pageNum) throws Exception
	{
		return null;
	}
	
	@Override
	public int search(String searchTerm) {
		// TODO implement this method
		return 0;
	}

}
