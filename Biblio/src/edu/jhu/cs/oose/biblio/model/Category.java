package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.Set;

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
public class Category {
	
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
	
	public Category() {
		super();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "Category -  ID:" + id + " NAME:" + name + "\n";
	}
	
	public Set<Tag> getTag() {
		return tags;
	}
	
	public void addTag(Tag t) {
		tags.add(t);
	}
	
	public Category(String n) {
		this.name = n;
		this.tags = new HashSet<Tag>();
	}
}
