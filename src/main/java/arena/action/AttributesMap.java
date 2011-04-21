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
package arena.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

/**
 * Special map that mocks an attributes map by using the request's attributes to store stuff 
 * passed around. Avoids the need for a second map.
 * 
 * NOTE: needs synchronization like Hashtable in order to be completely backwards compatible
 */
public class AttributesMap implements Map<String,Object> {
    private final Log log = LogFactory.getLog(AttributesMap.class);

    private final static String ROUTER_ATTS[];
    
    static {
        ROUTER_ATTS = new String[] {
                RouterConstants.USER_AGENT_PARAM, RouterConstants.METHOD_PARAM, 
                RouterConstants.CLIENT_HOSTNAME_PARAM, RouterConstants.CLIENT_IP_ADDR_PARAM, 
                RouterConstants.FULL_REQUEST_URL_PARAM, RouterConstants.QUERY_STRING_PARAM,
                RouterConstants.INTERNAL_WEBAPP_URI_PARAM
                };
        Arrays.sort(ROUTER_ATTS);
    }
    
    private HttpServletRequest request;
    private KeySet keySet;
    
    public AttributesMap(HttpServletRequest request) {
        if (request == null) {
            throw new RuntimeException("request was null");
        }
        this.request = request;
    }
    
    public void clear() {
        throw new RuntimeException("containsValue() not implemented");
    }

    public boolean containsKey(Object key) {
        return keySet().contains(key);
    }

    public boolean containsValue(Object value) {
        throw new RuntimeException("containsValue() not implemented");
    }

    public Set<Map.Entry<String,Object>> entrySet() {
        // used by new HashMap(Map) constructor
        Set<Map.Entry<String,Object>> out = new HashSet<Map.Entry<String,Object>>();
        for (Iterator<String> i = keySet().iterator(); i.hasNext(); ) {
            out.add(new Entry(i.next()));
        }
        return out;
    }

    public Object get(Object key) {
        if (key == null) {
            return null;
        }
        return getFromRequest(key.toString());
    }

    public boolean isEmpty() {
        return false; // never true, because we always have a request
    }

    public Set<String> keySet() {
        synchronized (this) {
            if (this.keySet == null) {
                this.keySet = new KeySet();
            }
        }
        return this.keySet;
    }

    public Object put(String key, Object value) {
        if (key != null) {
            if (value == null) {
                remove(key);
            } else {
                this.request.setAttribute(key.toString(), value);
            }
        }
        return null;
    }

    public void putAll(Map<? extends String,? extends Object> t) {
        if (t != null) {
            for (Iterator<? extends String> i = t.keySet().iterator(); i.hasNext(); ) {
                String key = i.next();
                put(key, t.get(key));
            }
        }
    }

    public Object remove(Object key) {
        if (key != null) {
            this.request.removeAttribute(key.toString());
        }
        return null;
    }

    public int size() {
        return this.keySet().size();
    }

    public Collection<Object> values() {
        List<Object> out = new ArrayList<Object>();
        for (Iterator<String> i = keySet().iterator(); i.hasNext(); ) {
            out.add(getFromRequest(i.next().toString()));
        }
        return out;
    }
    
    private Object getFromRequest(String key) {
        // First, prefer attribute values if available because of overwrites
        Object att = request.getAttribute(key);
        if (att != null) {
            log.trace("Getting param " + key + " from request attributes");
            return att;
        }
        
        // Second, check reserved framework words
        int posOfReserved = Arrays.binarySearch(ROUTER_ATTS, key.toString());
        if (posOfReserved >= 0) {
            log.trace("Getting param " + key + " from fixed router parameters");
            // get based on hidden
            if (key.equals(RouterConstants.USER_AGENT_PARAM)) {
                String agent = request.getHeader("User-Agent");
                if (agent == null) {
                    agent = "(not supplied)";
                }
                return agent;
            } else if (key.equals(RouterConstants.CLIENT_HOSTNAME_PARAM)) {
                return request.getRemoteHost();
            } else if (key.equals(RouterConstants.CLIENT_IP_ADDR_PARAM)) {
                return request.getRemoteAddr();
            } else if (key.equals(RouterConstants.FULL_REQUEST_URL_PARAM)) {
                String qs = request.getQueryString();
                StringBuffer fullRequestURL = request.getRequestURL();
                if ((qs != null) && !qs.equals("")) {
                    fullRequestURL.append('?').append(qs);
                }
                return fullRequestURL.toString();
            } else if (key.equals(RouterConstants.QUERY_STRING_PARAM)) {
                String qs = request.getQueryString();
                return (qs == null ? "" : qs);
            } else if (key.equals(RouterConstants.METHOD_PARAM)) {
                return request.getMethod();
            }
        }
        
        // Third, check url params
        String paramValues[] = request.getParameterValues(key);
        if (paramValues != null) {
            log.trace("Getting param " + key + " from request parameters: length=" + paramValues.length);
            if (paramValues.length == 1) {
                return paramValues[0];
            } else {
                return paramValues;
            }
        }
        
        // Fourth check multipart files if required
        if (request instanceof MultipartRequest) {
            MultipartFile mf = ((MultipartRequest) request).getFile(key);
            if (mf != null) {
                log.trace("Getting param " + key + " from multipart file upload");
                return mf;
            }
        }
        return null;
    }
    
    /**
     * Set implementation to satisfy the keySet() call for this map
     */
    private class KeySet implements Set<String> {

        public boolean contains(Object o) {
            if (o == null) {
                return false;
            }
            String key = o.toString();
            if (Arrays.binarySearch(ROUTER_ATTS, key) >= 0) {
                return true;
            } else if (request.getAttribute(key) != null) {
                return true;
            } else if (request.getParameter(key) != null) {
                return true;
            } else if (request instanceof MultipartRequest) {
                return (((MultipartRequest) request).getFile(key) != null);
            } else {
                return false;
            }
        }

        public boolean isEmpty() {
            return false; // always false because of routerAtts
        }

        public Iterator<String> iterator() {
            return new Iterator<String>() {
                Enumeration<?> params = request.getParameterNames();
                Enumeration<?> atts = request.getAttributeNames();
                Iterator<?> filenames = (request instanceof MultipartRequest) ? 
                        ((MultipartRequest) request).getFileNames() : null;
                int routerAttIndex = 0;
                String consumed;

                public boolean hasNext() {
                    if (consumed == null) {
                        consumed = next();
                    }
                    return (consumed != null);
                }

                public String next() {
                    if (consumed != null) {
                        String out = consumed;
                        consumed = null;
                        return out;
                    } else if (atts != null) {
                        if (atts.hasMoreElements()) {
                            return (String) atts.nextElement();
                        } else {
                            atts = null;
                            return next();
                        }
                    } else if (routerAttIndex < ROUTER_ATTS.length) {
                        String found = ROUTER_ATTS[routerAttIndex++];
                        if (request.getAttribute(found) == null) {
                            return found;
                        } else {
                            return next();
                        }
                    } else if (params != null) {
                        if (params.hasMoreElements()) {
                            String found = (String) params.nextElement();
                            if ((request.getAttribute(found) == null) &&
                                    (Arrays.binarySearch(ROUTER_ATTS, found) < 0)) {
                                return found;
                            }
                        } else {
                            params = null;
                        }
                        return next();
                    } else if (filenames != null) {
                        if (filenames.hasNext()) {
                            String found = (String) filenames.next();
                            if ((request.getParameter(found) == null) &&
                                (request.getAttribute(found) == null) &&
                                    (Arrays.binarySearch(ROUTER_ATTS, found) < 0)) {
                                return found;
                            }
                        } else {
                            filenames = null;
                        }
                        return next();
                    } else {
                        return null;
                    }
                }

                public void remove() {
                    throw new RuntimeException("remove() not implemented");
                }
            };
        }

        public int size() {
            // slow, but necessary in order to resolve overlaps without object creation. 
            // hopefully we use iterators instead
            int size = ROUTER_ATTS.length;
            for (Enumeration<?> e = request.getParameterNames(); e.hasMoreElements(); ) {
                if (Arrays.binarySearch(ROUTER_ATTS, e.nextElement()) < 0) {
                    size++;
                }
            }
            for (Enumeration<?> e = request.getAttributeNames(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                if ((Arrays.binarySearch(ROUTER_ATTS, key) < 0) && (request.getParameter(key.toString()) == null)) {
                    size++;
                }
            }
            if (request instanceof MultipartRequest) {
                MultipartRequest mr = ((MultipartRequest) request);
                for (Iterator<?> filenames = mr.getFileNames(); filenames.hasNext(); ) {
                    Object key = filenames.next();
                    if ((Arrays.binarySearch(ROUTER_ATTS, key) < 0) && 
                            (request.getParameter(key.toString()) == null) &&
                            (request.getAttribute(key.toString()) == null)) {
                        size++;
                    }
                }
            }
            return size;            
        }

        public String[] toArray() {
            return toArray(new String[size()]);
        }

        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            if ((a == null) || (a.length < size())) {
                a = (T[])java.lang.reflect.Array
                .newInstance(a.getClass().getComponentType(), size());
            }
            int n = 0;
            for (Iterator<String> i = iterator(); i.hasNext(); n++) {
                a[n] = (T) i.next();
            }
            return a;
        }
        
        public boolean add(String o) {
            throw new RuntimeException("add() not allowed on keyset");
        }

        public boolean addAll(Collection<? extends String> c) {
            throw new RuntimeException("addAll() not allowed on keyset");
        }

        public void clear() {
            throw new RuntimeException("clear() not allowed on keyset");
        }

        public boolean containsAll(Collection<?> c) {
            throw new RuntimeException("containsAll() not allowed on keyset");
        }

        public boolean remove(Object o) {
            throw new RuntimeException("remove() not allowed on keyset");
        }

        public boolean removeAll(Collection<?> c) {
            throw new RuntimeException("removeAll() not allowed on keyset");
        }

        public boolean retainAll(Collection<?> c) {
            throw new RuntimeException("retainAll() not allowed on keyset");
        }
    }
    
    /**
     * Used in the entrySet() call
     */
    class Entry implements Map.Entry<String,Object> {
        private String key;
        Entry(String key) {
            this.key = key;
        }
        public String getKey() {
            return this.key;
        }

        public Object getValue() {
            return get(key);
        }

        public Object setValue(Object value) {
            return put(key, value);
        }
    }
}
