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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;


/**
 * This object performs text transforms using the Velocity engine
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: VelocityProcessor.java,v 1.9 2007/07/25 11:33:05 rickknowles Exp $
 */
public class VelocityProcessor implements InitializingBean, ServletContextAware {
    private final Log log = LogFactory.getLog(VelocityProcessor.class);
    
    private VelocityEngineBean velocityEngineBean;
    private Map<String,Object> threadSafeTools;
    private Map<String,String> nonThreadSafeTools;
    private boolean logContextVars = false;
    private ServletContext servletContext;
    private String encoding;
    
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setVelocityEngineBean(VelocityEngineBean velocityEngineBean) {
        this.velocityEngineBean = velocityEngineBean;
    }

    public void setThreadSafeTools(Map<String, Object> threadSafeTools) {
        this.threadSafeTools = threadSafeTools;
    }

    public void setNonThreadSafeTools(Map<String, String> nonThreadSafeTools) {
        this.nonThreadSafeTools = nonThreadSafeTools;
    }

    public void setLogContextVars(boolean logContextVars) {
        this.logContextVars = logContextVars;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.velocityEngineBean == null) {
            VelocityEngineBean engineBean = new VelocityEngineBean();
            engineBean.setServletContext(this.servletContext);
            engineBean.setBasePath("");
            engineBean.afterPropertiesSet();
            setVelocityEngineBean(engineBean);
            log.info("Created implicit velocity engine for VelocityProcessor bean");
        }
    }

    /**
     * Actually performs the transform
     * @param stylesheetName The name of the stylesheet we need
     */
    protected void processTemplate(Map<String,?> attributes, Writer out, Template template, String encoding) throws IOException {
        long startTime = System.currentTimeMillis();

        Context context = buildContext(attributes);
        template.setEncoding(encoding);

        try {
            template.merge(context, out);
            log.debug("Velocity template transform processed in " + 
                       (System.currentTimeMillis() - startTime) + " ms");
        } catch (ResourceNotFoundException  errAny) {
            throw new RuntimeException("Error merging the velocity template", errAny);
        } catch (ParseErrorException errAny) {
            throw new RuntimeException("Error merging the velocity template", errAny);
        } catch (Exception errAny) {
            throw new RuntimeException("Error merging the velocity template", errAny);
        }
    }
    
    protected Context buildContext(Map<String,?> attributes) {
        if (this.logContextVars) {
            log.debug("Velocity context variables: " + attributes);
        }
        Context context = new VelocityContext(attributes);
        context.put("Math", Math.class);
        if (this.threadSafeTools != null) {
            for (String toolName : this.threadSafeTools.keySet()) {
                context.put(toolName, this.threadSafeTools.get(toolName));
            }
        }
        if (this.nonThreadSafeTools != null) {
            for (String toolName : this.nonThreadSafeTools.keySet()) {
                String toolClass = this.nonThreadSafeTools.get(toolName);
                try {
                    Class<?> c = Class.forName(toolClass);
                    context.put(toolName, c.newInstance());
                } catch (ClassNotFoundException err) {
                    throw new RuntimeException("Velocity tool class not found: " + toolClass, err);
                } catch (Exception err) {
                    throw new RuntimeException("Error initializing velocity tool: " + toolName, err);
                }
            }
        }
        return context;
    }
    
    /**
     * Gets a stylesheet from the collection, caching it after loading if not in
     * the cache
     * @param stylesheetName The name of the stylesheet we need
     */
    public Template getTemplate(String sheetName) {
        try {
            if (sheetName == null) {
                throw new RuntimeException("urlSheet was null");
            }

            log.debug("Parsing velocity template - " + sheetName);
            Template template = this.velocityEngineBean.getEngine().getTemplate(sheetName);

            return template;
        } catch (ResourceNotFoundException  errAny) {
            throw new RuntimeException("Error loading the velocity template", errAny);
        } catch (ParseErrorException errAny) {
            throw new RuntimeException("Error loading the velocity template", errAny);
        } catch (Exception errAny) {
            throw new RuntimeException("Error loading the velocity template", errAny);
        }
    }

    public Template makeTemplate(Reader templateContent, String sheetName) {
        try {
            RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
            SimpleNode node = runtimeServices.parse(templateContent, sheetName);
            Template template = new Template();
            template.setRuntimeServices(runtimeServices);
            template.setData(node);
            template.initDocument();
            return template;
        } catch (ResourceNotFoundException errAny) {
            throw new RuntimeException("Error parsing the velocity template", errAny);
        } catch (ParseErrorException errAny) {
            throw new RuntimeException("Error parsing the velocity template", errAny);
        } catch (Exception errAny) {
            throw new RuntimeException("Error loading the velocity template", errAny);
        }
    }

    // map attribute forms
    
    /**
     * Renders with velocity template using the default encoding for the output
     * 
     * @param attributes Map of context vars to replace
     * @param out Writer to send the output to
     * @param stylesheetName A stylesheet to render through
     * @return The result of the transform as a string
     */
    public void transform(Map<String,?> attributes, Writer out, String stylesheetName)
            throws IOException {
        transform(attributes, out, stylesheetName, this.encoding);
    }
    
    /**
     * Renders with velocity template using an explicit encoding for the output
     * 
     * @param attributes Map of context vars to replace
     * @param out Writer to send the output to
     * @param stylesheetName A stylesheet to render through
     * @param encoding override the encoding used
     * @return The result of the transform as a string
     */
    public void transform(Map<String,?> attributes, Writer out, String stylesheetName, String encoding) throws IOException {
        processTemplate(attributes, out, getTemplate(stylesheetName), encoding);
    }

    public void transform(Map<String,?> attributes, Writer out, Reader template, String stylesheetName) throws IOException {
        transform(attributes, out, template, stylesheetName, this.encoding);
    }
    
    public void transform(Map<String,?> attributes, Writer out, Reader template, String stylesheetName, String encoding) throws IOException {
        processTemplate(attributes, out, makeTemplate(template, stylesheetName), encoding);
    }
}
