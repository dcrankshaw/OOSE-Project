package edu.jhu.cs.oose.biblio.model;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * The directory watcher that detects unimported files in a specified folder.
 * 
 *
 */
public class Watcher implements Runnable {
	private String dir;
	private Set<File> dirFiles;
	private Set<File> imported;
	private Set<File> unImported;
	private Set<WatchDirListener> dirListeners;
	SessionFactory sessionFactory;

	public Watcher(String dirPath) {
		dir = dirPath;
		dirFiles = new HashSet<File>();
		imported = new HashSet<File>();
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				this.getFiles();
				this.getImported();
				this.watchDir();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Check if there is any unimported file in the directory.
	 */
	public void watchDir() {
		for (File f : dirFiles){
			if (!imported.contains(f)){
				unImported.add(f);
			}				
		}
		this.fireToImport();
	}
	
	/**
	 * Get all files with proper extension from the folder.
	 */
	public void getFiles() {
		String files;
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles)
			if (f.isFile()) {
				files = f.getName();
				if (files.endsWith(".pdf") || files.endsWith(".PDF")
						|| files.endsWith(".epub") || files.endsWith(".EPUB")) {
					System.out.println(files);
					dirFiles.add(f);
				}
			}
	}
	
	/**
	 * Get all the imported files in the library from database.
	 */
	public void getImported() {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		imported = (Set<File>) session.createQuery("from Files").list();
		// TODO not sure if we have a Files relation table.
		session.getTransaction().commit();
	}

	/**
	 * Add an object that wants to be notified when new import is needed.
	 * @param w
	 */
	public void addDirListener(WatchDirListener w) {
		dirListeners.add(w);
		this.fireToImport();

	}
	/**
	 * Notify all relevant objects that new import is needed.
	 */
	public void fireToImport() {
		for (WatchDirListener w : dirListeners) {
			w.toImport(unImported);
		}
	}

}
