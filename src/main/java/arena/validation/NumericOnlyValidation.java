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
 * Implements a numeric only check - ensures the field is either null or
 * a numeric value
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: NumericOnlyValidation.java,v 1.1 2005/02/04 04:40:50 rickknowles Exp $
 */
public class NumericOnlyValidation extends ValidationCheck {

    public ValidationFailure validate(Object value) {
        if (value == null || value.equals("")) {
            return null;
        }
        boolean foundNonNumeric = false;
        String strValue = value + "";

        for (int n = 0; (n < strValue.length()) && !foundNonNumeric; n++) {
            char current = strValue.charAt(n);
            foundNonNumeric = (current < '0') || (current > '9');
        }

        return foundNonNumeric ? new ValidationFailure(getFieldName(),
                Validation.NUMERIC_ONLY, value) : null;
    }
}
