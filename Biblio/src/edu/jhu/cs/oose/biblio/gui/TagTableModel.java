package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import edu.jhu.cs.oose.biblio.SearchTagsListener;
import edu.jhu.cs.oose.biblio.model.Tag;

public class TagTableModel implements TableModel, SearchTagsListener {

	private Set<TableModelListener> listeners;
	private List<Tag> tags;
	private Set<Tag> selectedTags;

	public TagTableModel() {
		listeners = new HashSet<TableModelListener>();
		tags = new ArrayList<Tag>();
		selectedTags = new HashSet<Tag>();
	}

	@Override
	public void addTableModelListener(TableModelListener listener) {
		listeners.add(listener);
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
			return "s";
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
		listeners.remove(listener);
	}

	public class TagSelectionChangedEvent extends TableModelEvent {
		Collection<Tag> oldTags;
		Collection<Tag> newTags;
		Collection<Tag> removedTags;

		// The event takes ownership of these things and assumes that
		// they will not be changed later, i.e., they are already copies
		public TagSelectionChangedEvent(TableModel model, int row,
				Collection<Tag> old, Collection<Tag> n, Collection<Tag> gone) {
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
		Collection<Tag> oldTags = Collections.unmodifiableCollection(this.selectedTags);
		Collection<Tag> newTags = null;
		Collection<Tag> rmTags = null;
		// this cast will always succeed because we do the
		// runtime check just above
		Boolean val = (Boolean)(newValue);
		Tag t = tags.get(row);
		if( val ) {
			selectedTags.add(t);
			newTags = Collections.singleton(t);
		}
		else {
			selectedTags.remove(tags.get(row));
			rmTags = Collections.singleton(t);
		}
		emitEvent(new TagSelectionChangedEvent(this, row, oldTags, newTags, rmTags));
	}

	public void matchedTags(Set<Tag> matches) {
		tags = new ArrayList<Tag>(matches);
		Collections.sort(tags);
		emitEvent(new TableModelEvent(this));
	}

	private void emitEvent(TableModelEvent e) {
		for (TableModelListener listener : listeners) {
			listener.tableChanged(e);
		}
	}
}