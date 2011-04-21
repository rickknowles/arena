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

import java.util.Map;


/**
 * Defines a user-session object, so that we can use any kind of session
 * object
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: ServerSideUserState.java,v 1.4 2006/10/03 01:34:45 rickknowles Exp $
 */
public interface ServerSideUserState {
    
    /**
     * Retrieve the unique of the user-session. This is often used for user tracking etc.
     */
    public String getId();
    
    /**
     * Retrieve the attribute keyed by the key name.
     */
    public Object getAttribute(String name);

    /**
     * Add or overwrite the variable keyed with name with the object value
     */
    public void setAttribute(String name, Object value);

    /**
     * Remove the value name from the session
     */
    public void removeAttribute(String name);

    /**
     * Returns an unmodifiable map containing a copy of the attributes in the session. 
     */
    public Map<String,Object> getAttributes();
    
    /**
     * Used to generate user-level semaphores if we don't already have one 
     */
    public Object getSemaphore(String name);
    
    public ServerSideUserState cloneServerSideUserState();
    
    public void close();
    
}
