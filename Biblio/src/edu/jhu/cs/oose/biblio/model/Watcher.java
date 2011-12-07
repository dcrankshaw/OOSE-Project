package edu.jhu.cs.oose.biblio.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton Watcher class. This class runs in a separate thread
 * 
 * @author Daniel
 * 
 */
public class Watcher implements Runnable {
	private Set<File> directories;
	private Set<File> cachedFiles;
	private Set<File> currentState;
	private List<WatcherEventListener> listeners;
	private File configFile;
	private static Watcher myWatcher;
	private boolean stop;

	private Lock directoryListLock;
	private Lock stopLock;

	/** Length of time to sleep between checking for directory changes */
	public static final int UPDATE_DELAY = 1000;

	// singleton accessor
	public static Watcher getWatcher() {
		if (myWatcher == null) {
			myWatcher = new Watcher();
		}
		return myWatcher;
	}

	private Watcher() {
		directoryListLock = new ReentrantLock();
		stopLock = new ReentrantLock();
		List<File> directoryPaths = new ArrayList<File>();
		directories = new TreeSet<File>(new Comparator<File>() {
			public int compare (File f1, File f2)
			{
				return (f1.getAbsoluteFile().compareTo(f2.getAbsoluteFile()));
			}
		});
		stop = false;
		File configDir = new File("config");
		try {
			if (!configDir.isDirectory()) {
				configDir.mkdir();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		String configFilePath = "watch_dir_config";
		configFile = new File(configDir.getName() + File.separator
				+ configFilePath);
		if (configFile.canRead()) {
			// read directories from config file
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						configFile));
				String line = null;
				while ((line = reader.readLine()) != null) {
					directoryPaths.add(new File(line));
				}
				reader.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		addDirectories(directoryPaths);
		cachedFiles = new HashSet<File>();
		currentState = new HashSet<File>();
		listeners = new ArrayList<WatcherEventListener>();

	}

	public void requestStop() {
		try {
			stopLock.lock();
			stop = true;
		} finally {
			stopLock.unlock();
		}

	}

	private void addDirectories(List<File> directoryPaths) {
		for (File current : directoryPaths) {
			if (current.isDirectory()) {
				directories.add(current);
			}
		}
	}

	private void removeDirectories(List<File> paths) {
		for (File currentFile : paths) {
			if (directories.contains(currentFile)) {
				directories.remove(currentFile);
			}
		}
	}

	public void addListener(WatcherEventListener l) {
		listeners.add(l);
	}

	public void removeListener(WatcherEventListener l) {
		listeners.remove(l);
	}

	private void writeConfigFile() {
		try {
			FileWriter writer = new FileWriter(configFile);
			for (File dir : directories) {
				writer.write(dir.getAbsolutePath() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		boolean done = false;
		while (!done) {
			try {
				Thread.sleep(UPDATE_DELAY);
			} catch (InterruptedException e) {
				System.err.println("Watcher thread interrupted: "
						+ e.getLocalizedMessage());
				writeConfigFile();
			}
			try {
				stopLock.lock();
				directoryListLock.lock();
				if (!stop) {
					this.checkForUpdates();
				} else {
					writeConfigFile();
					done = true;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				directoryListLock.unlock();
				stopLock.unlock();
			}

		}
	}

	// TODO currently this does not check recursively for subdirectories, do we
	// want to do this?
	private void checkForUpdates() {
		// get latest state of directory
		getCurrentState();
		// get all files that are now in the directory that weren't previously
		Set<File> addedFiles = new HashSet<File>();
		addedFiles.addAll(currentState);
		addedFiles.removeAll(cachedFiles);

		// get all files that were in directory that no longer are
		Set<File> deletedFiles = new HashSet<File>();
		deletedFiles.addAll(cachedFiles);
		deletedFiles.removeAll(currentState);

		// update stored state to latest version
		cachedFiles = currentState;

		// tell listeners
		fireListeners(addedFiles, deletedFiles);
	}

	public void addWatchedDirectories(List<File> paths) {
		try {
			directoryListLock.lock();
			addDirectories(paths);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			directoryListLock.unlock();
		}
	}

	public void removeWatchedDirectories(List<File> paths) {
		try {
			directoryListLock.lock();
			removeDirectories(paths);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			directoryListLock.unlock();
		}
	}

	public Set<File> getWatchedDirectories() {
		Set<File> watchedDirs = null;
		try {

			directoryListLock.lock();
			watchedDirs = Collections.unmodifiableSet(directories);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			directoryListLock.unlock();
		}
		return watchedDirs;
	}

	private void fireListeners(Set<File> addedFiles, Set<File> deletedFiles) {
		if ((addedFiles.size() > 0) || (deletedFiles.size() > 0)) {
			for (WatcherEventListener l : listeners) {
				l.directoryModified(Collections.unmodifiableSet(addedFiles),
						Collections.unmodifiableSet(deletedFiles));
			}
		}
	}

	private void getCurrentState() {
		currentState.clear();
		for (File currentDir : directories) {
			String[] children = currentDir.list();
			for (String s : children) {
				if(supportedFileType(s)) {
					currentState.add(new File(s));
				}
			}
		}
	}

	//TODO Bad way to do this, we should think of a better one
	private boolean supportedFileType(String s)
	{
		List<String> supportedTypes = new ArrayList<String>();
		supportedTypes.add(".epub");
		supportedTypes.add(".pdf");
		boolean supported = false;
		for(String type: supportedTypes)
		{
			if(s.toLowerCase().endsWith(type)) {
				supported = true;
			}
		}
		return supported;
	}
	
}
