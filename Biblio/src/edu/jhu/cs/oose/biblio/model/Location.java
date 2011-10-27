package edu.jhu.cs.oose.biblio.model;


import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table( name = "LOCATION" )
/**
* Store a location in a file to be used for a bookmark
*/
public class Location {

	/**
	 * The amount into the file the location is at, relative to the total file length
	 */
	public float percentageOfFile;
	
	
	//TODO
	public Location(float p) {
		this.percentageOfFile = p;
	}
}
