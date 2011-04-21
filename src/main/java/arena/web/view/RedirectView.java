/*
 * Generator Runtime Servlet Framework
 * Copyright (C) 2004 Rick Knowles
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
package arena.web.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.util.UrlPathHelper;

import arena.utils.ServletUtils;


public class RedirectView extends AbstractView {
    private final Log log = LogFactory.getLog(RedirectView.class);
    
    private String url;
    private boolean serverSideRedirect = false;
    private boolean allowRequestArgsInURI = false;
    private boolean absoluteURI = true;
    private boolean absoluteURL = false;

    @SuppressWarnings("unchecked")
    protected void renderMergedOutputModel(Map model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String uri = ServletUtils.replaceWildcards(this.url, this.allowRequestArgsInURI, model, request);
        
        if (this.serverSideRedirect) {
            log.info("Server side forwarding to: " + uri);
            getServletContext().getRequestDispatcher(uri).forward(request, response);
        } else {
            if (!this.absoluteURL && this.absoluteURI) {
                UrlPathHelper helper = new UrlPathHelper();
                uri = helper.getContextPath(request) + uri;
            }
            log.info("Client side forwarding to: " + uri);
            response.sendRedirect(uri);
        }
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    public void setServerSideRedirect(boolean serverSideRedirect) {
        this.serverSideRedirect = serverSideRedirect;
    }

    public void setAllowRequestArgsInURI(boolean allowRequestArgsInURI) {
        this.allowRequestArgsInURI = allowRequestArgsInURI;
    }

    public void setAbsoluteURI(boolean absoluteURI) {
        this.absoluteURI = absoluteURI;
    }

    public void setAbsoluteURL(boolean absoluteURL) {
        this.absoluteURL = absoluteURL;
    }
}
