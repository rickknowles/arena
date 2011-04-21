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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import arena.utils.StringUtils;

/**
 * Utility methods used by the CronJob class to build the regexes used in 
 * unix style "star pattern" matching. 
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: CronUtils.java,v 1.4 2008/10/21 10:52:48 rickknowles Exp $
 */
public class CronUtils {

    private static final String STAR = "*";
    private static final String DATE_MATCHING_MASK = "MMddHHmmF";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_MATCHING_MASK);

    private static final int MINUTE_IDX = 0;
    private static final int HOUR_IDX = 1;
    private static final int DAY_IDX = 2;
    private static final int MONTH_IDX = 3;
    private static final int DAY_OF_WEEK_IDX = 4;    
    
    public static String formatDateForPatternCheck(Date input) {
        synchronized (DATE_FORMATTER) {
            return DATE_FORMATTER.format(input);
        }
    }
    
    /**
     * Parses and validates the cron pattern into it's component tokens, then
     * re-assembles each of the variations (eg "* 2,4 * 1,2 *" gets translated 
     * into ["* 2 * 1 *", "* 2 * 2 *", "* 4 * 1 *", "* 4 * 2 *"]
     */
    public static String[] parseCronPattern(String pattern) {
        String tokens[] = splitOnSpaces(pattern);
        String validatedTokens[] = new String[5];
        System.arraycopy(tokens, 0, validatedTokens, 0, Math.min(tokens.length, 5));
        for (int n = tokens.length; n < 5; n++) {
            validatedTokens[n] = "*";
        }        
        validatedTokens[MINUTE_IDX] = formatCronPatternToken(validatedTokens[MINUTE_IDX], 0, 59);
        validatedTokens[HOUR_IDX] = formatCronPatternToken(validatedTokens[HOUR_IDX], 0, 23);
        validatedTokens[DAY_IDX] = formatCronPatternToken(validatedTokens[DAY_IDX], 1, 31);
        validatedTokens[MONTH_IDX] = formatCronPatternToken(validatedTokens[MONTH_IDX], 1, 12);
        validatedTokens[DAY_OF_WEEK_IDX] = formatCronPatternToken(validatedTokens[DAY_OF_WEEK_IDX], 1, 7);
        
        // Build an array of permutations once broken up
        List<String> patterns = new ArrayList<String>();
        buildSubPatterns(StringUtils.tokenizeToArray(validatedTokens[MONTH_IDX], ","),
                StringUtils.tokenizeToArray(validatedTokens[DAY_IDX], ","),
                StringUtils.tokenizeToArray(validatedTokens[HOUR_IDX], ","),
                StringUtils.tokenizeToArray(validatedTokens[MINUTE_IDX], ","),
                StringUtils.tokenizeToArray(validatedTokens[DAY_OF_WEEK_IDX], ","), patterns);
        return patterns.toArray(new String[patterns.size()]);
    }
    
    private static void buildSubPatterns(String month[], String day[], String hour[], 
            String minute[], String dayOfWeek[], List<String> results) {
        if (month.length > 1) {
            for (int n = 0; n < month.length; n++) {
                buildSubPatterns(new String[] {month[n]}, day, hour, minute, dayOfWeek, results);
            }
        } else if (day.length > 1) {
            for (int n = 0; n < day.length; n++) {
                buildSubPatterns(month, new String[] {day[n]}, hour, minute, dayOfWeek, results);
            }
        } else if (hour.length > 1) {
            for (int n = 0; n < hour.length; n++) {
                buildSubPatterns(month, day, new String[] {hour[n]}, minute, dayOfWeek, results);
            }
        } else if (minute.length > 1) {
            for (int n = 0; n < minute.length; n++) {
                buildSubPatterns(month, day, hour, new String[] {minute[n]}, dayOfWeek, results);
            }
        } else if (dayOfWeek.length > 1) {
            for (int n = 0; n < dayOfWeek.length; n++) {
                buildSubPatterns(month, day, hour, minute, new String[] {dayOfWeek[n]}, results);
            }
        } else {
            results.add(minute[0] + " " + hour[0] + " " + day[0] + " " + month[0] + " " + 
                    dayOfWeek[0]);
        }
    }
    
    /**
     * Given a parsed cron pattern, this determines the period of repetition (basically
     * searching for the highest specified repeating marker).
     */
    public static long getPatternPeriod(String pattern) {
        String tokens[] = splitOnSpaces(pattern);
        // if * * * X *, return 1 year
        if (!tokens[MONTH_IDX].equals(STAR)) {
            return 365L * 24L * 60L * 60000L;
        }
        // if * * X * *, return 1 month (min 28 days, and rely on regex matching for extra 3 days)
        else if (!tokens[DAY_IDX].equals(STAR)) {
            return 28L * 24L * 60L * 60000L;
        } 
        // if * * * * X, return 1 week
        else if (!tokens[DAY_OF_WEEK_IDX].equals(STAR)) {
            return 7L * 24L * 60L * 60000L;
        } 
        // if * X * * *, return 1 day
        else if (!tokens[HOUR_IDX].equals(STAR)) {
            return 24L * 60L * 60000L;
        } 
        // if X * * * *, return 1 hour
        else if (!tokens[MINUTE_IDX].equals(STAR)) {
            return 60L * 60000L;
        } 
        // else 1 minute 
        else {
            return 60000L;
        }
    }
    
    /**
     * Assumes a date format of MMddHHmmF, building a regex to match dates against 
     * the cron pattern. If a non-star token is found, specify all below tokens to be
     * minimum values (eg "* 3 * * *" actually means "* 3 0 0 *"). This is to stop 
     * a job from executing on restart if a month 1st job exists and we restart during
     * the day.
     */
    public static String convertCronPatternToRegex(String cronPattern) {
        String splitOnSpaces[] = splitOnSpaces(cronPattern);
        return
            // month
            (splitOnSpaces[MONTH_IDX].equals(STAR) ? "[0-9][0-9]" : splitOnSpaces[MONTH_IDX]) +
            // day of month
            (splitOnSpaces[DAY_IDX].equals(STAR) ? 
                    (splitOnSpaces[MONTH_IDX].equals(STAR) ? 
                            "[0-9][0-9]" : "01") : splitOnSpaces[DAY_IDX]) +
            // hour
            (splitOnSpaces[HOUR_IDX].equals(STAR) ? 
                    (splitOnSpaces[MONTH_IDX].equals(STAR) && 
                     splitOnSpaces[DAY_IDX].equals(STAR) && 
                     splitOnSpaces[DAY_OF_WEEK_IDX].equals(STAR) ? 
                            "[0-9][0-9]" : "00") : splitOnSpaces[HOUR_IDX]) +
            // minute
            (splitOnSpaces[MINUTE_IDX].equals(STAR) ? 
                    (splitOnSpaces[MONTH_IDX].equals(STAR) && 
                     splitOnSpaces[HOUR_IDX].equals(STAR) && 
                     splitOnSpaces[DAY_IDX].equals(STAR) && 
                     splitOnSpaces[DAY_OF_WEEK_IDX].equals(STAR) ? 
                            "[0-9][0-9]" : "00"): splitOnSpaces[MINUTE_IDX]) +
            // day of week
            (splitOnSpaces[DAY_OF_WEEK_IDX].equals(STAR) ? "[0-9]" : splitOnSpaces[DAY_OF_WEEK_IDX]);
    }
    
    /**
     * Take a single cron token, and remove any duplicates, modulus on max value, etc. Basically
     * clean it up and make it legal
     */
    protected static String formatCronPatternToken(String token, int minValue, int maxValue) {
        // Break on commas
        String splitOnCommas[] = splitOnCommas(token);
        for (int n = 0; n < splitOnCommas.length; n++) {
            splitOnCommas[n] = splitOnCommas[n].trim();
            if (splitOnCommas[n].equals(STAR)) {
                return STAR;
            }
            int parsed = Integer.parseInt(splitOnCommas[n]);
            if (parsed < minValue) {
                LogFactory.getLog(CronUtils.class).warn("Skipping cron pattern element " + splitOnCommas[n] + 
                        " - should be between " + minValue + " and " + maxValue);
            } else if (parsed > maxValue) {
                parsed = ((parsed - minValue) % (maxValue + 1 - minValue)) + minValue;
            }
            splitOnCommas[n] = Integer.toString(parsed);
        }

        Arrays.sort(splitOnCommas, new Comparator<String>() {
            public int compare(String one, String two) {
                return Integer.valueOf(one).compareTo(Integer.valueOf(two));
            }
        });
        StringBuffer out = new StringBuffer();
        for (int n = 0; n < splitOnCommas.length; n++) {
            if ((n == 0) || !splitOnCommas[n].equals(splitOnCommas[n - 1])) {
                out.append(splitOnCommas[n]).append(",");
            }
        }
        return out.length() > 0 ? out.substring(0, out.length() - 1) : STAR;
    }
    
    private static String[] splitOnSpaces(String pattern) {
        return StringUtils.tokenizeToArray(pattern.trim(), " \t");
    }
    
    private static String[] splitOnCommas(String pattern) {
        return StringUtils.tokenizeToArray(pattern, ",");
    }

    private static final SimpleDateFormat MINS_ONLY_DATEMASK = new SimpleDateFormat("yyyyMMddHHmm"); 
    
    /**
     * Gets the timestamp that matches the zeroth second of the minute for the 
     * supplied date.
     */
    public static long getZerothSecondMinuteTimestamp(long inputTimestamp) {
        Date input = new Date(inputTimestamp);
        try {
            synchronized (MINS_ONLY_DATEMASK) {
                input = MINS_ONLY_DATEMASK.parse(MINS_ONLY_DATEMASK.format(input));
            }
        } catch (ParseException err) {
            LogFactory.getLog(CronUtils.class).error("Error parsing date for cron: " + input, err);        
        }
        return input.getTime();
    }
    
    /**
     * Sleeps whatever the remaining period is between now and the next time after the 
     * input timestamp that will be the zero-th second of the minute
     */
    public static void sleepUntilNextMinute(long inputTimestamp) throws InterruptedException {
        // wait until the zero'th second of the next minute
        long sleepPeriod = getZerothSecondMinuteTimestamp(inputTimestamp) + 60000L - 
                    System.currentTimeMillis();
        if (sleepPeriod > 0) {
            Thread.sleep(sleepPeriod);
        }
    }

}
