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


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import arena.httpclient.AbstractHttpClient;
import arena.httpclient.HttpResponse;
import arena.utils.URLUtils;


/**
 * This is an adapter around the Apache Commons Http client. 
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: JakartaCommonsHttpClient.java,v 1.11 2006/11/03 16:25:42 rickknowles Exp $
 */
public class JakartaCommonsHttpClient extends AbstractHttpClient {
//    private final Log log = LogManager.getLog(JakartaCommonsHttpClient.class);

    private HttpState state;
    private HttpConnectionManager manager;

    private Integer connectTimeout;
    private Integer readTimeout;
    
    public JakartaCommonsHttpClient(HttpConnectionManager manager) {
        this.manager = manager;
        this.state = new HttpState();
    }

    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }
    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    public HttpResponse get() throws IOException {
        NameValuePair params[] = new NameValuePair[0];
        if (this.requestParameters != null) {
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            for (String key : this.requestParameters.keySet()) {
                String[] multipleValues = this.requestParameters.get(key);
                for (String value : multipleValues) {
                    paramList.add(new NameValuePair(key, value));                
                }
            }
            params = paramList.toArray(new NameValuePair[paramList.size()]);
        }
        
        HttpMethod method = null;
        if (this.isPost) {
            method = new PostMethod(this.url);
            if (this.encoding != null) {
                method.getParams().setContentCharset(this.encoding);
            }
            ((PostMethod) method).setRequestBody(params);
        } else {
            String url = this.url;
            for (int n = 0; n < params.length; n++) {
                url = URLUtils.addParamToURL(url, params[n].getName(), 
                        params[n].getValue(), this.encoding, false);
            }
            method = new GetMethod(url);
            method.setFollowRedirects(true);
            if (this.encoding != null) {
                method.getParams().setContentCharset(this.encoding);
            }
        }
        
        HttpClient client = new HttpClient(this.manager);
        if (this.connectTimeout != null) {
            client.getHttpConnectionManager().getParams().setConnectionTimeout(this.connectTimeout.intValue());
        }
        if (this.readTimeout != null) {
            client.getHttpConnectionManager().getParams().setSoTimeout(this.readTimeout.intValue());
        }
        client.setState(this.state);
        try {
            int statusCode = client.executeMethod(method);
            return new HttpResponse(getResponseBytes(method), statusCode, 
                    buildHeaderMap(method.getResponseHeaders()));
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }
    
    private Map<String,List<String>> buildHeaderMap(Header headers[]) {
        Map<String,List<String>> headerMap = new HashMap<String,List<String>>();
        if (headers != null) {
            for (Header header : headers) {
                List<String> oldValues = headerMap.get(header.getName());
                if (oldValues == null) {
                    oldValues = new ArrayList<String>();
                    headerMap.put(header.getName(), oldValues);
                }
                oldValues.add(header.getValue());
            }
        }
        return headerMap;
    }

    public void addCookie(String name, String value, int maxAge, String domain, String path) {
        org.apache.commons.httpclient.Cookie commonsCookie = new org.apache.commons.httpclient.Cookie();
        commonsCookie.setName(name);
        commonsCookie.setValue(value);
        if (maxAge >= 0) {
            commonsCookie.setExpiryDate(new Date(System.currentTimeMillis() + maxAge));
        }
        if (path != null) {
            commonsCookie.setPath(path);
        }
        if (domain != null) {
            commonsCookie.setDomain(domain);
        }
        this.state.addCookie(commonsCookie);
    }
    
    private byte[] getResponseBytes(HttpMethod method) throws IOException {
        InputStream responseStream = null;
        ByteArrayOutputStream stash = new ByteArrayOutputStream();
        try {
            responseStream = method.getResponseBodyAsStream();
            byte buffer[] = new byte[1024];
            int read = 0;
            while ((read = responseStream.read(buffer)) != -1) {
                stash.write(buffer, 0, read);
            }
            return stash.toByteArray();
        } finally {
            stash.close();
            if (responseStream != null) {
                try {responseStream.close();} catch (IOException err) {}
            }
        }
    }
}
