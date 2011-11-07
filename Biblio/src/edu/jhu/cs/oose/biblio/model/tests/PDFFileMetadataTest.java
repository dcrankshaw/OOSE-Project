package edu.jhu.cs.oose.biblio.model.tests;

import java.io.File;
import java.util.Date;
import java.util.TreeSet;

import junit.framework.TestCase;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;


public class PDFFileMetadataTest extends TestCase {

	PDFFileMetadata searchTestFile;
	
	protected void setUp() throws Exception {
		super.setUp();
		String path = "testfiles/test1.pdf";
		File f = new File(path);
		if(f.exists())
		{
			searchTestFile = new PDFFileMetadata(new Date(), 0, path, new TreeSet<Tag>());
		}
		else
		{
			searchTestFile = null;
		}
		
	}

	public void testGetContents() {
		fail("Not yet implemented");
	}

	public void testSearchText() {
		String search1 = "Gildas"; //String occurs once in document
		String search0 = "kjsdhfkjdsh"; //String occurs 0 times
		String searchMany = "Maxwell";
		assertFalse(searchTestFile == null);
		try
		{
			int results1 = searchTestFile.searchText(search1);
			assertEquals(1, results1);
			assertEquals(0, searchTestFile.searchText(search0));
			int resultsMany = searchTestFile.searchText(searchMany);
			assertTrue(resultsMany > 10);
			System.out.println(resultsMany);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
			fail("Threw unexpected exception");
		}
	}

}
