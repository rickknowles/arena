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
 * @version $Id: UrlValidation.java,v 1.3 2007/07/01 05:14:42 rickknowles Exp $
 */
public class UrlValidation extends ValidationCheck {
    
    public ValidationFailure validate(Object valueObj) {
        if (valueObj == null) {
            return null; // check elsewhere
        } else if (valueObj.equals("")) {
            return null;
        }
        
        String value = (String) valueObj;
        
        if ((value.indexOf("://") == -1) || (value.length() < 11)) {
            return new ValidationFailure(getFieldName(), Validation.URL_CHECK, value);
        } else {
            return null;
        }
    }
}
