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
package arena.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basically just an email object, but also includes facilities for token
 * replacement and attachment inlining.
 */
public class MailAddressUtils {

    /**
     * Builds a list of internet address objects by parsing the
     * address list of the form "name <email>, name <email>"
     */
    public static InternetAddress[] parseAddressList(String addressList, String delim, String encoding) {
        if ((addressList == null) || (addressList.trim().length() == 0)) {
            return new InternetAddress[0];
        }
        Log log = LogFactory.getLog(MailAddressUtils.class);
        log.debug("Address list for parsing: " + addressList);
        StringTokenizer st = new StringTokenizer(addressList.trim(), delim);
        List<InternetAddress> addresses = new ArrayList<InternetAddress>();
        
        for (int n = 0; st.hasMoreTokens(); n++) {
            String fullAddress = st.nextToken().trim();
            if (fullAddress.equals("")) {
                continue;
            }
            
            try {
                int openPos = fullAddress.indexOf('<');
                int closePos = fullAddress.indexOf('>');

                if (openPos == -1) {
                    addresses.add(new InternetAddress((closePos == -1)
                            ? fullAddress.trim()
                            : fullAddress.substring(0, closePos).trim()));
                } else if (closePos == -1) {
                    addresses.add(new InternetAddress(fullAddress.substring(openPos + 1).trim(),
                            fullAddress.substring(0, openPos).trim(), encoding));
                } else {
                    addresses.add(new InternetAddress(fullAddress.substring(openPos + 1, closePos).trim(),
                            fullAddress.substring(0, openPos).trim(), encoding));
                }
            } catch (Throwable err) {
                throw new RuntimeException("Error parsing address: " +
                    fullAddress, err);
            }
        }

        log.debug("Found mail addresses: " + addresses);

        return (InternetAddress []) addresses.toArray(new InternetAddress[addresses.size()]);
    }
    
    public static String formatAddress(String name, String email) {
        if ((name != null) && !name.equals("")) {
            return name + "<" + email + ">";
        } else {
            return email;
        }
    }
}
