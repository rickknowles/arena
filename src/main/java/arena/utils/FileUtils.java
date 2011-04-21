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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Useful functions for dealing with files and filenames
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class FileUtils {    
    public static String extractFileWithoutPath(String input) {
        if (input == null) {
            return null;
        } else {
            int lastSlashPos = Math.max(input.lastIndexOf('/'),
                    input.lastIndexOf('\\'));
            return (lastSlashPos == -1 ? input : 
                    input.substring(lastSlashPos + 1));
        }
    }
    
    public static String extractFileExtension(String input) {
        if (input == null) {
            return null;
        } else {
            int lastDotPos = input.lastIndexOf('.');
            return (lastDotPos == -1 ? ".txt" : 
                    input.substring(lastDotPos));
        }
    }
    
    public static String protectExtensions(String extension) {
        if (extension == null) {
            return null;
        } else if (extension.equalsIgnoreCase(".exe") || 
                extension.equalsIgnoreCase(".com") ||
                extension.equalsIgnoreCase(".bat") ||
                extension.equalsIgnoreCase(".msi") ||
                extension.equalsIgnoreCase(".pif") ||
                extension.equalsIgnoreCase(".scr")) {
            return ".txt";
        } else {
            return extension;
        }            
    }
    
    public static File writeArrayToTempFile(byte data[], File parentDir, 
            String prefix, String suffix) throws IOException {
        parentDir.mkdirs();
        File outFile = File.createTempFile(prefix, 
                FileUtils.protectExtensions(suffix), parentDir);
        writeArrayToFile(data, outFile);
        return outFile;
    }

    public static void writeArrayToFile(byte data[], File outFile) throws IOException {
        OutputStream out = null;
        try {
            outFile.getParentFile().mkdirs();
            out = new FileOutputStream(outFile);
            out.write(data);
            out.close();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    public static byte[] convertStreamToByteArray(InputStream in, int maxLength) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = 0;
        int read = 0;
        byte buffer[] = new byte[4096];
        while (((maxLength < 0) || (count < maxLength)) && ((read = in.read(buffer)) != -1)) {
            out.write(buffer, 0, maxLength < 0 ? read : Math.min(maxLength - out.size(), read));
            count += read;
        }
        return out.toByteArray();
    }
    
    /**
     * Used for checking path inheritances. This is useful for canonicalizing URLs that 
     * maybe don't exist, but we want to treat them as paths anyway.
     */
    public static boolean isDescendant(File parent, File child, File commonBase) {
        if (child.equals(parent)) {
            return true;
        } else {
            // Start by checking canonicals if possible
            try {
                String canonicalParent = parent.getAbsoluteFile().getCanonicalPath();
                String canonicalChild = child.getAbsoluteFile().getCanonicalPath();
                if (canonicalChild.startsWith(canonicalParent)) {
                    return true;
                }
            } catch (IOException err) {
                // assume the files are imaginary ... try with the manual construction below
            }
            
            // If canonicals don't match, we're dealing with symlinked files, so if we can
            // build a path from the parent to the child, 
            String childOCValue = constructOurCanonicalVersion(child, commonBase);
            String parentOCValue = constructOurCanonicalVersion(parent, commonBase);
            return childOCValue.startsWith(parentOCValue);
        }
    }
    
    public static String constructOurCanonicalVersion(File current, File stopPoint) {
        int backOnes = 0;
        StringBuffer ourCanonicalVersion = new StringBuffer();
        while ((current != null) && !current.equals(stopPoint)) {
            if (current.getName().equals("..")) {
                backOnes++;
            } else if (current.getName().equals(".")) {
                // skip - do nothing
            } else if (backOnes > 0) {
                backOnes--;
            } else {
                ourCanonicalVersion.insert(0, "/" + current.getName());
            }
            current = current.getParentFile();
        }
        return ourCanonicalVersion.toString();
    }
    
    public static void copyFile(File from, File to, boolean appendNotReplace) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            if (!appendNotReplace && to.exists() && to.length() > 0) {
                to.delete();
            }
            to.getParentFile().mkdirs();
            
            in = new FileInputStream(from);
            out = new FileOutputStream(to, true);
            byte buffer[] = new byte[4096];
            int read = 0;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            if (in != null) {
                try {in.close();} catch (IOException err) {}
            }
            if (out != null) {
                try {out.close();} catch (IOException err) {}
            }
        }
    }
    
    public static void gzip(File input) {
        gzip(input, null);
    }
    
    public static void gzip(File input, File outFile) {
        Log log = LogFactory.getLog(FileUtils.class);
        InputStream inStream = null;
        OutputStream outStream = null;
        GZIPOutputStream gzip = null;
        try {
            long inFileLengthKB = input.length() / 1024L;
            
            // Open the out file
            if (outFile == null) {
                outFile = new File(input.getParentFile(), input.getName() + ".gz");
            }
            inStream = new FileInputStream(input);
            outStream = new FileOutputStream(outFile, true);
            gzip = new GZIPOutputStream(outStream);
            
            // Iterate through in buffers and write out to the gzipped output stream
            byte buffer[] = new byte[102400]; // 100k buffer
            int read = 0;
            long readSoFar = 0;
            while ((read = inStream.read(buffer)) != -1) {
                readSoFar += read;
                gzip.write(buffer, 0, read);
                log.debug("Gzipped " + (readSoFar / 1024L) + 
                        "KB / " + inFileLengthKB + "KB of logfile " + input.getName());
            }
            
            // Close the streams
            inStream.close();
            inStream = null;
            gzip.close();
            gzip = null;
            outStream.close();
            outStream = null;
            
            // Delete the old file
            input.delete();
            log.debug("Gzip of logfile " + input.getName() + " complete");
        } catch (IOException err) {
            // Delete the gzip file
            log.error("Error during gzip of logfile " + input, err);
        } finally {
            if (inStream != null) {
                try {inStream.close();} catch (IOException err2) {}
            }
            if (gzip != null) {
                try {gzip.close();} catch (IOException err2) {}
            }
            if (outStream != null) {
                try {outStream.close();} catch (IOException err2) {}
            }
        }
    }

    /**
     * Deletes this folder and all the folders and files below it (recursive)
     */
    public static void deleteThisAndAllDescendants(File parentDir) {
        if (parentDir == null) {
            return;
        }
        if (parentDir.isDirectory()) {
            File children[] = parentDir.listFiles();
            for (int n = 0; (children != null) && n < children.length; n++) {
                deleteThisAndAllDescendants(children[n]);
            }
        }
        parentDir.delete();
    }
    
    public static int readInt(InputStream in) throws IOException {
        byte buf[] = new byte[4];
        int readCount = in.read(buf);
        if (readCount == 4) {
            return ((buf[0] & 0xFF) << 0) + ((buf[1] & 0xFF) << 8) + ((buf[2] & 0xFF) << 16) + ((buf[3]) << 24);
        } else {
            if (readCount >= 0) {
                LogFactory.getLog(FileUtils.class).warn("Error reading int from stream: read = " + 
                        Arrays.asList(buf).subList(0, readCount));
            }
            return -1;
        }
    }

    public static void writeInt(OutputStream out, int value) throws IOException {
        byte[] buf = new byte[4];
        writeIntToBuffer(value, buf, 0);
        out.write(buf);
    }
    
    public static void writeIntToBuffer(int value, byte[] buffer, int offset) {
        buffer[offset] = (byte) (value >>> 0);
        buffer[offset + 1] = (byte) (value >>> 8);
        buffer[offset + 2] = (byte) (value >>> 16);
        buffer[offset + 3] = (byte) (value >>> 24);
    }
}
