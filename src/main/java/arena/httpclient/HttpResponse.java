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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Response to an HttpClient request
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: HttpResponse.java,v 1.1 2005/09/25 18:49:14 rickknowles Exp $
 */
public class HttpResponse {
    private byte content[];
    private int statusCode; 
    private Map<String,List<String>> headers; // contains a list of strings
    
    public HttpResponse(byte content[], int statusCode, Map<String,List<String>> headers) {
        this.content = content;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public byte[] getContentBytes() {
        return content;
    }
    
    public String getContentString() {
        String encoding = getEncoding();
        if (encoding == null) {
            encoding = "8859_1";
        }
        try {
            return new String(content, encoding);
        } catch (UnsupportedEncodingException err) {
            throw new RuntimeException("Invalid encoding: " + encoding, err);
        }
    }
    
    public String[] getHeaderValues(String name) {
        List<String> values = headers.get(name);
        if (values == null) {
            return null;
        } else {
            return (String[]) values.toArray(new String[0]);
        }
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getEncoding() {
        String encoding = null;
        String contentTypeHeaders[] = getHeaderValues("Content-Type");
        if (contentTypeHeaders != null) {
            int charsetPos = contentTypeHeaders[0].indexOf("charset=");
            if (charsetPos != -1) {
                int endPos = contentTypeHeaders[0].indexOf(";", charsetPos);
                encoding = contentTypeHeaders[0].substring(charsetPos + 8, 
                        endPos == -1 ? contentTypeHeaders[0].length() : endPos);
            }
        }
        return encoding;
    }
}
