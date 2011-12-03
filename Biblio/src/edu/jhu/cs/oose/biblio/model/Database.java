package edu.jhu.cs.oose.biblio.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;

/**
 * When we execute a query, Hibernate will create new objects for the records in
 * the database.  This creates inconsistencies in our application, because now
 * different parts are referencing different objects that refer to the same
 * underlying thing.  Instead, we send queries to this object, which executes
 * them and then replaces new objects with ones that have already been
 * created.  This class need to know about every class that is stored in the
 * database.
 * 
 * @param <T> the type that should be returned by the query
 */
public class Database<T> {
	
	/**
	 * A map from a class to the Database object that
	 * deals with queries returning that type. 
	 */
	private static Map<Class<?>, Database<?>> caches = createDatabases();
	
	/**
	 * Creates the mapping from types to the right database for that type. 
	 * @return a map from types to the database interface for that type 
	 * */
	private static Map<Class<?>, Database<?>> createDatabases() {
		HashMap<Class<?>, Database<?>> c = new HashMap<Class<?>, Database<?>>();
		c.put(Bookmark.class, new Database<Bookmark>(Bookmark.class));
		c.put(Category.class, new Database<Category>(Category.class));
		c.put(FileMetadata.class, new Database<FileMetadata>(FileMetadata.class));
		c.put(Location.class, new Database<Location>(Location.class));
		c.put(Tag.class, new Database<Tag>(Tag.class));
		return c;
	}
	
	/**
	 * The type that queries to this database return.
	 */
	private Class<T> criteriaResultType;
	
	/**
	 * A mapping from primary key to the actual object
	 *  of things that have already been created.
	 */
	private Map<Integer, T> objectCache; 
	
	/**
	 * Initializes a new Database interface for queries of the given type.
	 * This should only ever be called from createDatabases(), so it is kind
	 * of a multi-singleton...
	 * @param resultType the type that queries to this database return
	 */
	private Database(Class<T> resultType) {
		this.criteriaResultType = resultType;
		objectCache = new HashMap<Integer, T>();
	}
	
	/**
	 * Returns the database that deals with queries for instances
	 * of type cl.
	 * @param cl the type the query should return
	 * @return the database for these kinds of queries
	 */
	public static Database<?> get(Class<?> cl) {
		return caches.get(cl);
	}
	
	/**
	 * Executes the given criteria and replaces the results
	 * with already existing objects if possible.
	 * @param c the criteria to execute
	 * @return the results, using currently existing objects
	 */
	public List<T> executeCriteria(Criteria c) {
		return null;
	}
}
