/**
 * 
 */
package edu.jhu.cs.oose.biblio.model.tests;

import java.io.File;
import java.util.Date;
import java.util.TreeSet;

import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;
import junit.framework.TestCase;

/**
 * 
 *
 */
public class SearchControllerTest extends TestCase {
	
	PDFFileMetadata testFile1,testFile2,testFile3;
	
	protected void setUp() throws Exception {
		super.setUp();
		String path1 = "testfiles/test1.pdf", path2 = "testfiles/test2.pdf", path3 = "testfiles/test3.pdf";
		File f1 = new File(path1), f2 = new File(path2), f3 = new File(path3);
		if(f1.exists())
		{
			testFile1 = new PDFFileMetadata(new Date(), 0, path1, new TreeSet<Tag>());
		}
		else
		{
			testFile1 = null;
		}
		if(f2.exists())
		{
			testFile2 = new PDFFileMetadata(new Date(), 0, path2, new TreeSet<Tag>());
		}
		else
		{
			testFile2 = null;
		}
		if(f3.exists())
		{
			testFile2 = new PDFFileMetadata(new Date(), 0, path3, new TreeSet<Tag>());
		}
		else
		{
			testFile2 = null;
		}//TODO maybe abstract to a class -Cain
		
	}

	public void testGetContents() {
		fail("Not yet implemented");
	}

	public void testSearchText() {
		String search1 = "Gildas"; //String occurs once in document
		String search0 = "kjsdhfkjdsh"; //String occurs 0 times
		String searchMany = "Maxwell";
		assertFalse(testFile1 == null);
		try
		{
			int results1 = testFile1.searchText(search1);
			assertEquals(1, results1);
			assertEquals(0, testFile1.searchText(search0));
			int resultsMany = testFile1.searchText(searchMany);
			assertTrue(resultsMany > 10);
			System.out.println(resultsMany);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();//
			fail("Threw unexpected exception");
		}
	}
	
	public void testSearchTags() {
		fail("Not yet implemented");
		//TODO need to complete searchTag()first
	}
	
}


