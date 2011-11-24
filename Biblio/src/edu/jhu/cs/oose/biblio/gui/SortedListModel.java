package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class SortedListModel<T extends Comparable<? super T> > implements ListModel {
	
	List<T> data;
	Set<ListDataListener> listeners;
	
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
	
	private void emitIntervalAdded(ListDataEvent e) {
		for( ListDataListener l : listeners ) {
			l.intervalAdded(e);
		}
	}
	private void emitIntervalRemoved(ListDataEvent e) {
		for( ListDataListener l : listeners ) {
			l.intervalRemoved(e);
		}
	}
	private void emitContentsChanged(ListDataEvent e) {
		for( ListDataListener l : listeners ) {
			l.contentsChanged(e);
		}
	}
	
	public void add(T item) {
		data.add(item);
		Collections.sort(data);
		int index = data.indexOf(item);
		emitIntervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index));
	}
	
	public void remove(T item) {
		int index = data.indexOf(item);
		data.remove(item);
		emitIntervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index));
	}
}
