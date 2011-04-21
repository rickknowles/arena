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
import java.util.Map;

/**
 * Interface defining the method signatures of any http client adapters.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: HttpClient.java,v 1.3 2006/04/26 16:16:43 rickknowles Exp $
 */
public interface HttpClient {

    public void setUrl(String url);
    public void setPost(boolean isPost);
    public void setRequestEncoding(String encoding);
    
    public void setConnectTimeout(int timeout);
    public void setReadTimeout(int timeout);
    public void setRequestParameters(Map<String,String[]> parameters);
    public void addRequestParameter(String name, String value);
    public void addCookie(String name, String value);
    public void addCookie(String name, String value, int maxAge, String domain, String path);

    public HttpResponse get() throws IOException;
    public void getThreaded();
}
