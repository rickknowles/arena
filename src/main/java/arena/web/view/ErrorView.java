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
package arena.web.view;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.web.servlet.view.AbstractView;

import arena.utils.ServletUtils;


public class ErrorView extends AbstractView {

    private int errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    private String message;
    private boolean allowRequestArgs = false;

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setAllowRequestArgs(boolean allowRequestArgs) {
        this.allowRequestArgs = allowRequestArgs;
    }

    @SuppressWarnings("unchecked")
    protected void renderMergedOutputModel(Map model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (this.message != null) {
            response.setStatus(this.errorCode);
            response.getWriter().append(ServletUtils.replaceWildcards(
                    this.message, this.allowRequestArgs, model, request));
            response.flushBuffer();
        } else {
            response.sendError(this.errorCode);
        }
    }
}
