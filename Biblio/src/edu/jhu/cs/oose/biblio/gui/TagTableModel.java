package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import edu.jhu.cs.oose.biblio.model.SearchTagsListener;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.TagListener;

/**
 * The data model of the table that displays
 * the tags matching the current query and selects
 * which ones should be used for filtering.
 */
public class TagTableModel implements TableModel, SearchTagsListener {

	/** Objects that need to know when the table should be updated.	 */
	private Set<TableModelListener> tableListeners;
	/** Objects that need to know when the selected tags have changed. */
	private Set<TagSelectionChangedListener> tagSelectionListeners;
	/** The tags that should be displayed */
	private List<Tag> tags;
	/** The tags that have been selected for filtering */
	private Set<Tag> selectedTags;

	private TagListener listener;
	private TagListener childrenChangedListener;
	
	/** Creates a new data model for displaying found tags and filtering. */
	public TagTableModel() {
		tableListeners = new HashSet<TableModelListener>();
		tagSelectionListeners = new HashSet<TagSelectionChangedListener>();
		tags = new ArrayList<Tag>();
		selectedTags = new HashSet<Tag>();
		listener = new TagListener() {
			@Override
			public void nameChanged(Tag t) {
				TagTableModel.this.matchedTags(TagTableModel.this.tags);
			}
			@Override
			public void childrenChanged(Tag t) {}
		};
		childrenChangedListener = new TagListener() {
			@Override
			public void nameChanged(Tag t) {	}
			@Override
			public void childrenChanged(Tag t) {
				emitTagEvent(new TagSelectionChangedEvent(TagTableModel.this, 0, new HashSet<Tag>(TagTableModel.this.selectedTags), null, null));
			}
		};
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
	public void addTagSelectionListener(TagSelectionChangedListener listener)
	{
		tagSelectionListeners.add(listener);
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
	public String getColumnName(int col) {
		if (col == 0) {
			return "";
		} else if (col == 1) {
			return "Tag Name";
		} else {
			return "";
		}
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
	public boolean isCellEditable(int row, int col) {
		if (col == 0) {
			return true;
		} else {
			return false;
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
	public void removeTagSelectionListener(TagSelectionChangedListener listener)
	{
		tagSelectionListeners.remove(listener);
	}

	/** An event describing how the selected tags have changed. */
	public class TagSelectionChangedEvent extends TableModelEvent {
		/** The tags that were selected before this event. */
		Set<Tag> oldTags;
		/** Tags that weren't selected before but are selected now. */
		Set<Tag> newTags;
		/** Tags that were selected before but aren't now.
		 * These are also in oldTags. */
		Set<Tag> removedTags;

		// The event takes ownership of these things and assumes that
		// they will not be changed later, i.e., they are already copies
		/** Creates a new event, with the given changes in selected tags.
		 * @param model the TableModel triggering the event
		 * @param row the row that triggered the event
		 * @param old the set of tags selected before this event
		 * @param n the set of tags that are newly selected
		 * @param gone the set of tags that are no longer selected
		 */
		public TagSelectionChangedEvent(TableModel model, int row,
				Set<Tag> old, Set<Tag> n, Set<Tag> gone) {
			super(model, row);
			oldTags = old;
			newTags = n;
			removedTags = gone;
		}
	}

	@Override
	public void setValueAt(Object newValue, int row, int col) {
		// only the checkbox column is editable by the user
		if( col != 0 ) {
			return;
		}
		if( !(newValue instanceof Boolean) ) {
			return;
		}
		
		// grab a copy of the tags right now, for the event
		Set<Tag> oldTags = new HashSet<Tag>(this.selectedTags);
		Set<Tag> newTags = null;
		Set<Tag> rmTags = null;
		// this cast will always succeed because we do the
		// runtime check just above
		Boolean val = (Boolean)(newValue);
		Tag t = tags.get(row);
		if( val ) {
			selectedTags.add(t);
			newTags = Collections.singleton(t);
			t.addListener(this.childrenChangedListener);
		}
		else {
			selectedTags.remove(t);
			rmTags = Collections.singleton(t);
			t.removeListener(this.childrenChangedListener);
		}
		emitTagEvent(new TagSelectionChangedEvent(this, row, oldTags, newTags, rmTags));
	}

	/**
	 * When the user enters text, then submits it to the SearchManager,
	 * the search manager calls this method
	 */
	@Override
	public void matchedTags(List<Tag> matches) {
		for( Tag t : tags ) {
			t.removeListener(this.listener);
		}
		tags = new ArrayList<Tag>(matches);
		for( Tag t : tags ) {
			t.addListener(this.listener);
		}
		Collections.sort(tags);
		emitEvent(new TableModelEvent(this));
	}

	/**
	 * Sends the given tag changed event to all the listeners.
	 * This also sends the event to the Table Listeners
	 * @param e the event to broadcast.
	 */
	private void emitTagEvent(TagSelectionChangedEvent e) {
		for (TagSelectionChangedListener listener : tagSelectionListeners) {
			listener.tagSelectionChanged(e);
		}
		emitEvent(e);
	}
	
	/**
	 * Sends the given event to all the listeners that need
	 * to know something in the table changed.
	 * @param e the event to broadcast
	 */
	private void emitEvent(TableModelEvent e) {
		for (TableModelListener listener : tableListeners) {
			listener.tableChanged(e);
		}
	}
}