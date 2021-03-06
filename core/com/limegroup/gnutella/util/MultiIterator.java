
// Edited for the Learning branch

package com.limegroup.gnutella.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A MultiIterator object keeps a list of iterators.
 * Give the constructor an array of Iterator objects to make one.
 * Call hasNext() and next() to move through all the iterators, one after the other.
 */
public class MultiIterator implements Iterator {

	protected final Iterator[] iterators;
	protected int current;
	
	public MultiIterator(Iterator[] iterators) {
		this.iterators = iterators;
	}
	
	public void remove() {
		if (iterators.length == 0)
			throw new IllegalStateException();
		
		iterators[current].remove();
	}

	public boolean hasNext() {
		for (int i = 0; i < iterators.length; i++) {
			if (iterators[i].hasNext())
				return true;
		}
		return false;
	}

	public Object next() {
		if (iterators.length == 0)
			throw new NoSuchElementException();
		
		positionCurrent();
		return iterators[current].next();
	}
	
	protected void positionCurrent() {
		while (!iterators[current].hasNext() && current < iterators.length)
			current++;
	}

}
