package edu.jhu.cs.oose.biblio.model;

/** 
 * An object that has a primary key to identify
 * it in the database.  This is not an abstract class because
 * then we would have to change our database schema.
 */
public interface Keyed {
	/**
	 * Returns the primary key of this object.
	 * @return the primary key of this object.
	 */
	public int getId();
}
