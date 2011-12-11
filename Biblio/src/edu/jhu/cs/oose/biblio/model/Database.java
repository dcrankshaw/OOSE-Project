package edu.jhu.cs.oose.biblio.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

/**
 * When we execute a query, Hibernate will create new objects for the records in
 * the database. This creates inconsistencies in our application, because now
 * different parts are referencing different objects that refer to the same
 * underlying thing. Instead, we send queries to this object, which executes
 * them and then replaces new objects with ones that have already been created.
 * This class need to know about every class that is stored in the database.
 * 
 * @param <T>
 *            the type that should be returned by the query
 */
public class Database<T extends Keyed> {
	/**
	 * A factory for sessions for interacting with the Database. This is
	 * probably the best place for this. Can we put a decorator on here that
	 * redirects criteria to this class?
	 */
	private static SessionFactory sessionFactory = new Configuration()
			.configure().buildSessionFactory();

	/**
	 * The session that is currently open
	 */
	private static Session session;

	/**
	 * Indicate if the session is currently open
	 */
	private static boolean isOpen;

	/**
	 * Returns the session factory for connecting to the database.
	 * 
	 * @return the session factory for connecting to the database.
	 */
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * A map from a class to the Database object that deals with queries
	 * returning that type.
	 */
	private static Map<Class<?>, Database<?>> caches = createDatabases();

	/**
	 * Creates the mapping from types to the right database for that type.
	 * 
	 * @return a map from types to the database interface for that type
	 * */
	private static Map<Class<?>, Database<?>> createDatabases() {
		HashMap<Class<?>, Database<?>> c = new HashMap<Class<?>, Database<?>>();
		c.put(Bookmark.class, new Database<Bookmark>(Bookmark.class));
		c.put(Category.class, new Database<Category>(Category.class));
		c.put(FileMetadata.class,
				new Database<FileMetadata>(FileMetadata.class));
		c.put(Location.class, new Database<Location>(Location.class));
		c.put(Tag.class, new Database<Tag>(Tag.class));
		return c;
	}

	/**
	 * The type that queries to this database return.
	 */
	private Class<T> criteriaResultType;

	/**
	 * A mapping from primary key to the actual object of things that have
	 * already been created.
	 */
	private Map<Integer, T> objectCache;

	/**
	 * Initializes a new Database interface for queries of the given type. This
	 * should only ever be called from createDatabases(), so it is kind of a
	 * multi-singleton...
	 * 
	 * @param resultType
	 *            the type that queries to this database return
	 */
	private Database(Class<T> resultType) {
		this.criteriaResultType = resultType;
		objectCache = new HashMap<Integer, T>();
	}

	/**
	 * Returns the database that deals with queries for instances of type cl.
	 * 
	 * @param cl
	 *            the type the query should return
	 * @return the database for these kinds of queries
	 */
	public static Database<?> get(Class<?> cl) {
		return caches.get(cl);
	}

	
	
	/**
	 * Executes the given query, attaching all known objects
	 * to the Session before execution to ensure that there is one
	 * copy of each object.
	 * @param q the query to execute
	 * @return the results, using currently existing objects
	 */
	public List<T> executeQuery(Query q) {
		// re-attach all of our objects - TODO is this expensive?
		for( T toAttach : objectCache.values() ) {
			update(toAttach);
		}
		// first, execute the query.  This can't possibly be entirely
		// typesafe, but we must continue anyway, so the warning is suppressed.
		@SuppressWarnings("unchecked")
		List<T> results = (List<T>) q.list();

		// for each object in the list, see if there is already an
		// object with that primary key.  If there is, then it is one that
		// we just attached.  If it's new, then we add it.
		for( int i = 0; i < results.size(); i++ ) {
			T newObj = results.get(i);
			int key = newObj.getId();
			T oldObj = this.objectCache.get(key);

			// If objects that we didn't know about were returned,
			// now we know about them
			if( null == oldObj ) {
				this.add(newObj);
			}
		}
		return results;
	}
	
	/**
	 * Executes the given criteria, attaching all known objects
	 * to the Session before execution to ensure that there is one
	 * copy of each object.
	 * @param c the criteria to execute
	 * @return the results, using currently existing objects
	 */
	public List<T> executeCriteria(Criteria c) {
		// re-attach all of our objects - TODO is this expensive?
		for( T toAttach : objectCache.values() ) {
			update(toAttach);
		}
		// first, execute the query.  This can't possibly be entirely
		// typesafe, but we must continue anyway, so the warning is suppressed.
		@SuppressWarnings("unchecked")
		List<T> results = (List<T>) c.list();
		
		// for each object in the list, see if there is already an
		// object with that primary key.  If there is, then it is one that
		// we just attached.  If it's new, then we add it.
		for( int i = 0; i < results.size(); i++ ) {
			T newObj = results.get(i);
			int key = newObj.getId();
			T oldObj = this.objectCache.get(key);
			
			// If objects that we didn't know about were returned,
			// now we know about them
			if( null == oldObj ) {
				this.add(newObj);
			}
		}
		return results;
	}

	/**
	 * Adds a new object to the memory cache. This object is assumed to be the
	 * original object.
	 * 
	 * @param newObj
	 *            the new object to track
	 */
	public void add(T newObj) {
		this.objectCache.put(newObj.getId(), newObj);
	}

	/**
	 * Removes the given object from the memory cache and the DB
	 * 
	 * @param oldObj
	 *            the object to remove
	 */
	public void delete(T oldObj) {
		this.objectCache.remove(oldObj.getId());
		Database.getSessionFactory().getCurrentSession().delete(oldObj);
	}

	/**
	 * Updates the given object in the DB
	 * 
	 * @param obj
	 *            the object to sync to the DB
	 */
	public static void update(Keyed obj) {
		Database.getSessionFactory().getCurrentSession().update(obj);
	}

	/**
	 * Finds the Tag with the given name if it exists
	 * 
	 * @param name
	 *            the name of the Tag to fine
	 * @return the Tag named name, or null if it does not exist
	 */
	public static Tag getTag(String name) {
		if (!isOpen) {
			Database.getNewSession();
		}
		// TODO cleanse the input, using sql parameters instead of string
		// concatenation
		Criteria crit = session.createCriteria(Tag.class).add(
				Restrictions.eq("name", "%" + name + "%"));
		@SuppressWarnings("unchecked")
		List<Tag> result = ((Database<Tag>) Database.get(Tag.class))
				.executeCriteria(crit);

		if (result.size() <= 0) {
			return null;
		} else if (result.size() == 1) {
			return result.get(0);
		} else {
			System.err.println("Searching for tags named " + name
					+ " yielded multiple results.  Picking the first one.");
			return result.get(0);
		}
	}

	/**
	 * Get the current running session
	 */
	public static Session getSession() {
		if (isOpen) {
			return session;
		} else {
			return null;
		}
	}

	/**
	 * Create a new session and begin transaction
	 */
	public static Session getNewSession() {
		if (isOpen) {
			return null;
		} else {
			session = Database.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			isOpen = true;
			return session;
		}
	}

	/**
	 * Commit
	 */
	public static void commit() {
		if (isOpen) {
			session.getTransaction().commit();
			isOpen = false;
		}
	}

	/**
	 * Rollback
	 */
	public static void rollback() {
		if (isOpen) {
			session.getTransaction().rollback();
			isOpen = false;
		}
	}
}
