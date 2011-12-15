package edu.jhu.cs.oose.biblio.model.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Watcher;
import edu.jhu.cs.oose.biblio.model.WatcherEventListener;

public class WatcherTest {

	@Before
	public void setup() {
		WatcherEventListener l = new WatcherEventListener() {

			@Override
			public void directoryModified(Set<File> addedFiles,
					Set<File> deletedFiles) {
				DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				System.out.println(dateFormat.format(cal.getTime()));
				System.out.println("Added files:");
				for (File f : addedFiles) {
					System.out.println(f.getName());
				}
				System.out.println("Deleted Files:\n");
				for (File f : deletedFiles) {
					System.out.println(f.getName());
				}
				System.out.println("\n");

			}
		};
		Watcher w = Watcher.getWatcher();
		w.addListener(l);
	}

	
	@Test
	public void testRun() {
		Watcher w = Watcher.getWatcher();
		File configdir = new File("config");
		assertTrue(configdir.exists());
		Thread myThread = new Thread(w);
		myThread.start();
		List<File> dir = new ArrayList<File>();

		File baseDir = new File("testfiles");
		String newDir = "dirwatch1";
		String myFullDir = baseDir + File.separator + newDir;
		dir.add(new File(myFullDir));
		File testIfDir = new File(baseDir + File.separator + newDir);
		w.addWatchedDirectories(dir);
		assertTrue(testIfDir.isDirectory());
		Set<File> watchedDirs = w.getWatchedDirectories();
		assertTrue(watchedDirs.contains(new File(myFullDir)));
		
		try {
			Thread.sleep(Watcher.UPDATE_DELAY * 5);
		} catch (InterruptedException e) {
			fail();
			e.printStackTrace();
		}

		try {
			FileUtils.copyFile(new File(baseDir + File.separator
					+ "test1.pdf"), new File(baseDir + File.separator
					+ newDir + File.separator + "test1.pdf"));
			FileUtils.copyFile(new File(baseDir + File.separator
					+ "test2.pdf"), new File(baseDir + File.separator
					+ newDir + File.separator + "test2.pdf"));
			FileUtils.copyFile(new File(baseDir + File.separator
					+ "test3.pdf"), new File(baseDir + File.separator
					+ newDir + File.separator + "test3.pdf"));

		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}

		/*************************************************************/
		String secondDir = "dirwatch2";
		dir.add(new File(baseDir + File.separator + secondDir));
		w.addWatchedDirectories(dir);
		watchedDirs = w.getWatchedDirectories();

		try {
			Thread.sleep(Watcher.UPDATE_DELAY * 5);
		} catch (InterruptedException e) {
			fail();
			e.printStackTrace();
		}

		try {
			FileUtils.copyFile(new File(baseDir + File.separator
					+ "sherlockholmes.epub"), new File(baseDir + File.separator
					+ secondDir + File.separator + "sherlockholmes.epub"));
			File fileToDelete = new File(baseDir + File.separator + newDir + File.separator +  "test1.pdf");
			fileToDelete.delete();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		try {
			Thread.sleep(Watcher.UPDATE_DELAY * 5);
		}
		catch(InterruptedException e)
		{	fail();
			e.printStackTrace();
		}
		w.requestStop();

	}
	
	
	@Test
	public void testAddWatchedDirectory() {
		File baseDir = new File("testfiles");
		File f = new File(baseDir + File.separator + "dirwatch3");
		if(!f.isDirectory()) {
			f.delete();
			f.mkdir();
		}
		Watcher w = Watcher.getWatcher();
		List<File> dirToWatch = new ArrayList<File>();
		dirToWatch.add(f);
		w.addWatchedDirectories(dirToWatch);
		Set<File> watchedDirs = w.getWatchedDirectories();
		assertTrue(watchedDirs.contains(f.getAbsoluteFile()));
	}
}