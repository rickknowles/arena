/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Utility class that stores an extra variable - the actual max list size - so
 * that we can not have to fully populate resultsets for the PagedListController
 * when we know the data will not be used.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: ResultFragmentList.java,v 1.4 2006-12-31 16:27:47 rickk Exp $
 */
public class ResultFragmentList<T> implements List<T> {
//    private final Log log = LogManager.getLog(ResultFragmentList.class);

    private List<T> fragment;
    private int offset;
    private int fullSize;

    public ResultFragmentList() {
        this(new ArrayList<T>());
    }
    
    public ResultFragmentList(List<T> fragment) {
        this.fragment = fragment;
    }

    public int getPartialSize() {
        return offset + this.fragment.size();
    }
    
    public int getFragmentSize() {
        return this.fragment.size();
    }

    public int getFullSize() {
        return fullSize;
    }

    public void setFullSize(int fullSize) {
        this.fullSize = fullSize;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void add(int index, T o) {
        this.fragment.add(index - this.offset, o);
    }

    public boolean add(T o) {
        return this.fragment.add(o);
    }

    public boolean addAll(Collection<? extends T> collection) {
        return this.fragment.addAll(collection);
    }

    public boolean addAll(int index, Collection<? extends T> collection) {
        return this.fragment.addAll(index - this.offset, collection);
    }

    public void clear() {
        this.fragment.clear();
        this.offset = 0;
        this.fullSize = 0;
    }

    public boolean contains(Object o) {
        return this.fragment.contains(o);
    }

    public boolean containsAll(Collection<?> collection) {
        return this.fragment.containsAll(collection);
    }

    public T get(int index) {
        int pos = index - this.offset;
        if ((pos >= 0) && (pos < this.fragment.size())) {
            return this.fragment.get(pos);
        } else {
            return null;
        }
    }

    public int indexOf(Object o) {
        return this.fragment.indexOf(o) + this.offset;
    }

    public boolean isEmpty() {
        return this.fragment.isEmpty() && (this.offset == 0);
    }

    public Iterator<T> iterator() {
        return this.fragment.iterator();
    }

    public int lastIndexOf(Object o) {
        return this.fragment.lastIndexOf(o) + this.offset;
    }

    public ListIterator<T> listIterator() {
        return this.fragment.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return this.fragment.listIterator(index);
    }

    public T remove(int index) {
        return this.fragment.remove(index - this.offset);
    }

    public boolean remove(Object o) {
        return this.fragment.remove(o);
    }

    public boolean removeAll(Collection<?> collection) {
        return this.fragment.removeAll(collection);
    }

    public boolean retainAll(Collection<?> collection) {
        return this.fragment.retainAll(collection);
    }

    public T set(int index, T o) {
        return this.fragment.set(index - this.offset, o);
    }

    public int size() {
        return getFullSize();
    }

    public List<T> subList(int fromIndex, int toIndex) {
//        log.info("Sublist({0}, {1}) called, fragmentSize={2}", fromIndex, toIndex, this.fragment.size());
        return this.fragment.subList(fromIndex - this.offset, toIndex - this.offset);
    }

    public Object[] toArray() {
        return toArray(null);
    }

    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] toArray) { 
        int outSize = size();
        if ((toArray == null) || (toArray.length < outSize)) {
            toArray = (E[])java.lang.reflect.Array.newInstance(
                    toArray.getClass().getComponentType(), outSize);
        }
        Object in[] = this.fragment.toArray();
        System.arraycopy(in, 0, toArray, this.offset, in.length);
        return toArray;
    }

    @Override
    public String toString() {
        return "[ResultFragmentList offset=" + offset + " fullSize=" + fullSize + " fragment=" + this.fragment + "]";
    }
}
