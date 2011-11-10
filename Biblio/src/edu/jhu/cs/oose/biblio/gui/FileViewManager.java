package edu.jhu.cs.oose.biblio.gui;

import java.util.HashMap;
import java.util.Map;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

public class FileViewManager {
	private static FileViewManager propManager = new FileViewManager();
	private static FileViewManager viewManager = new FileViewManager();
	
	public static FileViewManager getPropertiesManager() {
		return FileViewManager.propManager;
	}
	
	public static FileViewManager getViewManager() {
		return FileViewManager.viewManager;
	}
	
	Map<FileMetadata, FileView> dataToView;
	
	FileViewFactory factory;
	
	private FileViewManager() {
		this.factory = null;
		this.dataToView = new HashMap<FileMetadata, FileView>();
	}
	
	
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
	
	public void setViewFor(FileMetadata file, FileView newView) {
		this.dataToView.put(file, newView);
	}
	
	public void setFactory(FileViewFactory f) {
		this.factory = f;
	}
	
	public void removeView(FileMetadata f) {
		this.dataToView.remove(f);
	}
}
