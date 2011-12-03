package edu.jhu.cs.oose.biblio.model.epub;

import java.awt.Image;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
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
	
	Book book;
	
	/**
	 * Creates a new instance of the file at the provided path
	 * @param path the path to the file
	 * @throws Exception if there is an error reading the file
	 */
	public EpubFileContents(String path) throws Exception
	{
		pages = new HashMap<Integer, Image>();
		preview = null;
		
		book = (new EpubReader()).readEpub(new FileInputStream(path));
	}
	
	public Book getBook()
	{
		return book;
	}
	
	
	@Override
	public int search(String searchTerm) {
		// TODO implement this method
		return 0;
	}

}
