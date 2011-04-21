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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Cookie implementation of a ClientSideUserState object.
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: ClientSideUserState.java,v 1.2 2005/09/16 12:14:02 rickknowles Exp $
 */
public class CookieWrapper implements ClientSideUserState {
//    private final Log log = LogManager.getLog(CookieWrapper.class);

    private HttpServletResponse response;
    private Map<String,Cookie> cookies;
    
    private CookieWrapper(HttpServletResponse response) {
        this.cookies = new Hashtable<String,Cookie>();
        this.response = response;
    }
    
    public CookieWrapper(HttpServletRequest request, HttpServletResponse response) {
        this(response);
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
//                log.info("Cookie found: {0}={1}", cookie.getName(), cookie.getValue());
                this.cookies.put(cookie.getName(), cookie);
            }
        }
    }
    
    public ClientSideUserState cloneClientSideUserState() {
        if (response == null) {
            throw new RuntimeException("Cookie client side user state is closed");
        }
        CookieWrapper me = new CookieWrapper(this.response);
        synchronized (this.cookies) {
            me.cookies.putAll(this.cookies);
        }
        return me;
    }

    public String getAttribute(String name) {
        Cookie c = null;
        synchronized (this.cookies) {
            c = this.cookies.get(name);
        }
        if (c != null) {
            return c.getValue();
        }
        return null;
    }

    public void removeAttribute(String name, String domain, String path) {
        if (response == null) {
            throw new RuntimeException("Cookie client side user state is closed");
        }
        Cookie removed = null;
        synchronized (this.cookies) {
            removed = this.cookies.remove(name);
            if (removed != null) {
                removed.setMaxAge(0);
                removed.setValue("");
                if (domain != null) {
                    removed.setDomain(domain);
                }
                if (path != null) {
                    removed.setPath(path);
                }
                this.response.addCookie(removed);
            }
        }
    }

    public void setAttribute(String name, String value) {
        setAttribute(name, value, -1, null, null);
    }
    public void setAttribute(String name, String value, int maxAge, String domain, String path) {
        if (response == null) {
            throw new RuntimeException("Cookie client side user state is closed");
        }
        Cookie c = null;
        synchronized (this.cookies) {
            c = this.cookies.get(name);
            if (c == null) {
                c = new Cookie(name, value);
                this.cookies.put(name, c);
            } else {
                c.setValue(value);
            }
            c.setMaxAge(maxAge);
            if (domain != null) {
                c.setDomain(domain);
            }
            if (path != null) {
                c.setPath(path);
            }
            this.response.addCookie(c);
        }
    }

    public void close() {
        this.response = null;
    }
    
}
