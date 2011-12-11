package edu.jhu.cs.oose.biblio.model;

/**
 * An interface that is called when a particular Tag is changed.
 */
public interface TagListener {
	/**
	 * Notification that that Tag tag has changed
	 * @param tag the Tag that has changed
	 */
	public void tagChanged(Tag tag);
}
