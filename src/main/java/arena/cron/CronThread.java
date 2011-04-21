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
package arena.cron;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.httpclient.HttpClient;
import arena.httpclient.HttpClientSource;
import arena.httpclient.commons.JakartaCommonsHttpClientSource;

/**
 * Implements the cron thread. This is a Runnable implementation that triggers a url get on
 * urls in the "jobs" array.
 * 
 * In a spring container, need to add 'init-method="init" destroy-method="close"' to bean 
 * definition to start and close the thread automatically
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: CronThread.java,v 1.21 2008/10/21 10:52:48 rickknowles Exp $
 */
public class CronThread implements Runnable {
    private final Log log = LogFactory.getLog(CronThread.class);

    private String requestEncoding;
    private HttpClientSource httpClientSource;
    private CronJob jobs[];
    private String baseUrl;
    
    private Thread thread;
    
    public String getRequestEncoding() {
        return requestEncoding;
    }

    public void setRequestEncoding(String requestEncoding) {
        this.requestEncoding = requestEncoding;
    }

    public HttpClientSource getHttpClientSource() {
        return httpClientSource;
    }

    public void setHttpClientSource(HttpClientSource httpClientSource) {
        this.httpClientSource = httpClientSource;
    }

    public CronJob[] getJobs() {
        return jobs;
    }

    public void setJobs(CronJob[] jobs) {
        long minuteBefore = CronUtils.getZerothSecondMinuteTimestamp(System.currentTimeMillis());
        List<CronJob> resolvedJobs = new ArrayList<CronJob>();
        for (CronJob job : jobs) {
            job.resolvePatterns(minuteBefore, resolvedJobs);
        }
        this.jobs = resolvedJobs.toArray(new CronJob[resolvedJobs.size()]);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void init() {
        synchronized (this) {
            close();        
            this.thread = new Thread(this, "Keystone Cron Thread");
            this.thread.setDaemon(true);
            this.thread.start();
        }
        if (this.jobs != null) {
            for (CronJob job : jobs) {
                if (job.isRunAtStartup()) {
                    executeJob(job);
                }
            }
        }
    }

    public void close() {
        synchronized (this) {
            if (this.thread != null) {
                this.thread.interrupt();
                this.thread = null;
            }
        }
    }
    
    public void run() {
        try {Thread.sleep(2000);} catch (InterruptedException err) {return;}
//        LogContext.push("cronThread");
        try {
            log.debug("Beginning cron thread");
            if (this.httpClientSource == null) {
                this.httpClientSource = new JakartaCommonsHttpClientSource();
            }
            
            boolean interruptedFlag = false;
            try {
                CronUtils.sleepUntilNextMinute(System.currentTimeMillis());
            } catch (InterruptedException err) {
                interruptedFlag = true;
            }
            
            while (!interruptedFlag) {
                long now = CronUtils.getZerothSecondMinuteTimestamp(System.currentTimeMillis());
                try {
                    // Perform the url gets
                    String formattedDate = CronUtils.formatDateForPatternCheck(new Date(now));
                    for (int n = 0; (n < this.jobs.length) && !interruptedFlag; n++) {
                        
                        // Check last exec date is in the past
                        if (this.jobs[n].isReadyForExecution(now, formattedDate)) {
                            this.jobs[n].setLastExecutedDate(new Date(now));
                            executeJob(this.jobs[n]);
                        }
                        
                        if (Thread.interrupted()) {
                            interruptedFlag = true;
                        }
                    }

                    CronUtils.sleepUntilNextMinute(now);
                    if (Thread.interrupted()) {
                        interruptedFlag = true;
                    }
                } catch (InterruptedException err) {
                    interruptedFlag = true;
                    continue;
                }
            }
            log.debug("Terminating cron thread");
        } finally {
//            LogContext.pop();
        }
    }

    protected void executeJob(CronJob job) {
        log.debug("Executing: " + job);
        HttpClient getter = this.httpClientSource.getClient();
        getter.setUrl(job.buildUrl(this.baseUrl));
        getter.setPost(job.isPost());
        if (job.getParameters() != null) {
            for (String key : job.getParameters().keySet()) {
                Object val = job.getParameters().get(key);
                if (val != null) {
                    getter.addRequestParameter(key, job.toString());
                }
            }
        }
        getter.setRequestEncoding(requestEncoding);
        
        getter.getThreaded();
    }
}
