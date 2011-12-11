package edu.jhu.cs.oose.biblio.model.tests;

import java.util.Set;

import junit.framework.TestCase;

import org.hibernate.Session;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.Tag;

/** Tests nontrivial operations on the Tag class */
public class TagTest extends TestCase {
	
	/** Tests to make sure that setName enforces proper syntax. 
	 * @throws Exception */
	public void testSetName() throws Exception {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Tag tag = new Tag("");
		assertFalse(tag.setName("has a :"));
		assertTrue(tag.setName("no colon"));
		session.getTransaction().rollback();
	}
	
	/** Tests to make sure that getAllDescendents finds all descendents 
	 * @throws Exception */
	public void testGetAllDescendants() throws Exception
	{
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
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
		school.addChild(math);
		school.addChild(english);
		math.addChild(numbers);
		math.addChild(one);
		numbers.addChild(onePointFive);
		numbers.addChild(decimal);
		numbers.addChild(integer);
		integer.addChild(one);
		integer.addChild(two);
		decimal.addChild(onePointFive);
		decimal.addChild(twoPointFive);
		english.addChild(letters);
		letters.addChild(a);
		letters.addChild(b);
		letters.addChild(c);
		
		Set<Tag> descendants = math.getAllDescendants();
		assertTrue(descendants.contains(numbers));
		assertTrue(descendants.contains(one));
		assertTrue(descendants.contains(two));
		assertTrue(descendants.contains(integer));
		assertTrue(descendants.contains(decimal));
		assertTrue(descendants.contains(onePointFive));
		assertTrue(descendants.contains(twoPointFive));
		
		session.getTransaction().rollback();
	}
}
