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

/**
 * Constants referring to parameter names that are used by the RouterServlet
 */
public interface RouterConstants {
//    public static final String PREFERRED_LANGUAGE_PARAM = "routerServlet.preferredLanguage";
//    public static final String PREFERRED_SKIN_PARAM = "routerServlet.preferredSkin";
    public static final String RAW_PARAMETERS_PARAM = "routerServlet.rawParameters";
    public static final String RAW_BYTES_PARAM = "routerServlet.rawBytes";
    
    public static final String FILENAMES_PARAM = "routerServlet.fileNames";
    public static final String USER_AGENT_PARAM = "routerServlet.userAgent";
    public static final String CLIENT_HOSTNAME_PARAM = "routerServlet.clientHostname";
    public static final String CLIENT_IP_ADDR_PARAM = "routerServlet.clientIPAddress";
    public static final String FULL_REQUEST_URL_PARAM = "routerServlet.fullRequestURL";
    public static final String INTERNAL_WEBAPP_URI_PARAM = "routerServlet.internalWebappURI";
    public static final String QUERY_STRING_PARAM = "routerServlet.queryString";
    public static final String REDIRECT_PARAM = "routerServlet.redirectURL";
    public static final String REQUEST_ID_PARAM = "routerServlet.requestId";
    
    public static final String ALLOWED_METHOD_PARAM = "routerServlet.allowedMethod";
    public static final String METHOD_PARAM = "routerServlet.httpMethod";
}