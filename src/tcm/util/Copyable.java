package tcm.util;

public interface Copyable<E> {
	
	/**
	 * Return a copy of this object.  In most cases this is a deep-copy
	 * of the object.
	 */
	public E copy();
	
}
