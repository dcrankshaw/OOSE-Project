package edu.jhu.cs.oose.biblio.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * Store a location in a file to be used for a bookmark
 */
@Entity
@Table( name = "LOCATION" )
public class Location implements Keyed {
	
	/** The database's primary key for this Location */
	@Id
	@GenericGenerator(name="generator", strategy="increment")
	@GeneratedValue(generator="generator")
    @Column(name="LOC_ID")
	private int id;
	
	/**
	 * The amount into the file the location is at, relative to the total file length.
	 */
	@Column(name="PERCENTAGE_OF_FILE")
	private float percentageOfFile;
	
	/**
	 * Creates a new, blank Location.
	 * This has protected visibility; use the other constructor instead.
	 * Hibernate uses this, so it's not actually unused.
	 */
	@SuppressWarnings("unused")
	private Location() {
	}
	
	/**
	 * Creates a new Location that is the given amount through a file.
	 * This generates a primary key for the object, so there must be
	 * an open transaction when it its called.
	 * @param pos the amount through the file
	 */
	public Location(float pos) {
		this.percentageOfFile = pos;
		
		Database.getSessionFactory().getCurrentSession().save(this);
		@SuppressWarnings("unchecked")
		Database<Location> db = (Database<Location>)Database.get(Location.class);
		db.add(this);
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the database's primary key for this Location
	 * @param id the new primary key
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Sets the amount through the file this Location refers to.
	 * @param p the new amount into the file
	 */
	public void setPercentageOfFile(float p) {
		this.percentageOfFile = p;
	}
	
	/**
	 * Returns the amount through the file this Location refers to.
	 * @return the amount through the file this location refers to.
	 */
	public float getPercentageOfFile() {
		return percentageOfFile;
	}
}
