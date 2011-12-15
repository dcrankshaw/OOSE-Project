package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;

import edu.jhu.cs.oose.biblio.model.SearchTagsListener;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.TagListener;
import edu.jhu.cs.oose.biblio.model.Tagable;

/**
 * The data model of the table that displays
 * the tags matching the current query and selects
 * which ones should be used for filtering.
 */
public class TagTableModel extends AbstractTableModel<Tag> implements SearchTagsListener {

	private TagListener listener;
	private TagListener childrenChangedListener;
	
	/** Creates a new data model for displaying found tags and filtering. */
	public TagTableModel() {
		super();
		listener = new TagListener() {
			@Override
			public void nameChanged(Tagable t) {
				TagTableModel.this.matchedTags(TagTableModel.this.tags);
			}
			@Override
			public void childrenChanged(Tagable t) {}
		};
		childrenChangedListener = new TagListener() {
			@Override
			public void nameChanged(Tagable t) {	}
			@Override
			public void childrenChanged(Tagable t) {
				emitEvent(new SelectionChangedEvent(TagTableModel.this, 0, new HashSet<Tag>(TagTableModel.this.selectedTags), null, null));
			}
		};
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
	public boolean isCellEditable(int row, int col) {
		if (col == 0) {
			return true;
		} else {
			return false;
		}
	}
	

	/*
	 * When the user enters text, then submits it to the SearchManager,
	 * the search manager calls this method
	 */
	@Override
	public void matchedTags(List<Tag> matches) {
		for( Tag t : tags ) {
			t.removeListener(this.listener);
		}
		if( null != matches ) {
			tags = new ArrayList<Tag>(matches);
		}
		else {
			tags = new ArrayList<Tag>();
		}
		for( Tag t : tags ) {
			t.addListener(this.listener);
		}
		Collections.sort(tags);
		emitEvent(new TableModelEvent(this));
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
		emitEvent(new SelectionChangedEvent(this, row, oldTags, newTags, rmTags));
	}
}