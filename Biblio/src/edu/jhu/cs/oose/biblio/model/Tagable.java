package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class Tagable {
	public abstract boolean addTag(Tag t);
	public abstract boolean removeTag(Tag t);
	public abstract Collection<Tag> getTags();
	
	private Set<TagListener> listeners;
	
	public Tagable() {
		listeners = new HashSet<TagListener>();
	}
	
	public boolean addListener(TagListener l) {
		if( l == null ) {
			throw new NullPointerException();
		}
		return listeners.add(l);
	}
	
	public boolean removeListener(TagListener l) {
		return listeners.remove(l);
	}
	
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
