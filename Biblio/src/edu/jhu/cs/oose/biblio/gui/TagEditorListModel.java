package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.Collections;
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
		listeners = new HashSet<ListDataListener>();
		tags.addAll(this.manager.getAllTags());
		Collections.sort(tags);
	}
	
	@Override
	public String getElementAt(int index) {
		return tags.get(index).getName();
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
	
	public void fireIntervalAddedEvent(ListDataEvent event) {
		for( ListDataListener listener : listeners ) {
			listener.intervalAdded(event);
		}
	}
	
	public void fireIntervalRemovedEvent(ListDataEvent event) {
		for( ListDataListener listener : listeners ) {
			listener.intervalAdded(event);
		}
	}
	
	public Tag getTag(int idx) {
		return this.tags.get(idx);
	}
	
	public void newTag() {
		Tag newTag = this.manager.newTag();
		newTag.setName("Hello");
		this.tags.add(newTag);
		Collections.sort(this.tags);
		int index = this.tags.indexOf(newTag);
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
		this.fireIntervalAddedEvent(event);
	}
	
	public void deleteTag(int index) {
		this.tags.remove(index);
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
		this.fireIntervalRemovedEvent(event);
	}
}
