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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements an enumeration check - ensures the field is either null or 
 * in the set of specified values
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: EnumerationValidation.java,v 1.7 2008/10/21 10:52:47 rickknowles Exp $
 */
public class EnumerationValidation extends ValidationCheck {
    private final Log log = LogFactory.getLog(EnumerationValidation.class);
    
    public ValidationFailure validate(Object value) {
        String enumeration[] = (String []) getExtraInfo();
        Arrays.sort(enumeration);
        log.debug("Validating enum: " + value + " enum=" + enumeration +
                " size=" + enumeration.length);
        
        if ((value == null) || value.equals("")){
            return null; // check elsewhere
        }
        // Note enums are always arrays of strings
        else {
            for (String item : enumeration) {
                if (item.equals("" + value)) {
                    return null;
                }
            }
            return new ValidationFailure(getFieldName(), Validation.ENUMERATION, value, enumeration);
        }
    }
}
