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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.utils.StringUtils;


/**
 * This maps to the HostConfiguration element of the commons http client. It's
 * intended to hold the configuration details of a single host, so that
 * host pools can be formed. Currently it makes no attempt to actually pool
 * http connections, etc, so this is purely for failover rather than performance.
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: HostPool.java,v 1.8 2006/11/03 16:25:34 rickknowles Exp $
 */
public class HostPool implements Comparator<HostConfig> {
    private final Log log = LogFactory.getLog(HostPool.class);
    
    private String poolName;
    private HostConfig[] hostConfigs;
    private Map<HostConfig,Date> availableDates;
    private int recoveryTimeout = 60000;
    private Integer connectTimeout;
    private Integer readTimeout;
    
    public HostPool() {
        this.availableDates = new HashMap<HostConfig,Date>();        
    }

    public String getPoolName() {
        return poolName;
    }
    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }
    public void setRecoveryTimeout(int recoveryTimeout) {
        this.recoveryTimeout = recoveryTimeout;
    }
    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }
    public void setHosts(Iterable<?> hosts) {
        int n = 0;
        synchronized (this) {
            List<HostConfig> configs = new ArrayList<HostConfig>();
            for (Object host : hosts) {
                if (host instanceof HostConfig) {
                    configs.add((HostConfig) host);
                } else {
                    String tokens[] = StringUtils.tokenizeToArray((String) host, ":");
                    HostConfig config = new HostConfig();
                    config.setHostname(tokens.length > 0 ? tokens[0] : null);
                    config.setPortNumber(tokens.length > 1 ? new Integer(tokens[1]) : null);
                    config.setHttps(tokens.length > 2 ? tokens[2].equalsIgnoreCase("https") : false);
                    config.setConnectTimeout(tokens.length > 3 ? new Integer(tokens[3]) : null);
                    config.setReadTimeout(tokens.length > 4 ? new Integer(tokens[4]) : null);
                    config.setOrderIndex(n);
                    config.setPool(this);
                    configs.add(config);
                }
                n++;
            }
            this.hostConfigs = configs.toArray(new HostConfig[configs.size()]);
            Arrays.sort(this.hostConfigs, this);
        }
    }
    
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }
    
    /**
     * Iterates the list of hosts, until it finds a valid one
     */
    public HostConfig getHost() {
        HostConfig found = null;
        synchronized (this) {
            for (int n = 0; (n < this.hostConfigs.length) && (found == null); n++) {
                HostConfig test = (HostConfig) this.hostConfigs[n];
                Date availableFrom = (Date) this.availableDates.get(test);
                if (availableFrom != null) {
                    // If the date has passed, mark it as valid and exit
                    if (availableFrom.before(new Date())) {
                        this.availableDates.remove(test);
                        found = test;
                        log.debug("Flagging host as recovered: " + test.getHostname());
                    }
                } else {
                    found = test;
                }
            }
        }
        return found;
    }
    
    /**
     * Flag a host as invalid for the period of time in recovery timeout
     */
    public void flagHostAsFailed(HostConfig config) {
        if (config != null) {
            log.debug("Flagging host as failed: " + config.getHostname());
            synchronized (this) {
                Date availableFrom = new Date(System.currentTimeMillis() +
                        this.recoveryTimeout);
                this.availableDates.put(config, availableFrom);
            }
        }
    }
    
    /**
     * Mark all hosts as recovered (basically a flush on invalid settings)
     */
    public void forceFullRecovery() {
        synchronized (this) {
            this.availableDates.clear();
        }
    }
    
    public int compare(HostConfig one, HostConfig two) {
        Integer order1 = one.getOrderIndex() != null ? one.getOrderIndex() : 100;
        Integer order2 = two.getOrderIndex() != null ? two.getOrderIndex() : 100;
        return order1.compareTo(order2);
    }
}
