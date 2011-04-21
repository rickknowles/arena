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
package arena.dao.listener;

/**
 * Simple support class that implements all peer listener methods, override only those of relevance 
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class AbstractPeerListener<T> implements DAOInsertListener<T>, DAOUpdateListener<T>, DAODeleteListener<T> {

    public Iterable<T> preInsert(Iterable<T> valueobjects) {return valueobjects;}
    public Iterable<T> preUpdate(Iterable<T> valueobjects) {return valueobjects;}
    public Iterable<T> preDelete(Iterable<T> valueobjects) {return valueobjects;}

    public void postInsert(Iterable<T> valueobjects) {}
    public void postUpdate(Iterable<T> valueobjects) {}
    public void postDelete(Iterable<T> valueobjects) {}

}
