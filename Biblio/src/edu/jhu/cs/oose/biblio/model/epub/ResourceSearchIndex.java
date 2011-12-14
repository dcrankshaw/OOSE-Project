package edu.jhu.cs.oose.biblio.model.epub;

import nl.siegmann.epublib.domain.Resource;

/**
 * /**
 * This class written by Paul Siegmann and taken from the EpubLib software project.
 * See http://www.siegmann.nl/epublib
 *
 * The search index for a single resource.
 * 
 * @author paul.siegmann
 *
 */
// package
class ResourceSearchIndex {
	private String content;
	private Resource resource;

	public ResourceSearchIndex(Resource resource, String searchContent) {
		this.resource = resource;
		this.content = searchContent;
	}

	public String getContent() {
		return content;
	}

	public Resource getResource() {
		return resource;
	}

}