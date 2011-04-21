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
package arena.httpclient.commons;



import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import arena.httpclient.HttpClient;
import arena.httpclient.HttpClientSource;


/**
 * Builds a wrapper around the Jakarta-commons httpclient backend for the
 * G-R's httpclient interface.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: JakartaCommonsHttpClientSource.java,v 1.4 2006/04/26 16:16:44 rickknowles Exp $
 */
public class JakartaCommonsHttpClientSource implements HttpClientSource {
    
    private MultiThreadedHttpConnectionManager manager;
    protected Integer connectTimeout;
    protected Integer readTimeout;

    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }
    
    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }
    
    public JakartaCommonsHttpClientSource() {
        this.manager = new MultiThreadedHttpConnectionManager();
    }

    public HttpClient getClient() {
        return new JakartaCommonsHttpClient(this.manager);
    }
}
