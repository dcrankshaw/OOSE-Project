package edu.jhu.cs.oose.biblio.gui;

import java.util.Collection;
import java.util.Collections;

import javax.swing.event.TableModelEvent;

import edu.jhu.cs.oose.biblio.model.Category;
import edu.jhu.cs.oose.biblio.model.EditorManager;
import edu.jhu.cs.oose.biblio.model.Tag;

// We should combine this with TagTableModel, b/c they're mostly the same
/**
 * The data model of the table that displays
 * the tags matching the current query and selects
 * which ones should be used for filtering.
 */
public class CategoryTableModel extends AbstractTableModel<Category> {

	/** The source of data / interaction with the Database */
	private EditorManager manager;
	/** The currently selected tag in the window.  Show this Tag's categories */
	private Tag selectedTag;
	
	/** Creates a new data model for displaying found tags and filtering.
	 * @param m the interface to the database model
	 */
	public CategoryTableModel(EditorManager m) {
		super();
		manager = m;
		Collection<Category> newCategories = manager.getAllCategories();
		if( newCategories != null ) {
			tags.addAll(manager.getAllCategories());
			Collections.sort(tags);
		}
	}

	@Override
	public String getColumnName(int col) {
		if (col == 0) {
			return "";
		} else if (col == 1) {
			return "Category Name";
		} else {
			return "";
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0) {
			if( selectedTag == null ) {
				return false;
			}
			else {
				return new Boolean(tags.get(row).getTags().contains(selectedTag));
			}
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
	public void setValueAt(Object newValue, int row, int col)
	{
		if(col == 1)
		{
			
		}
		else
		{
			super.setValueAt(newValue, row, col);
		}
	}
	
	/**
	 * Sets the list of Categories that are displayed in this table
	 * @param newCats the new list of Categories to display
	 */
	public void setCategories(Collection<Category> newCats) {
		tags.clear();
		tags.addAll(newCats);
		Collections.sort(tags);
		emitEvent(new TableModelEvent(this));
	}
	
	/** Creates a new Category and adds it to the list currently displayed. */
	public void newCategory() {
		Category newCat = manager.newCategory();
		addCategory(newCat);
	}
	
	/**
	 * Removes from the list and deletes the Category at index idx
	 * @param idx the index of the Category to delete / remove
	 */
	public void removeCategory(int idx) {
		removeCategory(tags.get(idx));
	}
	
	/**
	 * Adds the given Category to the list displayed
	 * @param newCat the new Category to display
	 */
	public void addCategory(Category newCat) {
		tags.add(newCat);
		Collections.sort(tags);
		tags.indexOf(newCat);
		emitEvent(new TableModelEvent(this));
	}
	
	/**
	 * Removes the given Category from the list displayed
	 * @param oldCat the Category to remove
	 */
	public void removeCategory(Category oldCat) {
		tags.remove(oldCat);
		emitEvent(new TableModelEvent(this));
	}
	
	/**
	 * This is the Tag that checking boxes alters
	 * which categories it is in.
	 * @param t the Tag whose Category membership is displayed in this table
	 */
	public void setTag(Tag t) {
		selectedTag = t;
		emitEvent(new TableModelEvent(this));
	}
}