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

/**
 * A ListModel that displays all of the Tag names (in the EditorPanel).
 * It gets its data from the DB.
 */
public class TagEditorListModel implements ListModel {
	/** The interface to the DB.  Provides helper methods */
	private EditorManager manager;
	/** The current list of displayed Tags. */
	private List<Tag> tags;
	/** Objects that should be notified when something happens. */
	private Set<ListDataListener> listeners;
	
	/**
	 * Creates a new ListModel using the given DB interface.
	 * @param m the Manager to use for interfacing with the DB
	 */
	public TagEditorListModel(EditorManager m) {
		this.manager = m;
		tags = new ArrayList<Tag>();
		listeners = new HashSet<ListDataListener>();
		forceUpdate();
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
	
	/**
	 * Sends the given IntervalAdded event to all the listeners
	 * @param event the IntervalAdded event to fire off
	 */
	private void fireIntervalAddedEvent(ListDataEvent event) {
		for( ListDataListener listener : listeners ) {
			listener.intervalAdded(event);
		}
	}
	
	/**
	 * Sends the given IntervalRemoved event to all the listeners
	 * @param event the IntervalAdded event to fire off
	 */
	private void fireIntervalRemovedEvent(ListDataEvent event) {
		for( ListDataListener listener : listeners ) {
			listener.intervalAdded(event);
		}
	}
	
	/**
	 * Sends the given ContentsChanged event to all the listeners
	 * @param event the ContentsChange event to fire off
	 */
	private void fireContentsChangedEvent(ListDataEvent event) {
		for( ListDataListener listener : listeners ) {
			listener.contentsChanged(event);
		}
	}
	
	/**
	 * Forces the model to reload everything from the DB
	 * and update all dependent listeners
	 */
	public void forceUpdate() {
		this.tags.clear();
		if( this.manager.getAllTags() != null ) {
			this.tags.addAll(this.manager.getAllTags());
		}
		Collections.sort(this.tags);
		this.fireContentsChangedEvent(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.tags.size()));
	}
	
	/**
	 * Returns the Tag at the given index in the list
	 * @param idx the index of the desired Tag
	 * @return the Tag at index idx
	 */
	public Tag getTag(int idx) {
		return this.tags.get(idx);
	}
	
	/**
	 * Returns the index of the given tag.
	 * @param t the tag to find
	 * @return the index of t
	 */
	public int indexOf(Tag t) {
		return this.tags.indexOf(t);
	}
	
	/** Creates a new Tag and inserts it into the list.	 */
	public void newTag() {
		Tag newTag = this.manager.newTag();
		this.tags.add(newTag);
		Collections.sort(this.tags);
		int index = this.tags.indexOf(newTag);
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index);
		this.fireIntervalAddedEvent(event);
	}
	
	/**
	 * Deletes the Tag at the given index and removes it from the list.
	 * @param index the Tag to annihilate
	 */
	public void deleteTag(int index) {
		Tag tag = this.tags.get(index);
		this.tags.remove(index);
		ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
		this.fireIntervalRemovedEvent(event);
		this.manager.deleteTag(tag);
	}
}
