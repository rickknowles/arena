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
 * Generates ArrayIterator on demand
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class ArrayIterable<T> implements Iterable<T> {  

    private T items[];
    private int offset;
    private int length;

    public ArrayIterable(T[] in) {
        this(in, 0, in == null ? 0 : in.length);
    }
    public ArrayIterable(T[] in, int length) {
        this(in, 0, length);
    }
    public ArrayIterable(T[] in, int offset, int length) {
        this.items = in;
        this.offset = offset;
        this.length = length;
    }
    
    public Iterator<T> iterator() {
        // needs to be created every time, so we can't create this in the constructor
        return new ArrayIterator<T>(items, offset, length);
    }
}
