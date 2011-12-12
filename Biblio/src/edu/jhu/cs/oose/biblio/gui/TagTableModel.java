package edu.jhu.cs.oose.biblio.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TableModelEvent;

import edu.jhu.cs.oose.biblio.model.SearchTagsListener;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * The data model of the table that displays
 * the tags matching the current query and selects
 * which ones should be used for filtering.
 */
public class TagTableModel extends AbstractTableModel<Tag> implements SearchTagsListener {

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
	
	/**
	 * When the user enters text, then submits it to the SearchManager,
	 * the search manager calls this method
	 */
	@Override
	public void matchedTags(List<Tag> matches) {
		super.tags = new ArrayList<Tag>(matches);
		Collections.sort(tags);
		emitEvent(new TableModelEvent(this));
	}

}