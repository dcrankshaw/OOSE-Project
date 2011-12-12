package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import edu.jhu.cs.oose.biblio.model.Named;

public abstract class AbstractTableModel<T extends Named> implements TableModel {
	
	/** Objects that need to know when the table should be updated.	 */
	private Set<TableModelListener> tableListeners;
	/** Objects that need to know when the selected tags have changed. */
	private Set<TableSelectionChangedListener<T>> selectionListeners;
	/** The tags that should be displayed */
	protected List<T> tags;
	/** The tags that have been selected for filtering */
	protected Set<T> selectedTags;

	/** Creates a new data model for displaying found tags and filtering. */
	public AbstractTableModel() {
		tableListeners = new HashSet<TableModelListener>();
		selectionListeners = new HashSet<TableSelectionChangedListener<T>>();
		tags = new ArrayList<T>();
		selectedTags = new HashSet<T>();
	}

	@Override
	public void addTableModelListener(TableModelListener listener) {
		tableListeners.add(listener);
	}
	
	/**
	 * Adds the given object to those that will be notified of changes
	 * to the set of selected tags.
	 * @param listener the object to be notified
	 */
	public void addSelectionListener(TableSelectionChangedListener<T> listener)
	{
		selectionListeners.add(listener);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if (col == 0) {
			return Boolean.class;
		} else if (col == 1) {
			return String.class;
		} else {
			return null;
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}


	@Override
	public int getRowCount() {
		return tags.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0) {
			return new Boolean(selectedTags.contains(tags.get(row)));
		} else if (col == 1) {
			return tags.get(row).getName();
		} else {
			return null;
		}
	}

	
	@Override
	public void removeTableModelListener(TableModelListener listener) {
		tableListeners.remove(listener);
	}
	
	/**
	 * Takes this object off the list of objects that will be notified when the
	 * selected tags for filtering change. 
	 * @param listener the object that doesn't want to be notified
	 */
	public void removeSelectionListener(TableSelectionChangedListener<T> listener)
	{
		selectionListeners.remove(listener);
	}

	/** An event describing how the selected items have changed. */
	public class SelectionChangedEvent extends TableModelEvent {
		/** The tags that were selected before this event. */
		Set<T> oldTags;
		/** Tags that weren't selected before but are selected now. */
		Set<T> newTags;
		/** Tags that were selected before but aren't now.
		 * These are also in oldTags. */
		Set<T> removedTags;

		// The event takes ownership of these things and assumes that
		// they will not be changed later, i.e., they are already copies
		/** Creates a new event, with the given changes in selected tags.
		 * @param model the TableModel triggering the event
		 * @param row the row that triggered the event
		 * @param old the set of tags selected before this event
		 * @param n the set of tags that are newly selected
		 * @param gone the set of tags that are no longer selected
		 */
		public SelectionChangedEvent(TableModel model, int row,
				Set<T> old, Set<T> n, Set<T> gone) {
			super(model, row);
			oldTags = old;
			newTags = n;
			removedTags = gone;
		}
	}

	/**
	 * Sends the given tag changed event to all the listeners.
	 * This also sends the event to the Table Listeners
	 * @param e the event to broadcast.
	 */
	protected void emitEvent(SelectionChangedEvent e) {
		for (TableSelectionChangedListener<T> listener : selectionListeners) {
			listener.selectionChanged(e);
		}
		emitEvent((TableModelEvent)e);
		System.out.print(e.getFirstRow());
	}
	
	/**
	 * Sends the given event to all the listeners that need
	 * to know something in the table changed.
	 * @param e the event to broadcast
	 */
	protected void emitEvent(TableModelEvent e) {
		for (TableModelListener listener : tableListeners) {
			listener.tableChanged(e);
		}
	}
}
