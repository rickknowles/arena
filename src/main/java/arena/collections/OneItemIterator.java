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

public class OneItemIterator<I> implements Iterator<I> {
    private I item;
    
    public OneItemIterator(I item) {
        this.item = item;
    }
    
    public boolean hasNext() {
        return this.item != null;
    }
    
    public I next() {
        if (this.item != null) {
            I out = this.item;
            this.item = null;
            return out;
        } else {
            return null;
        }
    }
    
    public void remove() {}
}
