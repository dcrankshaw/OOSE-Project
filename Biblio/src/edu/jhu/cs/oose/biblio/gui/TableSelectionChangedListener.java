package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.model.Named;

public interface TableSelectionChangedListener<T extends Named> {

	public void selectionChanged(AbstractTableModel<T>.SelectionChangedEvent e);
}
