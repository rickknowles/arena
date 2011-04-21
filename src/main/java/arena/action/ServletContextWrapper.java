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
import java.util.Map;

import javax.servlet.ServletContext;


/**
 * A request scope wrapper for the session object. This provides
 * ServerSideApplicationState implementation using the ServletContext object for
 * persistence.
 *
 * Note: this object must never exist beyond the scope of a request.
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: ServletContextWrapper.java,v 1.5 2006/10/03 01:34:45 rickknowles Exp $
 */
public class ServletContextWrapper implements ServerSideApplicationState {
    private ServletContext context;

    public ServletContextWrapper(ServletContext context) {
        this.context = context;
    }

    public ServerSideApplicationState cloneServerSideApplicationState() {
        return new ServletContextWrapper(context);
    }
    
    public void close() {
        this.context = null;
    }

    public Object getAttribute(String name) {
        if (context == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            return context.getAttribute(name);
        }
    }

    public void setAttribute(String name, Object value) {
        if (context == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            context.setAttribute(name, value);
        }
    }

    public void removeAttribute(String name) {
        if (context == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            context.removeAttribute(name);
        }
    }

    public Map<String,Object> getAttributes() {
        if (context == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            Map<String,Object> output = new HashMap<String,Object>();
            for (Enumeration<?> names = context.getAttributeNames(); names.hasMoreElements();) {
                String name = (String) names.nextElement();
                output.put(name, context.getAttribute(name));
            }
            return Collections.unmodifiableMap(output);
        }
    }
    
    /**
     * Used to generate semaphores if we don't already have one 
     */
    public Object getSemaphore(String name) {
        if (context == null) {
            throw new RuntimeException("Wrapper already disposed - illegal operation");
        } else {
            Object lock = null;
            synchronized (context) {
                String key = "semaphore:"  + name;
                lock = context.getAttribute(key);
                if (lock == null) {
                    lock = new Boolean(true);
                    context.setAttribute(key, lock);
                }
            }
            return lock;
        }
    }
}
