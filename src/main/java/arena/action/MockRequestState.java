package arena.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.Cookie;


import org.springframework.web.multipart.MultipartFile;

import arena.utils.FileUtils;
import arena.utils.ServletUtils;


public class MockRequestState implements RequestState {

    private Map<String,Object> attributesMap;
    private ServerSideUserState serverSideUserState;
    private ServerSideApplicationState serverSideApplicationState;
    private ClientSideUserState clientSideUserState;
    
    public MockRequestState() {
        this.attributesMap = new HashMap<String,Object>();
        this.serverSideUserState = new MapWrapper(new HashMap<String,Object>());
        this.serverSideApplicationState = new MapWrapper(new HashMap<String,Object>());
        this.clientSideUserState = new MapClientSideUserState(new HashMap<String,Cookie>());
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
        if (arg != null) {
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
        if (arg != null) {
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
        if (arg != null) {
            return new MultipartFile() {
                public String getName() {return key;}
                public String getOriginalFilename() {return file.getPath();}
                public String getContentType() {
                    throw new IllegalStateException("Not implemented");
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
    
    public String replaceAttributesInString(String input) {
        return ServletUtils.replaceWildcards(input, false, null, null);
    }

}
