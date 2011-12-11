package edu.jhu.cs.oose.biblio.model;

/**
 * An interface that is called when a particular Tag is changed.
 */
public interface TagListener {
	/**
	 * Notification that a Tag's name has changed
	 * @param tag the Tag whose name changed
	 */
	public void nameChanged(Tag tag);
	
	/**
	 * Notification that a Tag's implied children have changed
	 * @param tag the Tag that has changed
	 */
	public void childrenChanged(Tag tag);
}
