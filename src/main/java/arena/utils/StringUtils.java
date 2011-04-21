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

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * General utility functions for dealing with strings.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class StringUtils {
    
    public static String stringReplace(CharSequence input, CharSequence fromMarker, CharSequence toValue) {
        return stringReplace(input, 0, input.length(), fromMarker, toValue);
    }
    
    /**
     * Does string replace, but allows a callback to process the unmatched part. If the onUnmatched
     * callback function is null, the unmatched string is echoed through unchanged.
     * 
     * NOTE: main value of this function is that it can do a string replace with only CharSequence
     * implementations as input. This opens the field a lot wider for memory efficient matching on
     * large strings. It also prevents replacement of already replaced text.
     */
    public static String stringReplace(CharSequence input, int fromIndex, int toIndex, CharSequence fromMarker, CharSequence toValue) {
        if (input == null) {
            return null;
        } else if (fromMarker == null) {
            return input.toString();
        }

        StringBuilder out = new StringBuilder();
        int index = fromIndex;
        int foundAt = findCharSequence(input, fromMarker, index, toIndex);
        while (foundAt != -1) {
            CharSequence outUnMatched = input.subSequence(index, foundAt);
            out.append(outUnMatched);
            out.append(toValue);
            index = foundAt + fromMarker.length();
            foundAt = findCharSequence(input, fromMarker, index, toIndex);
        }
        CharSequence outUnMatched = input.subSequence(index, input.length());
        out.append(outUnMatched);
        return out.toString();
    }
    
    private static int findCharSequence(CharSequence toBeSearched, CharSequence key, int fromOffset, int toOffset) {
        int searchedIndex = 0;
        int searchLength = toOffset - fromOffset;
        int keyIndex = 0;
        int keyLength = key.length();
        while (searchedIndex < searchLength) {
            if (toBeSearched.charAt(fromOffset + searchedIndex++) != key.charAt(keyIndex)) {
                keyIndex = 0;
            } else {
                keyIndex++;
                if (keyIndex >= keyLength) {
                    return searchedIndex - keyIndex + fromOffset;
                }
            }
        }
        return -1;
    }
    
    public static String stringReplace(CharSequence input, CharSequence[][] fromTos) {
        if (input == null) {
            return null;
        } else if (fromTos == null || fromTos.length == 0) {
            return input.toString();
        }

        StringBuilder out = new StringBuilder();
        stringReplace(input, 0, input.length(), fromTos, 0, out);
        return out.toString();
    }
    
    private static void stringReplace(CharSequence input, int fromIndex, int toIndex, CharSequence[][] fromTos, int markerIndex, StringBuilder out) {
        int index = fromIndex;
        int foundAt = findCharSequence(input, fromTos[markerIndex][0], index, toIndex);
        while (foundAt != -1) {
            if (markerIndex < fromTos.length - 1) {
                stringReplace(input, index, foundAt, fromTos, markerIndex + 1, out);
            } else {
                out.append(input.subSequence(index, foundAt));
            }
            out.append(fromTos[markerIndex][1]);
            index = foundAt + fromTos[markerIndex][0].length();
            foundAt = findCharSequence(input, fromTos[markerIndex][0], index, toIndex);
        }
        if (markerIndex < fromTos.length - 1) {
            stringReplace(input, index, toIndex, fromTos, markerIndex + 1, out);
        } else {
            out.append(input.subSequence(index, toIndex));
        }
    }

    public static String upperFirstChar(String input) {
        return (input.length() == 0) ? input : (input.substring(0, 1)
                                                     .toUpperCase() + input.substring(1));
    }

    public static String lowerFirstChar(String input) {
        return (input.length() == 0) ? input : (input.substring(0, 1)
                                                     .toLowerCase() + input.substring(1));
    }
    
    public static String makeLowerMixedCase(String input) {
        if (input == null) {
            return null;
        } else if (input.length() < 2) {
            return input.toLowerCase();
        } else if (Character.isLowerCase(input.charAt(0))) {
            return input;
        } else if (Character.isLowerCase(input.charAt(1))) {
            return Character.toLowerCase(input.charAt(0)) + input.substring(1);
        } else {
            // Iterate till the first lower case char, then lowercase everything 
            // until the char before it
            for (int n = 0; n < input.length(); n++) {
                if (Character.isLowerCase(input.charAt(n))) {
                    return input.substring(0, n - 1).toLowerCase() + 
                            input.charAt(n - 1) + input.substring(n);
                }
            }
            // otherwise lowercase everything
            return input.toLowerCase();
        }
    }

    public static String lmcToUnderscore(String input) {
        StringBuffer out = new StringBuffer();

        for (int n = 0; n < input.length(); n++)
            if (Character.isUpperCase(input.charAt(n))) {
                out.append((n == 0) ? "" : "_")
                   .append(Character.toLowerCase(input.charAt(n)));
            } else {
                out.append(input.charAt(n));
            }

        return out.toString();
    }

    public static String upperCase(String input) {
        return input.toUpperCase();
    }

    public static String currentDate(String format, String locale) {
        return formatDate(new Date(), format, locale);
    }

    public static String formatDate(Date date, String format, String localeText) {
        String localeLanguage = null;
        String localeCountry = null;

        if ((localeText == null) || localeText.equals("") || (localeText.indexOf('_') == -1)) {
            localeLanguage = "en";
            localeCountry = "US";
        } else {
            localeLanguage = localeText.substring(0, localeText.indexOf('_'));
            localeCountry = localeText.substring(localeText.indexOf('_') + 1);
        }

        DateFormat sdf = new SimpleDateFormat(format, new Locale(localeLanguage, localeCountry));

        return sdf.format(date);
    }

    public static String rpad(String input, int finalLength) {
        return rpad(input, " ", finalLength);
    }
    
    public static String rpad(String input, String padChar, int finalLength) {
        StringBuffer out = new StringBuffer(input);

        int currentLength = out.length();
        while (currentLength < finalLength) {
            out.append(padChar);
            currentLength = out.length();
            if (currentLength  > finalLength) {
                // in case we went over
                out.setLength(finalLength);
                currentLength = finalLength;
            }
        }

        return out.toString();
    }

    public static String lpad(String input, String padChar, int finalLength) {
        StringBuffer out = new StringBuffer(input);

        int currentLength = out.length();
        while (currentLength < finalLength) {
            out.insert(0, padChar.subSequence(0, Math.min(padChar.length(), finalLength - currentLength)));
            currentLength = out.length();
        }

        return out.toString();
    }

    public static String escapeQuotes(String input) {
        return stringReplace(input, "\"", "\\\"");
    }

    public static String md5Encode(String input, String encoding) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5")
                .digest(input.getBytes(encoding == null ? "UTF-8" : encoding));
            return hexEncode(digest);
        } catch (Throwable err) {
            throw new RuntimeException("Error doing md5 encode on " + input, err);
        }
    }

    public static String hexEncode(byte input[]) {

        StringBuffer out = new StringBuffer();

        for (int i = 0; i < input.length; i++)
            out.append(Integer.toString((input[i] & 0xf0) >> 4, 16))
               .append(Integer.toString(input[i] & 0x0f, 16));

        return out.toString();
    }
    
    public static byte[] hexDecode(String input) {

        if (input == null) {
            return null;
//        } else if (input.length() % 2 != 0) {
//            throw new RuntimeException("Invalid hex for decoding: " + input);
        } else {
            byte output[] = new byte[(int) Math.ceil(input.length() / 2)];
            int outputPosition = output.length - 1;
            
            for (int i = input.length() - 1; i >= 0; i--) {
                int twoDigit = Integer.parseInt(input.substring(i, i+1), 16);
                if (i > 0) {
                    twoDigit += Integer.parseInt(input.substring(i-1, i), 16) << 4;
                    i--;
                }
                output[outputPosition--] = (byte) (twoDigit & 0xff);
            }
            return output;
        }
    }
    
    public static String[] tokenizeToArray(String input, String delims) {
        List<String> results = new ArrayList<String>();
    	if (input != null && input.length() > 0) {
	        StringTokenizer st = new StringTokenizer(input, delims, true);
	        boolean lastWasDelim = true;
	
	        for (; st.hasMoreTokens();) {
	            String token = st.nextToken();
	
	            if (delims.indexOf(token) == -1) {
	                results.add(token);
	                lastWasDelim = false;
	            } else if (lastWasDelim) {
	                results.add("");
	            } else {
	                lastWasDelim = true;
	            }
	        }
    	}

        return results.toArray(new String[results.size()]);
    }
    
    public static String removeOutsideQuotes(String input) {
        if (input == null) {
            return null;
        } else {
            if (input.startsWith("\"")) {
                input = input.substring(1);
            }
            if (input.endsWith("\"")) {
                input = input.substring(0, input.length() - 1);
            }
            return input;
        }
    }
    
    public static String getBefore(String input, String delimiter) {
        if (input == null) {
            return null;
        } else if (delimiter == null) {
            return null;
        } else  {
            int position = input.indexOf(delimiter);
            if (position == -1) {
                return null;
            } else {
                return input.substring(0, position);
            }
        }
    }
    
    public static <T> Map<T,T> makeLookupTable(T elements[][]) {
        return makeLookupTable(elements, true, false);
    }
    
    public static <T> Map<T,T> makeLookupTable(T elements[][], boolean readonly, boolean threadsafe) {
        Map<T,T> table = new HashMap<T,T>();
        if (elements != null) {
            for (int n = 0; n < elements.length; n++) {
                T[] row = elements[n];
                T key = null;
                T value = null;
                if (row.length > 0) {
                    key = row[0];
                }
                if (row.length > 1) {
                    value = row[1];
                }
                table.put(key, value);
            }
        }
        if (readonly) {
            table = Collections.unmodifiableMap(table);
        }
        if (threadsafe) {
            table = Collections.synchronizedMap(table);
        }
        return table;
    }
}
