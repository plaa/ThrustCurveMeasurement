package tcm.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.sf.openrocket.util.AbstractChangeSource;

/**
 * A List implementation that delegates to another list, and
 * additionally acts as a ChangeSource, firing events whenever the
 * list is changed.
 * 
 * Caveat:  Changes made to the list through the use of subList()
 * do not cause events to be fired.
 */
public class ChangeSourceList<E> extends AbstractChangeSource implements List<E> {
	
	private final List<E> list;
	
	public ChangeSourceList(List<E> list) {
		this.list = list;
	}
	
	
	@Override
	public int size() {
		return list.size();
	}
	
	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}
	
	@Override
	public Iterator<E> iterator() {
		return new SubIterator(list.iterator());
	}
	
	@Override
	public Object[] toArray() {
		return list.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
	
	@Override
	public boolean add(E e) {
		boolean ret = list.add(e);
		fireChangeEvent();
		return ret;
	}
	
	@Override
	public boolean remove(Object o) {
		boolean ret = list.remove(o);
		fireChangeEvent();
		return ret;
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean ret = list.addAll(c);
		fireChangeEvent();
		return ret;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean ret = list.addAll(index, c);
		fireChangeEvent();
		return ret;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean ret = list.removeAll(c);
		fireChangeEvent();
		return ret;
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		boolean ret = list.retainAll(c);
		fireChangeEvent();
		return ret;
	}
	
	@Override
	public void clear() {
		list.clear();
		fireChangeEvent();
	}
	
	@Override
	public E get(int index) {
		return list.get(index);
	}
	
	@Override
	public E set(int index, E element) {
		E ret = list.set(index, element);
		fireChangeEvent();
		return ret;
	}
	
	@Override
	public void add(int index, E element) {
		list.add(index, element);
		fireChangeEvent();
	}
	
	@Override
	public E remove(int index) {
		E ret = list.remove(index);
		fireChangeEvent();
		return ret;
	}
	
	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}
	
	@Override
	public ListIterator<E> listIterator() {
		return list.listIterator();
	}
	
	@Override
	public ListIterator<E> listIterator(int index) {
		return list.listIterator(index);
	}
	
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		System.err.println("WARNING:  Changes made to a ChangeSourceList through the use of subList do not cause events to be fired!");
		return list.subList(fromIndex, toIndex);
	}
	
	private class SubIterator implements Iterator<E> {
		
		private final Iterator<E> iterator;
		
		public SubIterator(Iterator<E> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		@Override
		public E next() {
			return iterator.next();
		}
		
		@Override
		public void remove() {
			iterator.remove();
			fireChangeEvent();
		}
		
	}
	
}
