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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

/**
 * A collection of utility methods for dealing with URLs, cookies and other servlet related
 * entities
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class ServletUtils {
   
    public static String getCookieValue(HttpServletRequest req, String cookieName, String defaultValue) {
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (int n = 0; n < cookies.length; n++) {
                if (cookies[n].getName().equals(cookieName)) {
                    return cookies[n].getValue();
                }
            }
        }

        return defaultValue;
    }

    public static String getURIFromRequest(HttpServletRequest request) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        return urlPathHelper.getPathWithinApplication(request);
    }
    public static String getURLFromRequest(HttpServletRequest request) {
        StringBuffer out = request.getRequestURL();
        if (request.getQueryString() != null) {
            out.append("?").append(request.getQueryString());
        }
        return out.toString();
    }
    
    public static String canonicalizeURI(String uri, String webrootPath,
            HttpServletRequest request,
            boolean isPublicURI) {
        
        boolean isInclude = WebUtils.isIncludeRequest(request);
        boolean isForward = (request.getAttribute(WebUtils.FORWARD_PATH_INFO_ATTRIBUTE) != null);
        
        // Check it's not an illegal URL
        File webroot = new File(webrootPath);
        File configFile = new File(webroot, uri); // build a canonical version if we can
        String canonicalURI = FileUtils.constructOurCanonicalVersion(configFile, webroot);
        if (!FileUtils.isDescendant(webroot, configFile, webroot)) {
            return null; // illegal
        } else if (isPublicURI && !isInclude && !isForward && 
                FileUtils.isDescendant(new File(webroot, "WEB-INF"), configFile, webroot)) {
            return null; // don't allow direct access to web-inf
        } else if (isPublicURI && !isInclude && !isForward && 
                FileUtils.isDescendant(new File(webroot, "META-INF"), configFile, webroot)) {
            return null; // don't allow direct access to meta-inf
        } else {
            return canonicalURI;
        }
    }
    
    public static String getRequestCharacterEncoding(HttpServletRequest request) {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) {
            encoding = "8859_1";
        }
        return encoding;
    }

    /**
     * Do a reflective call to the getContextPath method on the context object, since we don't want
     * to require the 2.5 servlet API, but we want to call it if it's available.
     */
    public static String getContextPath(ServletContext context) {
        String contextPath = null;
        try {
            Method methContextPath = context.getClass().getMethod("getContextPath", new Class[0]);
            if (methContextPath.getReturnType().equals(String.class)) {
                contextPath = (String) methContextPath.invoke(context, new Object[0]);
            }
        } catch (Throwable err) {
        }
        return contextPath;
    }
    
    private static final String JNDI_HOST_SETTING = "java:comp/env/hostname";
    
    public static String getHostSetting() {
        return getHostSetting(JNDI_HOST_SETTING, "generator.host");
    }
    
    public static String getHostSetting(String jndiParam, String jvmArg) {
        String hostSetting = null;
        // Get host settings via JNDI if available
        if (jndiParam != null) try {
            InitialContext initial = new InitialContext();
            hostSetting = (String) initial.lookup(jndiParam);
        } catch (Throwable err) { 
            /* Ignore - no jndi available, or no param set */
        }

        if (hostSetting == null) {
            hostSetting = System.getProperty(jvmArg);
        }
        return hostSetting;
    }
    
    public static String replaceWildcards(String pattern, boolean allowRequestArgs, 
            Map<String,Object> model, HttpServletRequest request) {
        int firstWildcard = pattern.indexOf("###");
        if (firstWildcard == -1) {
            return pattern;
        }
        int endOfFirstWildcard = pattern.indexOf("###", firstWildcard + 3);
        if (endOfFirstWildcard == -1) {
            return pattern;
        }
        String key = pattern.substring(firstWildcard + 3, endOfFirstWildcard);
        boolean escapeToken = key.startsWith("!");
        if (escapeToken) {
            key = key.substring(1);
        }
        Object out = model.get(key);
        if ((out == null) && allowRequestArgs) {
            out = request.getParameter(key);
        }
        if (out == null) {
            out = "";
        }
        return pattern.substring(0, firstWildcard) + 
                (escapeToken ? URLUtils.encodeURLToken(out.toString(), request.getCharacterEncoding()) : out) + 
                replaceWildcards(pattern.substring(endOfFirstWildcard + 3), allowRequestArgs, model, request);
    }
}
