/*
 * Generator Runtime Servlet Framework
 * Copyright (C) 2004 Rick Knowles
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
package arena.validation;

/**
 * Implements an alphanumeric only check - ensures the field is either null or 
 * a numeric value
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: KeitaiEmailAccountValidation.java,v 1.2 2005/10/24 13:47:20 rickknowles Exp $
 */
public class KeitaiEmailAccountValidation extends ValidationCheck {
    
	private static final String EVIL_DOMAINS[] = {
		"docomo.ne.jp",
		"ezweb.ne.jp",
		"vodafone.ne.jp"		
	}; 
	
    public ValidationFailure validate(Object valueObj) {
        if (valueObj == null) {
            return null; // check elsewhere
        }
        String strValue = (valueObj + "").toLowerCase();
        if (strValue.equals("")) {
            return null;
        }
        for (int n = 0; n < EVIL_DOMAINS.length; n++) {
        	if (strValue.endsWith(EVIL_DOMAINS[n])) {
                return new ValidationFailure(getFieldName(), 
                        Validation.KEITAI_EMAIL_ACCOUNT_CHECK, 
                        strValue);        		
        	}
        }
        return null;
    }
}
