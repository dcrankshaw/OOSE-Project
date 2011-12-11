package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
*An group of tags
*/
@Entity
@Table( name = "CATEGORY" )
public class Category implements Comparable<Category>, Keyed {
	
	/** The database's primary key for this Category */
	@Id
	@GenericGenerator(name="generator", strategy="increment")
	@GeneratedValue(generator="generator")
    @Column(name="CAT_ID")
	private int id;
	
	/**
	*The name of the Category
	*/
	@Column(name="NAME")
	public String name;

	/**
	*The tags associated with the Category
	*/
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(
		name="CATEGORY_TAG",
		joinColumns=@JoinColumn(name="CAT_ID", referencedColumnName="CAT_ID"),
		inverseJoinColumns=@JoinColumn(name="TAG_ID", referencedColumnName="TAG_ID")
	)
	public Set<Tag> tags;
	
	/**
	 * Creates a new, empty Category.
	 * This should only be used by Hibernate.
	 * Use the other constructor instead.
	 */
	@SuppressWarnings("unused")
	private Category() {
		tags = new TreeSet<Tag>();
	}
	
	/**
	 * Creates a new Category with the given name.
	 * It gets a primary key for the DB, so
	 * there must be an open transaction
	 * @param n the name of the new Category
	 * @throws Exception If the user tries to create a category with a colon
	 */
	public Category(String n) throws Exception {
		if(n.contains(":"))
			throw new Exception("Invalid tag name");
		this.name = n;
		this.tags = new HashSet<Tag>();
		
		Database.getSessionFactory().getCurrentSession().save(this);
		@SuppressWarnings("unchecked")
		Database<Category> db = (Database<Category>)Database.get(Category.class);
		db.add(this);
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the databases primary key for this Category.
	 * @param id the new primary key
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Returns the name of this category.
	 * @return the name of this category
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of this Category to the given value.  The name
	 * may not include the ':' character, as that denotes the end
	 * of a Category name when searching.  Attempting to set a name
	 * with a colon will fail
	 * @param n the new name of this Category
	 * @return true if this is a valid Category name, or false if the name
	 * contains a colon and is not a valid name.
	 */
	public boolean setName(String n) {
		if(n.contains(":"))
			return false;
		else {
			this.name = n;
			return true;
		}
	}
	
	@Override
	public String toString() {
		return "Category -  ID:" + id + " NAME:" + name + "\n";
	}
	
	/**
	 * Returns the set of Tags that are in this Category
	 * @return the set of Tags that are in this Category
	 */
	public Set<Tag> getTags() {
		return tags;
	}
	
	/**
	 * Adds the given Tag to this Category
	 * @param t the Tag to add to this Category
	 */
	public void addTag(Tag t) {
		tags.add(t);
	}
	
	@Override
	public int compareTo(Category other) {
		return getName().compareTo(other.getName());
	}
}
