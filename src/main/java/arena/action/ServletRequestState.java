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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.springframework.web.multipart.MultipartFile;

import arena.utils.FileUtils;
import arena.utils.ServletUtils;


public class ServletRequestState implements RequestState {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;
    private Map<String,Object> attributesMap;
    private ServerSideUserState serverSideUserState;
    private ServerSideApplicationState serverSideApplicationState;
    private ClientSideUserState clientSideUserState;
    
    public ServletRequestState(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        this.request = request;
        this.response = response;
        this.servletContext = context;
        this.attributesMap = new AttributesMap(request);
        this.serverSideUserState = new HttpSessionWrapper(request);
        this.serverSideApplicationState = new ServletContextWrapper(context);
        this.clientSideUserState = new CookieWrapper(request, response);
    }

    @Override
    public ServerSideUserState getServerSideUserState() {
        return this.serverSideUserState;
    }

    @Override
    public ServerSideApplicationState getServerSideApplicationState() {
        return this.serverSideApplicationState;
    }

    @Override
    public ClientSideUserState getClientSideUserState() {
        return this.clientSideUserState;
    }
    
    public Map<String,Object> actionValuesAsMap() {
        return this.attributesMap;
    }

    public Iterator<String> actionKeys() {
        return this.attributesMap.keySet().iterator();
    }

    public Object getArg(String key) {
        return this.attributesMap.get(key);
    }

    public void set(String key, Object value) {
        setArg(key, value);
    }

    public void setArg(String key, Object value) {
        this.attributesMap.put(key, value);
    }
    
    public void removeArg(String key) {
        this.attributesMap.remove(key);
    }

    public Iterator<String> getArgNames() {
        return this.attributesMap.keySet().iterator();
    }

    public Float getArg(String key, Float defaultValue) {
        Object arg = this.attributesMap.get(key);
        if (arg == null) {
            return defaultValue;
        } else if (arg instanceof Number) {
            return ((Number) arg).floatValue();
        } else {
            return Float.valueOf(arg.toString());
        }
    }

    public Integer getArg(String key, Integer defaultValue) {
        Object arg = this.attributesMap.get(key);
        if (arg == null) {
            return defaultValue;
        } else if (arg instanceof Number) {
            return ((Number) arg).intValue();
        } else {
            return Integer.valueOf(arg.toString());
        }
    }

    public Long getArg(String key, Long defaultValue) {
        Object arg = this.attributesMap.get(key);
        if (arg == null) {
            return defaultValue;
        } else if (arg instanceof Number) {
            return ((Number) arg).longValue();
        } else {
            return Long.valueOf(arg.toString());
        }
    }

    public String getArg(String key, String defaultValue) {
        Object arg = this.attributesMap.get(key);
        return arg == null ? defaultValue : arg.toString();
    }

    public Boolean getArg(String key, Boolean defaultValue) {
        Object arg = this.attributesMap.get(key);
        if (arg == null) {
            return defaultValue;
        } else if (arg instanceof Boolean) {
            return ((Boolean) arg);
        } else {
            return Boolean.valueOf(arg.toString());
        }
    }

    public MultipartFile getArg(final String key, final String filename, final byte content[]) {
        return getArg(key, filename, "application/octet-stream", content);
    }
    
    public MultipartFile getArg(final String key, final String filename, final String mimeType, final byte content[]) {
        Object arg = this.attributesMap.get(key);
        if (arg == null) {
            return new MultipartFile() {
                public String getName() {return key;}
                public String getOriginalFilename() {return filename;}
                public String getContentType() {return mimeType;}
                public boolean isEmpty() {return (content == null || content.length == 0);}
                public long getSize() {return (content != null ? content.length : 0);}
                public byte[] getBytes() {return content;}
                public InputStream getInputStream() {
                    return (content != null ? new ByteArrayInputStream(content) : null);
                }
                public void transferTo(File file) throws IOException, IllegalStateException {
                    if (content != null) {
                        FileUtils.writeArrayToFile(content, file);
                    }
                }
            };
        } else if (arg instanceof MultipartFile) {
            return ((MultipartFile) arg);
        } else {
            throw new IllegalArgumentException("Argument " + key + " is not a multipart file");
        }
    }

    public MultipartFile getArg(final String key, final String filename, final InputStream content, final int length) {
        return getArg(key, filename, "application/octet-stream", content, length);
    }
    
    public MultipartFile getArg(final String key, final String filename, final String mimeType, final InputStream content, final int length) {
        Object arg = this.attributesMap.get(key);
        if (arg == null) {
            return new MultipartFile() {
                public String getName() {return key;}
                public String getOriginalFilename() {return filename;}
                public String getContentType() {return mimeType;}
                public boolean isEmpty() {return (length == 0);}
                public long getSize() {return length;}
                public InputStream getInputStream() {return content;}
                public byte[] getBytes() throws IOException {
                    return FileUtils.convertStreamToByteArray(content, length);
                }
                public void transferTo(File file) throws IOException, IllegalStateException {
                    if (content != null) {
                        FileUtils.writeArrayToFile(getBytes(), file);
                    }
                }
            };
        } else if (arg instanceof MultipartFile) {
            return ((MultipartFile) arg);
        } else {
            throw new IllegalArgumentException("Argument " + key + " is not a multipart file");
        }
    }
    
    public MultipartFile getArg(final String key, final File file) {
        Object arg = this.attributesMap.get(key);
        if (arg == null) {
            return new MultipartFile() {
                public String getName() {return key;}
                public String getOriginalFilename() {return file.getPath();}
                public String getContentType() {
                    return servletContext.getMimeType(FileUtils.extractFileExtension(getOriginalFilename()));
                }
                public boolean isEmpty() {return !file.isFile() || (file.length() == 0);}
                public long getSize() {return file.length();}
                public InputStream getInputStream() throws IOException {return new FileInputStream(file);}
                public byte[] getBytes() throws IOException {
                    return FileUtils.convertStreamToByteArray(getInputStream(), -1);
                }
                public void transferTo(File outFile) throws IOException, IllegalStateException {
                    FileUtils.copyFile(file, outFile, false);
                }
            };
        } else if (arg instanceof MultipartFile) {
            return ((MultipartFile) arg);
        } else {
            throw new IllegalArgumentException("Argument " + key + " is not a multipart file");
        }
    }

    public Map<String, Object> getArgsAsMap() {
        return this.attributesMap;
    }
    
    public RequestState getWrappedRequestState() {
        return null;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
    
    public String replaceAttributesInString(String input) {
    	return ServletUtils.replaceWildcards(input, true, null, getRequest());
    }
////    
////    public static ServletRequestState unwrapToCore(RequestState requestState) {
////        while ((requestState != null) && !(requestState instanceof ServletRequestState)) {
////            requestState = requestState.getWrappedRequestState();
////        }
////        return (ServletRequestState) requestState;
////    }
}
