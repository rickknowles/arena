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
package arena.dao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.collections.ArrayIterable;
import arena.dao.listener.DAODeleteListener;
import arena.dao.listener.DAOInsertListener;
import arena.dao.listener.DAOUpdateListener;


/**
 * Support class that simplifies the implementation of persistence peers to only require the 
 * minimal set of operations. More advanced peers (like the SQL peers) will override to support 
 * batched operations. 
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 * @param <T>
 * @param <PK>
 */
public abstract class DAOSupport<T> extends ReadOnlyDAOSupport<T> implements DAO<T> {
    private final Log log = LogFactory.getLog(DAOSupport.class);

    private DAOInsertListener<T>[] insertListeners;
    private DAOUpdateListener<T>[] updateListeners;
    private DAODeleteListener<T>[] deleteListeners;
    
    public DAOInsertListener<T>[] getInsertListeners() {
        return insertListeners;
    }
    public DAOUpdateListener<T>[] getUpdateListeners() {
        return updateListeners;
    }
    public DAODeleteListener<T>[] getDeleteListeners() {
        return deleteListeners;
    }
    public void setInsertListeners(DAOInsertListener<T>... insertListeners) {
        this.insertListeners = insertListeners;
    }
    public void setUpdateListeners(DAOUpdateListener<T>... updateListeners) {
        this.updateListeners = updateListeners;
    }
    public void setDeleteListeners(DAODeleteListener<T>... deleteListeners) {
        this.deleteListeners = deleteListeners;
    }
    
    @SuppressWarnings("unchecked")
    public void setListeners(Object... listeners) {
        List<DAOInsertListener<T>> inserts = new ArrayList<DAOInsertListener<T>>();
        List<DAOUpdateListener<T>> updates = new ArrayList<DAOUpdateListener<T>>();
        List<DAODeleteListener<T>> deletes = new ArrayList<DAODeleteListener<T>>();
        for (Object listener : listeners) {
            if (listener instanceof DAOInsertListener) {
                inserts.add((DAOInsertListener<T>) listener);
            }
            if (listener instanceof DAOUpdateListener) {
                updates.add((DAOUpdateListener<T>) listener);
            }
            if (listener instanceof DAODeleteListener) {
                deletes.add((DAODeleteListener<T>) listener);
            }
        }
        this.insertListeners = inserts.toArray(new DAOInsertListener[inserts.size()]);
        this.updateListeners = updates.toArray(new DAOUpdateListener[updates.size()]);
        this.deleteListeners = deletes.toArray(new DAODeleteListener[deletes.size()]);
    }
    
    public abstract SelectSQL<T> select();
    
    protected abstract int doInsert(Iterator<T> valueobjects);
    protected abstract int doUpdate(Iterator<T> valueobjects);
    protected abstract int doDelete(Iterator<T> valueobjects);

    public int insert(T... valueobjects) {
        return insert(new ArrayIterable<T>(valueobjects));
    }
    public int update(T... valueobjects) {
        return update(new ArrayIterable<T>(valueobjects));
    }
    public int delete(T... valueobjects) {
        return delete(new ArrayIterable<T>(valueobjects));
    }
    
    public int insert(Iterable<T> valueobjects) {
        Iterable<T> actuallyInsert = valueobjects;
        if (this.insertListeners != null) {
            for (DAOInsertListener<T> pre : this.insertListeners) {
                actuallyInsert = pre.preInsert(actuallyInsert);
            }
        }
        if (actuallyInsert == null) {
            log.debug("No rows to insert - aborting");
            return 0;
        }
        int retVal = doInsert(actuallyInsert.iterator());

        // We only have post insert VOs if we sent at least one
        if (this.insertListeners != null) {
            for (DAOInsertListener<T> post : this.insertListeners) {
                post.postInsert(actuallyInsert);
            }
        }
        return retVal;
    }

    public int update(Iterable<T> valueobjects) {
        Iterable<T> actuallyUpdate = valueobjects;
        if (this.updateListeners != null) {
            for (DAOUpdateListener<T> pre : this.updateListeners) {
                actuallyUpdate = pre.preUpdate(actuallyUpdate);
            }
        }
        int retVal = doUpdate(actuallyUpdate.iterator());

        // We only have post insert VOs if we sent at least one
        if (this.updateListeners != null) {
            for (DAOUpdateListener<T> post : this.updateListeners) {
                post.postUpdate(actuallyUpdate);
            }
        }
        return retVal;
    }

    public int delete(Iterable<T> valueobjects) {
        Iterable<T> actuallyDelete = valueobjects;
        if (this.deleteListeners != null) {
            for (DAODeleteListener<T> pre : this.deleteListeners) {
                actuallyDelete = pre.preDelete(actuallyDelete);
            }
        }
        int retVal = doDelete(actuallyDelete.iterator());

        // We only have post delete VOs if we sent at least one
        if (this.deleteListeners != null) {
            for (DAODeleteListener<T> post : this.deleteListeners) {
                post.postDelete(actuallyDelete);
            }
        }
        return retVal;
    }
    
}
