package edu.jhu.cs.oose.biblio.model.tests;

import java.util.Set;

import junit.framework.TestCase;
import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

// TODO This class needs to check the results of the methods
public class TagTest extends TestCase {
	
	Tag tag, tag1;
	Bookmark bkmk;
	FileMetadata file;
	
	public void testSetName() {
		tag = new Tag();
		assertFalse(tag.setName("has a :"));
		assertTrue(tag.setName("no colon"));
	}
	
	
	public void testGetAllDescendants()
	{
		Tag math = new Tag("math");
		Tag numbers = new Tag("numbers");
		Tag one = new Tag("one");
		Tag two = new Tag("two");
		Tag integer = new Tag("integer");
		Tag decimal = new Tag("decimal");
		Tag onePointFive = new Tag("1.5");
		Tag twoPointFive = new Tag("2.5");
		Tag english = new Tag("english");
		Tag letters = new Tag("letters");
		Tag a = new Tag("a");
		Tag b = new Tag("b");
		Tag c = new Tag("c");
		Tag school = new Tag("school");
		school.addChildren(math);
		school.addChildren(english);
		math.addChildren(numbers);
		math.addChildren(one);
		numbers.addChildren(onePointFive);
		numbers.addChildren(decimal);
		numbers.addChildren(integer);
		integer.addChildren(one);
		integer.addChildren(two);
		decimal.addChildren(onePointFive);
		decimal.addChildren(twoPointFive);
		english.addChildren(letters);
		letters.addChildren(a);
		letters.addChildren(b);
		letters.addChildren(c);
		
		Set<Tag> descendants = math.getAllDescendants();
		assertTrue(descendants.contains(numbers));
		assertTrue(descendants.contains(one));
		assertTrue(descendants.contains(two));
		assertTrue(descendants.contains(integer));
		assertTrue(descendants.contains(decimal));
		assertTrue(descendants.contains(onePointFive));
		assertTrue(descendants.contains(twoPointFive));
		
		
		
	}
	

	
}
