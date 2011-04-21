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

import java.util.regex.Pattern;

/**
 * Implements an alphanumeric only check - ensures the field is either null or 
 * a numeric value
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: EmailValidation.java,v 1.3 2005/10/24 13:47:20 rickknowles Exp $
 */
public class EmailValidation extends ValidationCheck {

//    private static final char ALLOWED_CHARS[] = {'-', '.', '@', '_'};
    private static final Pattern NOT_ALLOWED_CHARS = Pattern.compile("[^a-zA-Z0-9\\-\\.\\@\\_]");
    
    public ValidationFailure validate(Object valueObj) {
        if (valueObj == null) {
            return null; // check elsewhere
        }
        String strValue = (valueObj + "").toLowerCase();
        if (strValue.equals("")) {
            return null;
        }
        
        int atPos = strValue.indexOf('@');
        int dotPos = strValue.lastIndexOf('.');
        
        if ((atPos == -1) || (dotPos == -1) || (atPos > dotPos - 1) ||
                (strValue.length() < 5) || strValue.endsWith(".")) {
            return new ValidationFailure(getFieldName(), 
                    Validation.EMAIL_CHECK, strValue.toLowerCase());
        } else if (NOT_ALLOWED_CHARS.matcher(strValue).find()) {
            return new ValidationFailure(getFieldName(), 
                    Validation.EMAIL_CHECK, strValue.toLowerCase());
        } else {
            return null;
        }
    }
}
