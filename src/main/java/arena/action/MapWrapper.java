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
package arena.action;

import java.util.Hashtable;
import java.util.Map;

/**
 * A request scope wrapper for the session object. This provides
 * UserSession implementation using the HttpSession object for
 * persistence.
 *
 * Note: this object must never exist beyond the scope of a request.
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: MapWrapper.java,v 1.4 2006/10/03 01:34:45 rickknowles Exp $
 */
public class MapWrapper implements ServerSideUserState, ServerSideApplicationState {
    private Map<String,Object> contents;
    private String id;

    public MapWrapper(Map<String,Object> contents) {
        this.contents = contents;
        this.id = "" + System.currentTimeMillis();
    }

    public void close() {
        this.contents = null;
        this.id = null;
    }

    public ServerSideApplicationState cloneServerSideApplicationState() {
        return new MapWrapper(contents);
    }

    public ServerSideUserState cloneServerSideUserState() {
        return new MapWrapper(contents);
    }
    
    public String getId() {
        if (id == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            return id;
        }
    }
    
    public Object getAttribute(String name) {
        if (contents == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            return contents.get(name);
        }
    }

    public void setAttribute(String name, Object value) {
        if (contents == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            contents.put(name, value);
        }
    }

    public void removeAttribute(String name) {
        if (contents == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            contents.remove(name);
        }
    }

    public Map<String,Object> getAttributes() {
        if (contents == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            return new Hashtable<String,Object>(contents);
        }
    }
    
    /**
     * Used to generate user-level semaphores if we don't already have one 
     */
    public Object getSemaphore(String name) {
        if (contents == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            Object lock = null;
            synchronized (this) {
                String key = "semaphore:"  + name;
                lock = contents.get(key);
                if (lock == null) {
                    lock = new Boolean(true);
                    contents.put(key, lock);
                }
            }
            return lock;
        }
    }
}
