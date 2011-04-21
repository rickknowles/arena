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
package arena.httpclient.redundant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This maps to the HostConfiguration element of the commons http client. It's
 * intended to hold the configuration details of a single host, so that
 * host pools can be formed. Currently it makes no attempt to actually pool
 * http connections, etc, so this is purely for failover rather than performance.
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: HostConfig.java,v 1.7 2007/01/19 07:28:38 rickknowles Exp $
 */
public class HostConfig {
    private final Log log = LogFactory.getLog(HostConfig.class);

    private HostPool pool;
    private String hostname;
    private Integer orderIndex;
    private boolean isHttps = false;
    private Integer portNumber;
    private Integer connectTimeout;
    private Integer readTimeout;
    
    public String getHostname() {
        return this.hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public Integer getOrderIndex() {
        return this.orderIndex;
    }
    public void setOrderIndex(int orderIndex) {
        this.orderIndex = new Integer(orderIndex);
    }
    public void setHttps(boolean isHttps) {
        this.isHttps = isHttps;
    }
    public void setPortNumber(int portNumber) {
        this.portNumber = new Integer(portNumber);
    }
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }
    public Integer getConnectTimeout() {
        return connectTimeout;
    }
    public Integer getReadTimeout() {
        return readTimeout;
    }
    public HostPool getPool() {
        return pool;
    }
    public void setPool(HostPool pool) {
        this.pool = pool;
    }

    /**
     * Gets an instantiated http client for this url
     */
    public String buildFullURL(String partialURL) {
        String prefix = (this.isHttps ? "https://" : "http://") +
                this.hostname;
        int port = this.portNumber != null ? 
                this.portNumber : 
                (this.isHttps ? 443 : 80);
        if ((this.isHttps && (port != 443)) ||
                (!this.isHttps && (port != 80))) {
            prefix = prefix + ":" + port;
        }
        String out = prefix + partialURL;
        log.debug("Rewrote host pooled url: " + out);
        return out;
    }
}
