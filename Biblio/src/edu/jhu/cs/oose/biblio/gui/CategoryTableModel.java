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

import edu.jhu.cs.oose.biblio.model.Category;
import edu.jhu.cs.oose.biblio.model.EditorManager;
import edu.jhu.cs.oose.biblio.model.Tag;

// We should combine this with TagTableModel, b/c they're mostly the same
/**
 * The data model of the table that displays
 * the tags matching the current query and selects
 * which ones should be used for filtering.
 */
public class CategoryTableModel implements TableModel {

	/** Objects that need to know when the table should be updated.	 */
	private Set<TableModelListener> tableListeners;
	/** The tags that should be displayed */
	private List<Category> categories;
	/** The tags that have been selected for filtering */
	private Set<Category> selectedCategories;
	/** Things that listen for when a Tag is put into / taken out of a Category */
	private Set<CategorySelectionListener> categorySelectionListeners;
	/** The source of data / interaction with the Database */
	private EditorManager manager;
	/** The currently selected tag in the window.  Show this Tag's categories */
	private Tag selectedTag;
	
	/** Creates a new data model for displaying found tags and filtering.
	 * @param m the interface to the database model
	 */
	public CategoryTableModel(EditorManager m) {
		manager = m;
		tableListeners = new HashSet<TableModelListener>();
		categories = new ArrayList<Category>();
		Collection<Category> newCategories = manager.getAllCategories();
		if( newCategories != null ) {
			categories.addAll(manager.getAllCategories());
			Collections.sort(categories);
		}
		selectedCategories = new HashSet<Category>();
		categorySelectionListeners = new HashSet<CategorySelectionListener>();
	}

	@Override
	public void addTableModelListener(TableModelListener listener) {
		tableListeners.add(listener);
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
			return "Category Name";
		} else {
			return "";
		}
	}

	@Override
	public int getRowCount() {
		return categories.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0) {
			if( selectedTag == null ) {
				return false;
			}
			else {
				return new Boolean(categories.get(row).getTags().contains(selectedTag));
			}
		} else if (col == 1) {
			return categories.get(row).getName();
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
	
	/** An event describing how the selected tags have changed. */
	public class CategorySelectionChangedEvent extends TableModelEvent {
		/** The tags that were selected before this event. */
		Set<Category> oldTags;
		/** Tags that weren't selected before but are selected now. */
		Set<Category> newTags;
		/** Tags that were selected before but aren't now.
		 * These are also in oldTags. */
		Set<Category> removedTags;

		// The event takes ownership of these things and assumes that
		// they will not be changed later, i.e., they are already copies
		/** Creates a new event, with the given changes in selected tags.
		 * @param model the TableModel triggering the event
		 * @param row the row that triggered the event
		 * @param old the set of tags selected before this event
		 * @param n the set of tags that are newly selected
		 * @param gone the set of tags that are no longer selected
		 */
		public CategorySelectionChangedEvent(TableModel model, int row,
				Set<Category> old, Set<Category> n, Set<Category> gone) {
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
		Set<Category> oldTags = Collections.unmodifiableSet(this.selectedCategories);
		Set<Category> newTags = null;
		Set<Category> rmTags = null;
		// this cast will always succeed because we do the
		// runtime check just above
		Boolean val = (Boolean)(newValue);
		Category t = categories.get(row);
		if( val ) {
			selectedCategories.add(t);
			newTags = Collections.singleton(t);
		}
		else {
			selectedCategories.remove(categories.get(row));
			rmTags = Collections.singleton(t);
		}
		emitCategoryEvent(new CategorySelectionChangedEvent(this, row, oldTags, newTags, rmTags));
	}
	
	/**
	 * Adds an object that is notified when a Tag is moved in/out of a Category
	 * @param l listens for Tags moving in/out of Categories
	 */
	public void addCategorySelectionListener(CategorySelectionListener l) {
		categorySelectionListeners.add(l);
	}

	/**
	 * Stops an object from being notified when a Tag is moved in/out of a Category
	 * @param l formerly listened for Tags moving in/out of Categories
	 */
	public void removeCategorySelectionListener(CategorySelectionListener l) {
		categorySelectionListeners.remove(l);
	}
	/**
	 * Sends the given tag changed event to all the listeners.
	 * This also sends the event to the Table Listeners
	 * @param e the event to broadcast.
	 */
	private void emitCategoryEvent(CategorySelectionChangedEvent e) {
		for (CategorySelectionListener listener : categorySelectionListeners) {
			listener.categorySelectionChanged(e);
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
	
	/**
	 * Sets the list of Categories that are displayed in this table
	 * @param newCats the new list of Categories to display
	 */
	public void setCategories(Collection<Category> newCats) {
		categories.clear();
		categories.addAll(newCats);
		Collections.sort(categories);
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
		removeCategory(categories.get(idx));
	}
	
	/**
	 * Adds the given Category to the list displayed
	 * @param newCat the new Category to display
	 */
	public void addCategory(Category newCat) {
		categories.add(newCat);
		Collections.sort(categories);
		categories.indexOf(newCat);
		emitEvent(new TableModelEvent(this));
	}
	
	/**
	 * Removes the given Category from the list displayed
	 * @param oldCat the Category to remove
	 */
	public void removeCategory(Category oldCat) {
		categories.remove(oldCat);
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