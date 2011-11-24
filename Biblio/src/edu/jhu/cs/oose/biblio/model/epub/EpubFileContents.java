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
	
	public EpubFileContents(String path) throws Exception
	{
		pages = new HashMap<Integer, Image>();
		preview = null;
	}
	
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
