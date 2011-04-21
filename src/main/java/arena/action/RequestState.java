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
package arena.action;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;


import org.springframework.web.multipart.MultipartFile;

import arena.utils.Setter;



/**
 * Wraps request/response pairs for 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public interface RequestState extends Setter<String,Object> {
    
    public Object getArg(String key);
    public String getArg(String key, String defaultValue);
    public Integer getArg(String key, Integer defaultValue);
    public Long getArg(String key, Long defaultValue);
    public Float getArg(String key, Float defaultValue);
    public Boolean getArg(String key, Boolean defaultValue);
    
    public MultipartFile getArg(String key, String defaultFilename, byte defaultContent[]);
    public MultipartFile getArg(String key, String defaultFilename, String defaultMimeType, byte defaultContent[]);
    public MultipartFile getArg(String key, String defaultFilename, InputStream defaultContent, int defaultLength);
    public MultipartFile getArg(String key, String defaultFilename, String defaultMimeType, InputStream defaultContent, int defaultLength);
    public MultipartFile getArg(String key, File defaultContent);
    
    public void removeArg(String key);
    public void setArg(String key, Object value);
    public Iterator<String> getArgNames();
    public Map<String,Object> getArgsAsMap();
    
    public ServerSideUserState getServerSideUserState();
    public ServerSideApplicationState getServerSideApplicationState();
    public ClientSideUserState getClientSideUserState();
    
    public String replaceAttributesInString(String input);
}
