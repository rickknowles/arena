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


import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

import arena.utils.ServletUtils;


public class DownloadView extends AbstractView {

    private String filenamePattern = "###filename###";
    private String contentParam = "content";
    private boolean allowRequestArgs = false;
    private String mimeType = "application/octet-stream";
    private String mimeTypeParam;
    private boolean closeStream = true;
    
    public void setFilenamePattern(String filenamePattern) {
        this.filenamePattern = filenamePattern;
    }

    public void setAllowRequestArgs(boolean allowRequestArgs) {
        this.allowRequestArgs = allowRequestArgs;
    }

    public void setContentParam(String contentParam) {
        this.contentParam = contentParam;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setCloseStream(boolean closeStream) {
        this.closeStream = closeStream;
    }

    public void setMimeTypeParam(String mimeTypeParam) {
        this.mimeTypeParam = mimeTypeParam;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void renderMergedOutputModel(Map model,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        String mt = (mimeTypeParam != null ? (String) model.get(mimeTypeParam) : this.mimeType);
        if (mt != null) {
            response.setContentType(this.mimeType);
        }
        
        Object contentObj = model.get(this.contentParam);
        String fileName = ServletUtils.replaceWildcards(
                this.filenamePattern, this.allowRequestArgs, model, request);
        if (!fileName.equals("")) {
            String rfc2047Name = javax.mail.internet.MimeUtility.encodeText(fileName, "UTF-8", null);
            String fullHeader = "attachment;filename=" + rfc2047Name;
            response.setHeader("Content-Disposition", fullHeader);
        }
        
        ServletOutputStream out = response.getOutputStream();
        if (contentObj instanceof byte[]) {
            byte[] content = (byte[]) model.get(this.contentParam);
            response.setContentLength(content == null ? 0 : content.length);
            if (content != null && content.length > 0) {
                out.write(content);
            }
        } else if (contentObj instanceof InputStream) {
            InputStream content = (InputStream) contentObj;
            byte[] buffer = new byte[response.getBufferSize()];
            int read = 0;
            while ((read = content.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            if (this.closeStream) {
                content.close();
            }
        }
    }
}
