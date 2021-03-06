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
 * Implements a maximum length check - ensures the field is either null or 
 * a numeric value
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: MaxLengthValidation.java,v 1.2 2005/10/04 13:35:33 rickknowles Exp $
 */
public class MaxLengthValidation extends ValidationCheck {
    
    public ValidationFailure validate(Object value) {
        Long length = Long.valueOf("" + getExtraInfo());
        if (value == null) {
            return null;
        } else if ((length != null) &&
                ((value + "").length() > length.longValue())) {
            return new ValidationFailure(getFieldName(), Validation.MAX_LENGTH, value, length);
        } else {
            return null;
        }
    }
}
