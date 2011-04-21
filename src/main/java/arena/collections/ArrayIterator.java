/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.collections;

import java.util.Iterator;

/**
 * Walks an array using the iterator pattern
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class ArrayIterator<T> implements Iterator<T> {  
    private T[] data;
    private int offset;
    private int position;
    private int limit;

    public ArrayIterator(T[] in) {
        this(in, 0, in == null ? 0 : in.length);
    }
    public ArrayIterator(T[] in, int length) {
        this(in, 0, length);
    }
    public ArrayIterator(T[] in, int offset, int length) {
        this.data = in;
        this.offset = offset;
        this.limit = offset + length;
        this.position = -1;
    }

    public boolean hasNext() {
        if (this.position < 0) {
            this.position = offset;
            moveToNext();
        }
        return (this.data != null) && (this.position < this.limit);
    }

    public T next() {
        T out = null;
        if (hasNext()) {
            out = this.data[this.position++];
            moveToNext();
        }
        return out;
    }
    
    private void moveToNext() {
        // put skip function here if required
    }

    public void remove() {
        if (this.position > this.offset) {
            // shift backwards
            for (int m = this.position - 1; m < this.limit - 1; m++) {
                this.data[m] = this.data[m + 1];
            }
            this.limit--;
        } else {
            throw new IllegalStateException("next() not yet called");
        }
    }
}
