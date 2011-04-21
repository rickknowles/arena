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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Valueobject for cron jobs
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: CronJob.java,v 1.6 2006/09/28 15:57:12 rickknowles Exp $
 */
public class CronJob {
    
    private String url;
    private boolean post;
    private Map<String,?> parameters;
    private boolean relativeToWebroot = true;
    private boolean enabled = true;
    private boolean runAtStartup = false;
    
    private Date lastExecutedDate;
    private long periodMilliseconds;
    private String patternText;
    
    private Pattern matchingRegex; // compiled from pattern text
    
    public CronJob() {}
    public CronJob(String url, Date lastExecutedDate, long periodMilliseconds, String patternText) {
        this();
        setUrl(url);
        setLastExecutedDate(lastExecutedDate);
        setPeriodMilliseconds(periodMilliseconds);
        setPatternText(patternText);
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setPatternText(String patternText) {
        if ((patternText != null) && !patternText.equals("")) {
            this.patternText = patternText;
            this.matchingRegex = Pattern.compile(CronUtils.convertCronPatternToRegex(patternText));
        } else {
            this.patternText = null;
            this.matchingRegex = null;
        }
    }
    
    public void setLastExecutedDate(Date lastExecutedDate) {
        if (lastExecutedDate == null) {
            throw new IllegalArgumentException("Can't set lastExecutedDate to null");
        } else {
            this.lastExecutedDate = lastExecutedDate;
        }
    }
    
    public long getPeriodMilliseconds() {
        return periodMilliseconds;
    }
    public void setPeriodMilliseconds(long periodMilliseconds) {
        this.periodMilliseconds = periodMilliseconds;
    }
    public long getPeriodSeconds() {
        return getPeriodMilliseconds() / 1000L;
    }
    public void setPeriodSeconds(long periodSeconds) {
        setPeriodMilliseconds(1000L * periodSeconds);
    }
    public long getPeriodMinutes() {
        return getPeriodSeconds() / 60L;
    }
    public void setPeriodMinutes(long periodMinutes) {
        setPeriodSeconds(60L * periodMinutes);
    }
    public long getPeriodHours() {
        return getPeriodMinutes() / 60L;
    }
    public void setPeriodHours(long periodHours) {
        setPeriodMinutes(60L * periodHours);
    }
    public long getPeriodDays() {
        return getPeriodHours() / 24L;
    }
    public void setPeriodDays(long periodDays) {
        setPeriodHours(24L * periodDays);
    }
    
    public Date getLastExecutedDate() {
        return lastExecutedDate;
    }
    public String getPatternText() {
        return patternText;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    
    public boolean isPost() {
        return post;
    }
    public void setPost(boolean post) {
        this.post = post;
    }
    public Map<String, ?> getParameters() {
        return parameters;
    }
    public void setParameters(Map<String, ?> parameters) {
        this.parameters = parameters;
    }    
    
    public boolean isRelativeToWebroot() {
        return relativeToWebroot;
    }
    public void setRelativeToWebroot(boolean relativeToWebroot) {
        this.relativeToWebroot = relativeToWebroot;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }    
    public boolean isRunAtStartup() {
        return runAtStartup;
    }
    public void setRunAtStartup(boolean runAtStartup) {
        this.runAtStartup = runAtStartup;
    }
    
    public String buildUrl(String baseUrl) {
        //maybe improve the logic here to auto-detect a protocol prefix
        return (this.relativeToWebroot ? baseUrl : "") + this.url; 
    }
    
    public boolean isReadyForExecution(long currentTime) {
        return isReadyForExecution(currentTime, null);
    }
    
    /**
     * Returns true if this job is due to be executed (ie it has waited it's turn)
     */
    public boolean isReadyForExecution(long currentTime, String formattedDate) {
        if (this.matchingRegex != null) {
            if (formattedDate == null) {
                formattedDate = CronUtils.formatDateForPatternCheck(new Date(currentTime));
            }
            return ((this.lastExecutedDate.getTime() + this.periodMilliseconds) <= currentTime) &&
                    this.matchingRegex.matcher(formattedDate).find();
        } else {
            return ((this.lastExecutedDate.getTime() + this.periodMilliseconds) <= currentTime);
        }
    }
    
    public String toString() {
        return "[CronJob: url=" + url + " (call every " + 
            (this.periodMilliseconds / 60000L) + " minute(s)" +
            (this.patternText != null ? ", pattern=" + this.patternText : "") +
            ")]";
    }
    
    public void resolvePatterns(long zerothSecondTimestamp, Collection<CronJob> resolvedJobOutput) {
        if (this.patternText == null) {
            this.setLastExecutedDate(new Date(zerothSecondTimestamp));
            resolvedJobOutput.add(this);
        } else {
            String validatedSubPatterns[] = CronUtils.parseCronPattern(this.patternText);
            long patternPeriod = CronUtils.getPatternPeriod(validatedSubPatterns[0]); // all same period
            Date lastExecutedDate = new Date(zerothSecondTimestamp - patternPeriod);
            
            for (int n = 0; n < validatedSubPatterns.length; n++) {
                CronJob resolvedTo = new CronJob();
                resolvedTo.setUrl(url);
                resolvedTo.setPost(post);
                resolvedTo.setParameters(parameters);
                resolvedTo.setRelativeToWebroot(relativeToWebroot);
                resolvedTo.setEnabled(enabled);
                resolvedTo.setRunAtStartup(runAtStartup);
                resolvedTo.setLastExecutedDate(lastExecutedDate);
                resolvedTo.setPeriodMilliseconds(patternPeriod);
                resolvedTo.setPatternText(validatedSubPatterns[n]);
                
                resolvedJobOutput.add(resolvedTo);
            }        
        }
    }
}