package edu.jhu.cs.oose.biblio.model.epub;

import nl.siegmann.epublib.domain.Resource;
/**
 * This class written by Paul Siegmann and taken from the EpubLib software project.
 * See http://www.siegmann.nl/epublib
 */

public class SearchResult {
	private int pagePos = -1;
	private String searchTerm;
	private Resource resource;
	public SearchResult(int pagePos, String searchTerm, Resource resource) {
		super();
		this.pagePos = pagePos;
		this.searchTerm = searchTerm;
		this.resource = resource;
	}
	public int getPagePos() {
		return pagePos;
	}
	public String getSearchTerm() {
		return searchTerm;
	}
	public Resource getResource() {
		return resource;
	}
}