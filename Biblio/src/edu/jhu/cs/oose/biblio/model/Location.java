package edu.jhu.cs.oose.biblio.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;


@Entity
@Table( name = "LOCATION" )
/**
* Store a location in a file to be used for a bookmark
*/
public class Location {
	
	@Id
	@GenericGenerator(name="generator", strategy="increment")
	@GeneratedValue(generator="generator")
    @Column(name="LOC_ID")
	private int id;
	
	/**
	 * The amount into the file the location is at, relative to the total file length
	 */
	@Column(name="PERCENTAGE_OF_FILE")
	private float percentageOfFile;
	
	public Location() {
		super();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setPercentageOfFile(float p) {
		this.percentageOfFile = p;
	}
	
	public float getPercentageOfFile() {
		return percentageOfFile;
	}
}
