package iTag;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table( name = "LOCATION" )
public class Location {
	private float percentageOfFile;
	
	public Location(float p) {
		this.percentageOfFile = p;
	}
}
