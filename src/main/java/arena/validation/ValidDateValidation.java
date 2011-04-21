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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements an alphanumeric only check - ensures the field is either null or 
 * a numeric value
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: ValidDateValidation.java,v 1.1 2005/02/13 08:04:37 rickknowles Exp $
 */
public class ValidDateValidation extends ValidationCheck {
	
	public static String getType() {
		return "validDate";
	}
	
	public ValidationFailure validate(Object value) {
		if (value == null) {
			return null;
		}
        String format = "" + getExtraInfo();
        DateFormat df = new SimpleDateFormat(format);
        try {
            String input = "" + value;
            Date dummy = df.parse(input);
            if (df.format(dummy).equals(input)) {
                return null;
            } else {
                return new ValidationFailure(getFieldName(), getType(), value);
            }
        } catch (ParseException err) {
            return new ValidationFailure(getFieldName(), getType(), value);
        }
	}
}
