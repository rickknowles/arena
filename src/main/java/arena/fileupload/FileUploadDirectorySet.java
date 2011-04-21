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
package arena.fileupload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;

import arena.utils.FileUtils;

/**
 * Manages a set of directories of uploaded files, one for an original and the others for 
 * various transforms of the originals. When save() is called, the original is added 
 * and the various transformers are applied to create transforms in the other directories.
 * 
 * This is useful for resizing uploaded images, or even re-encoding of audio files or text
 * documents etc. Essentially anything that at upload time requires rewriting at the binary
 * level.
 */
public class FileUploadDirectorySet implements ServletContextAware {
    private final Log log = LogFactory.getLog(FileUploadDirectorySet.class);

    private ServletContext servletContext;
    private String originalDirectory = "/uploads";
    private FileTransform originalTransform; // likely null, but if set modifies original
    
    // store name/dir/transform tuples as arrays, since maps are inefficient for such small sets
    private FileTransformDirectory transforms[];
    
    private String prefix = "file";    

    public File save(InputStream in, String uploadedFilename) throws IOException {
        String suffix = FileUtils.protectExtensions(
                FileUtils.extractFileExtension(uploadedFilename));
        suffix = (suffix == null ? ".txt" : suffix.toLowerCase());
        File originalDir = buildDirectory(this.originalDirectory);
        originalDir.mkdirs();
        File outFile = File.createTempFile(prefix, suffix, originalDir);
        byte buffer[] = new byte[4096];
        int read = 0;
        OutputStream out = null;
        try {
            log.debug("Writing content to original folder: " + outFile.getName());
            out = new FileOutputStream(outFile);
            while ((read = in.read(buffer)) >= 0) {
                out.write(buffer,0, read);
            }
            out.close();
            out = null;
            
            // Run transform on original if defined
            if (this.originalTransform != null) {
                log.debug("Executing transform on original");
                File transformed = File.createTempFile(prefix, suffix, originalDir);
                this.originalTransform.transform(outFile, transformed);
                outFile.delete();
                outFile = transformed;
            }   
            
            // Execute filters
            if (this.transforms != null) {
                for (int n = 0; n < this.transforms.length; n++) {
                    log.debug("Executing transform: " + transforms[n].getName());
                    File outDir = buildDirectory(transforms[n].getOutputDirectory());
                    outDir.mkdirs();
                    transforms[n].getTransform().transform(outFile, 
                            new File(outDir, outFile.getName()));
                }
            }
            return outFile;
        } finally {
            if (out != null) {
                try {out.close();} catch (IOException err) {}
            }
        }
    }
    
    public File getTransformedFile(String transformName, String filename) {
        if (transformName == null || transformName.equals("")) {
            File originalDir = buildDirectory(this.originalDirectory);
            return new File(originalDir, filename);
        } else {
            for (int n = 0; n < this.transforms.length; n++) {
                if (this.transforms[n].getName().equals(transformName)) {
                    return new File(buildDirectory(transforms[n].getOutputDirectory()), filename);
                }
            }
            return null;
        }
    }
    
    public File[] getUploadedFiles() {
        return buildDirectory(this.originalDirectory).listFiles();
    }
    
    protected File buildDirectory(String directory) {
        synchronized (this) {
            if (this.servletContext != null) {
                return new File(this.servletContext.getRealPath(directory));
            } else {
                return new File(directory);
            }
        }
    }

    public void setOriginalDirectory(String originalDirectory) {
        this.originalDirectory = originalDirectory;
    }

    public void setOriginalTransform(FileTransform originalTransform) {
        this.originalTransform = originalTransform;
    }

    public void setTransforms(FileTransformDirectory[] transforms) {
        this.transforms = transforms;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
