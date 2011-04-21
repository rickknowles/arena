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
package arena.lucene;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

public class LuceneDirectoryBean implements InitializingBean, ServletContextAware {
    private final Log log = LogFactory.getLog(LuceneDirectoryBean.class);
    
    private ServletContext servletContext;
    private Directory directory;
    private String location;
    private boolean initializeIndexIfNew = true;
    private boolean disableLocks = false;
    
    public Directory getDirectory() {
        return this.directory;
    }
    
    public void afterPropertiesSet() throws Exception {
        boolean initialize = false;
        if (this.location == null) {
            this.directory = new RAMDirectory();
            initialize = true;
        } else {
            File dir = new File(this.servletContext != null ? 
                    this.servletContext.getRealPath(location) : location);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            LockFactory lf = (this.disableLocks ? NoLockFactory.getNoLockFactory() : null);
            this.directory = FSDirectory.open(dir, lf);
            log.info("Using lucene directory: file=" + dir + " instance=" + this.directory.toString());
            initialize = (this.initializeIndexIfNew && dir.listFiles().length == 0);
        }
        
        if (initialize) {
            // create a dummy writer to initialize the index
            IndexWriter writer = null;
            try {
                writer = new IndexWriter(this.directory, new StandardAnalyzer(Version.LUCENE_30), true, MaxFieldLength.LIMITED);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public void close() throws IOException {
        if (this.directory != null) {
            this.directory.close();
            this.directory = null;
        }
    }

    public String getLocation() {
        return location;
    }

    public boolean isInitializeIndexIfNew() {
        return initializeIndexIfNew;
    }

    public void setInitializeIndexIfNew(boolean initializeIndexIfNew) {
        this.initializeIndexIfNew = initializeIndexIfNew;
    }
    
    public void setUseNIO(boolean useNIO) {
        // ugly hack to be replaced in lucene 2.9
        System.setProperty("org.apache.lucene.FSDirectory.class", 
                useNIO ? NIOFSDirectory.class.getName() : FSDirectory.class.getName());
    }

    public void setDisableLocks(boolean disableLocks) {
        this.disableLocks = disableLocks;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
