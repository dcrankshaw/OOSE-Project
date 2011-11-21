package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.jhu.cs.oose.biblio.model.EditorManager;
import edu.jhu.cs.oose.biblio.model.Tag;

public class TagEditorListModel implements ListModel {
	
	private EditorManager manager;
	private List<Tag> tags;
	private Set<ListDataListener> listeners;
	
	public TagEditorListModel(EditorManager m) {
		this.manager = m;
		tags = new ArrayList<Tag>();
		tags.addAll(this.manager.getAllTags());
		listeners = new HashSet<ListDataListener>();
		Collections.sort(tags);
	}
	
	@Override
	public Object getElementAt(int index) {
		return tags.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	@Override
	public int getSize() {
		return tags.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}
	
	public void newTag() {
		Tag newTag = manager.newTag();
		tags.add(newTag);
		Collections.sort(tags);
		int index = tags.indexOf(newTag);
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
		for( ListDataListener listener : listeners ) {
			listener.intervalAdded(event);
		}
	}
	
	public void deleteTag(int index) {
		tags.remove(index);
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
		for( ListDataListener listener : listeners ) {
			listener.intervalRemoved(event);
		}		
	}
}
