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
public class TagTableModel extends AbstractTableModel<Tag> implements SearchTagsListener {

	private TagListener listener;
	private TagListener childrenChangedListener;
	
	/** Creates a new data model for displaying found tags and filtering. */
	public TagTableModel() {
		super();
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
				emitEvent(new SelectionChangedEvent(TagTableModel.this, 0, new HashSet<Tag>(TagTableModel.this.selectedTags), null, null));
			}
		};
		this.addSelectionListener(new TableSelectionChangedListener<Tag>(){

			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				if( e.removedTags != null ) {
					for( Tag t : e.removedTags ) {
						t.removeListener(TagTableModel.this.childrenChangedListener);
					}
				}
				if( e.newTags != null ) {
					for( Tag t : e.newTags ) {
						t.addListener(TagTableModel.this.childrenChangedListener);
					}
				}
			}
			
		});
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
		super.tags = new ArrayList<Tag>(matches);
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

}