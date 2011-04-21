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
package arena.httpclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base Http client implementation, provides support methods for subclasses
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: JakartaCommonsHttpClientSource.java,v 1.4 2006/04/26 16:16:44 rickknowles Exp $
 */
public abstract class AbstractHttpClient implements HttpClient, Runnable {
    private final Log log = LogFactory.getLog(AbstractHttpClient.class);

    protected String url;
    protected boolean isPost;
    protected String encoding;
    protected Map<String,String[]> requestParameters;
    protected Map<String,Cookie[]> cookies;

    public void addCookie(String name, String value) {
        addCookie(name, value, -1, null, null);
    }
    public void addCookie(String name, String value, int maxAge, String domain, String path) {
        if (this.cookies == null) {
            this.cookies = new HashMap<String,Cookie[]>(); 
        }
        Cookie[] oldValue = this.cookies.get(name);
        if (oldValue == null) {
            this.cookies.put(name, new Cookie[] {new Cookie(name, value, maxAge, domain, path)});
        } else {
            Cookie newValueArr[] = new Cookie[oldValue.length + 1];
            System.arraycopy(oldValue, 0, newValueArr, 0, oldValue.length);
            newValueArr[oldValue.length] = new Cookie(name, value, maxAge, domain, path);
            this.cookies.put(name, newValueArr);
        }
    }
    
    public void addRequestParameter(String name, String value) {
        if (value != null) {
            if (this.requestParameters == null) {
                this.requestParameters = new HashMap<String,String[]>();
            }
            String[] oldValue = this.requestParameters.get(name);
            if (oldValue == null) {
                this.requestParameters.put(name, new String[] {value});
            } else {
                String newValueArr[] = new String[oldValue.length + 1];
                System.arraycopy(oldValue, 0, newValueArr, 0, oldValue.length);
                newValueArr[oldValue.length] = value;
                this.requestParameters.put(name, newValueArr);
            }
        }
    }

    public void setPost(boolean isPost) {
        this.isPost = isPost;
    }

    public void setRequestEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setRequestParameters(Map<String, String[]> parameters) {
        this.requestParameters = parameters;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public abstract HttpResponse get() throws IOException;
    
    public void run() {
//        LogContext.push("httpClient-background");
        try {
            HttpResponse response = get();
            log.trace("HttpClient returned: " + response.getContentString());
        } catch (Throwable err) {
            log.error("ERROR in HttpClient", err);
        } finally {
//            LogContext.pop();
        }
    }

    public void getThreaded() {
        Thread thread = new Thread(this, this.getClass().getName());
        thread.setDaemon(true);
        thread.start();
    }
    
    protected class Cookie {
        public String name;
        public String value;
        public int maxAge;
        public String domain;
        public String path;
        
        Cookie(String name, String value, int maxAge, String domain, String path) {
            this.name = name;
            this.value = value;
            this.maxAge = maxAge;
            this.domain = domain;
            this.path = path;
        }
    }
}
