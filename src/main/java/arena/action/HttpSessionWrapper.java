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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * A request scope wrapper for the session object. This provides
 * ServerSideUserState implementation using the HttpSession object for
 * persistence.
 *
 * Note: this object must never exist beyond the scope of a request.
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: HttpSessionWrapper.java,v 1.6 2006/10/03 01:34:45 rickknowles Exp $
 */
public class HttpSessionWrapper implements ServerSideUserState {
    private HttpServletRequest request;
    private HttpSession session;

    /**
     * Constructor. The request supplied is used to obtain a session object on demand.
     */
    public HttpSessionWrapper(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Marks this wrapper as closed, and prevents any further usage by user threads. It effectively
     * stops user-spawned threads from illegally holding a reference to the session object outside
     * of it's intended scope.
     */
    public void close() {
        this.request = null;
    }

    public ServerSideUserState cloneServerSideUserState() {
        HttpSessionWrapper copy = new HttpSessionWrapper(request);
        copy.session = session;
        return copy;
    }

    /**
     * Retrieve the unique of the user-session. This is often used for user tracking etc.
     */
    public String getId() {
        if (request == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        }
        if (session == null) {
            session = request.getSession(true); 
        }
        return session.getId();
    }

    /**
     * Retrieve the attribute keyed by the key name. Note this doesn't create a session if 
     * one doesn't already exist
     */
    public Object getAttribute(String name) {
        if (request == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        }
        // Don't allocate a new session for a simple read
        if (session == null) {
            session = request.getSession(false); 
        }
        if (session == null) {
            return null;
        } else {
            return session.getAttribute(name);
        }
    }

    /**
     * Add or overwrite the variable keyed with name with the object value. Note that if
     * we try to set a null value, this method does not forceably instantiate a session,
     * which it otherwise does.
     */
    public void setAttribute(String name, Object value) {
        if (request == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        }
        
        // only forceably create a session when we have a non-null value to insert
        if (value != null) {
            if (session == null) {
                session = request.getSession(true); 
            }
            session.setAttribute(name, value);
        }
        // if we don't have non-null value, ignore the operation if there is no session
        else {
            if (session == null) {
                session = request.getSession(false);
            }
            if (session != null) {
                session.setAttribute(name, value);
            }
        }
    }

    /**
     * Remove the value name from the session. Note this doesn't create a session if 
     * one doesn't already exist
     */
    public void removeAttribute(String name) {
        if (request == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        }
        if (session == null) {
            session = request.getSession(false);
        }
        if (session != null) {
            session.removeAttribute(name);
        }
    }

    /**
     * Returns an unmodifiable map containing a copy of the attributes in the session. Note
     * that this method doesn't create a new HttpSession, it only reads from one if it exists,
     * simply returning an empty map if it doesn't.
     */
    public Map<String,Object> getAttributes() {
        if (request == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        }
        
        if (session == null) {
            session = request.getSession(false);
        }
        
        Map<String,Object> output = new HashMap<String,Object>();
        if (session != null) {
            List<?> attNames = Collections.list((Enumeration<?>)session.getAttributeNames());
            for (Iterator<?> i = attNames.iterator(); i.hasNext(); ) {
                String name = (String) i.next();
                output.put(name, session.getAttribute(name));
            }
        }
        return Collections.unmodifiableMap(output);
    }
    
    /**
     * Used to generate semaphores if we don't already have one 
     */
    public Object getSemaphore(String name) {
        if (request == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            if (session == null) {
                session = request.getSession(true);
            }
            Object lock = null;
            synchronized (session) {
                String key = "semaphore:"  + name;
                lock = session.getAttribute(key);
                if (lock == null) {
                    lock = new Boolean(true);
                    session.setAttribute(key, lock);
                }
            }
            return lock;
        }
    }
}
