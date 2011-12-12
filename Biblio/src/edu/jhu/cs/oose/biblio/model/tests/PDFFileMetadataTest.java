package edu.jhu.cs.oose.biblio.model.tests;

import java.io.File;
import java.util.Date;
import java.util.TreeSet;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;


public class PDFFileMetadataTest extends TestCase {

	PDFFileMetadata searchTestFile, getContentFile;
	

	protected void setUp() throws Exception {
		super.setUp();
		String path = "testfiles/test1.pdf";
		File f = new File(path);
		Database.getNewSession();
		if(f.exists())
		{
			searchTestFile = new PDFFileMetadata(path);
		}
		else
		{
			searchTestFile = null;
		}
	}

	public void testGetContents() {
		getContentFile = new PDFFileMetadata("testfiles/test1.pdf");
		Database.rollback();
		assertNotNull(getContentFile.getContents());
		
	}

	public void testSearchText() throws Exception{
		String search1 = "Conclusion"; //String occurs once in document
		String search0 = "kjsdhfkjdsh"; //String occurs 0 times
		String searchMany = "NoSQL";
		
		assertFalse(searchTestFile == null);
		int results1 = searchTestFile.searchText(search1);
		//System.out.println(results1);
		assertEquals(1, results1);
		assertEquals(0, searchTestFile.searchText(search0));
		//System.out.println(searchTestFile.searchText(search0));
		int resultsMany = searchTestFile.searchText(searchMany);
		assertTrue(resultsMany > 10);
		//System.out.println(resultsMany);
		Database.commit();
		
	}

}
