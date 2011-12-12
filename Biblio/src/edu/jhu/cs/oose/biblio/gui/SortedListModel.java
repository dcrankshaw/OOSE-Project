package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * A ListModel (for JLists) that is an Adapter for List<T>.
 * This class also maintains the data in sorted order.
 * @param <T> the type to put in the List
 */
public class SortedListModel<T extends Comparable<? super T> > implements ListModel {
	
	/** The underlying list to Manage */
	private List<T> data;
	/** Objects that should be notified when the List changes */
	private Set<ListDataListener> listeners;
	
	/** Creates a new SortedLisModel with no listeners and an empty List of data. */
	public SortedListModel() {
		listeners = new HashSet<ListDataListener>();
		data = new ArrayList<T>();
	}
	
	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	@Override
	public T getElementAt(int idx) {
		return data.get(idx);
	}

	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}
	
	/**
	 * Sends the given IntervalAdded event to all the listeners
	 * @param e the IntervalAdded event to emit 
	 */
	private void emitIntervalAdded(ListDataEvent e) {
		for( ListDataListener l : listeners ) {
			l.intervalAdded(e);
		}
	}
	/**
	 * Sends the given IntervalRemoved event to all the listeners
	 * @param e the IntervalRemoved event to emit 
	 */
	private void emitIntervalRemoved(ListDataEvent e) {
		for( ListDataListener l : listeners ) {
			l.intervalRemoved(e);
		}
	}
	/*
	 * Sends the given ContentsChanged event to all the listeners
	 * This is never used, so it's commented out
	 * @param e the ContentsChanged event to emit
	 */
	/*private void emitContentsChanged(ListDataEvent e) {
		for( ListDataListener l : listeners ) {
			l.contentsChanged(e);
		}
	}*/
	
	/**
	 * Adds the given item into the correct position in the List
	 * @param item the item to insert into the List
	 */
	public void add(T item) {
		data.add(item);
		Collections.sort(data);
		int index = data.indexOf(item);
		emitIntervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index));
	}
	
	/**
	 * Removes the given item from the List
	 * @param item the item to remove from the List
	 */
	public void remove(T item) {
		int index = data.indexOf(item);
		data.remove(item);
		emitIntervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index));
	}
	
	/** Removes everything from this list */
	public void clear() {
		int size = this.data.size();
		if( size <= 0 ) {
			return;
		}
		this.data.clear();
		emitIntervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, size));
	}
}
