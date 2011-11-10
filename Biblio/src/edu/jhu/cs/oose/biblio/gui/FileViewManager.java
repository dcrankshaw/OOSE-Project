package edu.jhu.cs.oose.biblio.gui;

import java.util.HashMap;
import java.util.Map;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * A class that knows about where different files are displayed.
 * When something wants to display something about a file,
 * either its properties window or the full display of the files,
 * it uses this class to do it.
 */
public class FileViewManager {
	/** The object in charge of managing the Properties windows. */
	private static FileViewManager propManager = new FileViewManager();
	/** The object in charge of managing the full display of files. */
	private static FileViewManager viewManager = new FileViewManager();
	
	/** Returns the object that manages the Properties windows.
	 * @return the object that manages the Properties windows.
	 */
	public static FileViewManager getPropertiesManager() {
		return FileViewManager.propManager;
	}
	
	/** Returns the object that manages the full display of files.
	 * @return the object that manages the fullk display of files.
	 */
	public static FileViewManager getViewManager() {
		return FileViewManager.viewManager;
	}
	
	/** The view associated with all of the files
	 * managed by this object.
	 */
	private Map<FileMetadata, FileView> dataToView;
	
	/** The object used to create new windows/displays of files. */
	private FileViewFactory factory;
	
	/** Creates a new manager without the ability to creates
	 * new views.  You have to set the factory later.
	 */
	private FileViewManager() {
		this.factory = null;
		this.dataToView = new HashMap<FileMetadata, FileView>();
	}
	
	/**
	 * Opens the view for this file, creating it if necessary.
	 * @param file the file to display info on
	 */
	public void openFileView(FileMetadata file) {
		if( dataToView.containsKey(file) ) {
			this.dataToView.get(file).makeVisible();
		}
		else {
			FileView view = this.factory.newView(file);
			dataToView.put(file, view);
			view.makeVisible();
		}
	}
	
	/**
	 * Replaces the view for this file with the given view.
	 * This manager now owns the view.
	 * @param file the file to update the view for
	 * @param newView the new view of this file
	 */
	public void setViewFor(FileMetadata file, FileView newView) {
		this.dataToView.put(file, newView);
	}
	
	/**
	 * Removes the view that was associated with this file.
	 * It probably got closed, so it shouldn't exist anymore.
	 * @param f the file whose view should go away.
	 */
	public void removeView(FileMetadata f) {
		this.dataToView.remove(f);
	}
	
	/**
	 * Gives the Manager a way to create new views for files
	 * @param f an object that can create views of files
	 */
	public void setFactory(FileViewFactory f) {
		this.factory = f;
	}
	
}
