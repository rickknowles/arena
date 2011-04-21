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
package arena.velocity;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

/**
 * Acts as a wrapper for a velocity engine, and provides config methods for the engine when it 
 * initializes 
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class VelocityEngineBean implements LogChute, InitializingBean, ServletContextAware {
    private final Log log = LogFactory.getLog(VelocityEngineBean.class);
    
    private VelocityEngine engine;
    private String basePath = ".";
    private Properties properties;
    private String defaultEncoding = "UTF-8";
    private ServletContext servletContext;
    
    public VelocityEngine getEngine() {
        return this.engine;
    }
    
    public void afterPropertiesSet() throws Exception {
        if (this.engine == null) {
            log.info("Initializing velocity engine");
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, this);
            Properties newProps = initialProperties();
            if (this.properties != null) {
                newProps.putAll(this.properties);
            }
            try {
                log.info("Initializing velocity with properties: " + newProps);
                engine.init(newProps);
                this.engine = engine;
            } catch (Exception err) {
                log.error("Error initializing velocity engine", err);
                throw new RuntimeException("Error initializing velocity engine", err);
            }
        }
    }
    
    protected Properties initialProperties() {
        Properties props = new Properties();
        if (this.defaultEncoding != null) {
            props.put(VelocityEngine.INPUT_ENCODING, this.defaultEncoding);
        }
        if (this.servletContext != null) {
            props.put("file.resource.loader.path", this.servletContext.getRealPath(this.basePath));
        } else {
            props.put("file.resource.loader.path", this.basePath);
        }
        return props;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setEngine(VelocityEngine engine) {
        this.engine = engine;
    }

    public void setDefaultEncoding(String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void init(RuntimeServices runtimeServices) throws Exception {}

    public boolean isLevelEnabled(int level) {
        switch (level) {
        case LogChute.TRACE_ID: return log.isTraceEnabled();
        case LogChute.DEBUG_ID: return log.isDebugEnabled();
        case LogChute.INFO_ID: return log.isInfoEnabled();
        case LogChute.WARN_ID: return log.isWarnEnabled();
        case LogChute.ERROR_ID: return log.isErrorEnabled();
        }
        return true;
    }

    public void log(int level, String msg, Throwable err) {
        switch (level) {
        case LogChute.TRACE_ID: log.trace(msg, err); break;
        case LogChute.DEBUG_ID: log.debug(msg, err); break;
        case LogChute.INFO_ID: log.info(msg, err); break;
        case LogChute.WARN_ID: log.warn(msg, err); break;
        case LogChute.ERROR_ID: log.error(msg, err); break;
        }
    }

    public void log(int level, String msg) {
        switch (level) {
        case LogChute.TRACE_ID: log.trace(msg); break;
        case LogChute.DEBUG_ID: log.debug(msg); break;
        case LogChute.INFO_ID: log.info(msg); break;
        case LogChute.WARN_ID: log.warn(msg); break;
        case LogChute.ERROR_ID: log.error(msg); break;
        }
    }
}
