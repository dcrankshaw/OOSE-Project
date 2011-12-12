package edu.jhu.cs.oose.biblio.model.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;

public class EpubFileMetadataTest {
	EpubFileMetadata e;
	
	@Before
	public void setUp() throws Exception {
		Database.getNewSession();
		String path = "testfiles/sherlockholmes.epub";
		File f = new File(path);
		Database.getNewSession();
		if(f.exists())
		{
			e = new EpubFileMetadata(path);
		}
		else
		{
			e = null;
		}
	}

	@Test
	public void testSearchText() throws Exception {
		//System.out.println(e.searchText("detective"));
		assertTrue(e.searchText("detective")==7);
	}

}
