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
package arena.utils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A collection of utility methods for dealing with URLs, cookies and other servlet related
 * entities
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class URLUtils {

    /**
     * Adds extra parameters to a base url in the correct encoded format
     */
    public static String addParamsToURL(String baseURL, String[][] params, 
            String encoding, boolean replace) {

        for (int n = 0; n < params.length; n++) {
            baseURL = addParamToURL(baseURL, params[n][0], params[n][1],
                    encoding, replace);
        }
        
        return baseURL;
    }
    
    public static String addParamToURL(String baseURL, String paramName, 
            String paramValue, String encoding) {
        return addParamToURL(baseURL, paramName, paramValue, encoding, true);
    }
    
    public static String addParamToURL(String baseURL, String paramName, 
            String paramValue, String encoding, boolean replace) {
        StringBuffer out = new StringBuffer();
        Log log = LogFactory.getLog(URLUtils.class);

        String useEncoding = ((encoding == null) ? "ISO-8859-1" : encoding);

        if ((paramName != null) && (paramValue != null)) {
            log.debug("URL encoding param: " + 
                    paramName + "=" + paramValue + " (ignored if null)");

            String encodedParamName = encodeURLToken(paramName, useEncoding);
            String encodedParamValue = encodeURLToken(paramValue, useEncoding);

            out.append(encodedParamName).append("=")
               .append(encodedParamValue).append("&");

            // Remove if replacing
            int paramExistsLoc = baseURL.indexOf(encodedParamName + "=");

            if (replace && (paramExistsLoc != -1)) {
                log.debug("Replacing parameter: " + paramName + 
                        " in URL: " + baseURL + " with value " + paramValue);
                int prevAmpersandPos = baseURL.substring(0, paramExistsLoc)
                                              .lastIndexOf("&");
                int nextAmpersandPos = baseURL.indexOf("&", paramExistsLoc);

                if (nextAmpersandPos != -1) {
                    baseURL = baseURL.substring(0, paramExistsLoc)
                              + baseURL.substring(nextAmpersandPos + 1);
                } else {
                    baseURL = baseURL.substring(0, (prevAmpersandPos == -1) 
                            ? paramExistsLoc : prevAmpersandPos);
                }
            }
        }

        if (out.length() == 0) {
            return baseURL;
        } else if (baseURL.endsWith("?")) {
            return baseURL + out.substring(0, out.length() - 1);
        } else if (baseURL.indexOf("?") == -1) {
            return baseURL + "?" + out.substring(0, out.length() - 1);
        } else {
            return baseURL + "&" + out.substring(0, out.length() - 1);
        }
    }
    
    public static String decodeURLToken(String in, String encoding) {
        if (encoding == null) {
            encoding = "UTF-8";
        }
        try {
            return URLDecoder.decode(in, encoding);
        } catch (UnsupportedEncodingException err) {
            throw new RuntimeException("Error url encoding: " + encoding + 
                    " input=" + in, err);
        }
    }
    
    public static String encodeURLToken(String in, String encoding) {
        if (encoding == null) {
            encoding = "UTF-8";
        }
        try {
            return URLEncoder.encode(in, encoding);
        } catch (UnsupportedEncodingException err) {
            throw new RuntimeException("Error url encoding: " + encoding + 
                    " input=" + in, err);
        }
    }
    
    public static String[][] parseQueryString(String encodedQueryString, String encoding) {
        if (encodedQueryString == null) {
            return new String[0][0];
        }
        String params[] = StringUtils.tokenizeToArray(encodedQueryString, "&");
        List<String[]> out = new ArrayList<String[]>();
        for (String param : params) {
            int equalPos = param.indexOf('=');
            if (equalPos == -1) {
                out.add(new String[] {
                        decodeURLToken(param, encoding), ""
                        });
            } else {
                out.add(new String[] {
                        decodeURLToken(param.substring(0, equalPos), encoding), 
                        decodeURLToken(param.substring(equalPos + 1), encoding)
                        });
            }
        }
        return out.toArray(new String[out.size()][]);
    }

    public static String getServerName() {
        String serverName = "localhost";
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            serverName = localhost.getHostName();
        } catch (Throwable err) {
            LogFactory.getLog(URLUtils.class).error("Error looking up server name", err);
        }
        return serverName;
    }
}
