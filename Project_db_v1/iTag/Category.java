package iTag;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "CATEGORY" )
public class Category {
	private String name;
	private Set<Tag> tags;
	
	public Category(String n, Set<Tag> t) {
		this.name = n;
		this.tags = t;
	}
}
