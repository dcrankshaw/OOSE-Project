package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.CategoryTableModel.CategorySelectionChangedEvent;

/**
 * Listens for changes of which Categories were selected 
 */
public interface CategorySelectionListener {
	/**
	 * Indicates that the selected categories for filtering have changed
	 * @param e the categories and how they have changed
	 */
	public void categorySelectionChanged(CategorySelectionChangedEvent e);

}
