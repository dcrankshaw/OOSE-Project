package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
*An group of tags
*/
@Entity
@Table( name = "CATEGORY" )
public class Category {

	/**
	*The name of the Category
	*/
	public String name;

	/**
	*The tags associated with the Category
	*/
	public Set<Tag> tags;

	// this does not copy the set of tags - is that what we want?- Paul
	public Category(String n, Set<Tag> t) {
		this.name = n;
		this.tags = t;
	}
	
	public Category(String n) {
		this.name = n;
		this.tags = new HashSet<Tag>();
	}
}
