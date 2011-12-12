package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Tagable {
	/**
	 * Applies a new Tag to this object
	 * @param t the Tag to apply
	 * @return whether the add was successful
	 */
	public abstract boolean addTag(Tag t);
	
	/**
	 * Removes a Tag from this object
	 * @param t the Tag to remove
	 * @return whether the removes was successful
	 */
	public abstract boolean removeTag(Tag t);
	
	/**
	 * Returns a Collection of all of the Tags applied to this object
	 * @return a Collection of all of the Tags applied to this object
	 */
	public abstract Collection<Tag> getTags();
	
	/** The objects listening to changes to this object */
	private Set<TagListener> listeners;
	
	/** Initializes the listeners set */
	public Tagable() {
		listeners = new HashSet<TagListener>();
	}
	
	/**
	 * Adds an object that should be notified to changes to
	 * the Tags applied to this object.
	 * @param l The object to be notified
	 * @return	true if the add was successful
	 * 			false if this object was already added
	 */
	public boolean addListener(TagListener l) {
		if( l == null ) {
			throw new NullPointerException();
		}
		return listeners.add(l);
	}
	
	/**
	 * Removes the listener from this object, so that it will no
	 * longer receive notifications from this object.
	 * @param l the listener to remove
	 * @return whether the listener was actually attached to this object
	 */
	public boolean removeListener(TagListener l) {
		return listeners.remove(l);
	}
	
	/** Emits an event indicating that the name of this object has changed.
	 * This is really only used by Tags themselves, not other things.
	 */
	protected void emitNameChangedEvent() {
		Set<TagListener> currentListeners = new HashSet<TagListener>(this.listeners);
		for( TagListener l : currentListeners ) {
			l.nameChanged(this);
		}
	}
	
	/** Emits an event indicating that the children of this Tag changed. */
	protected void emitChildrenChangedEvent() {
		Set<TagListener> currentListeners = new HashSet<TagListener>(this.listeners);
		for( TagListener l : currentListeners ) {
			l.childrenChanged(this);
		}
	}
}
