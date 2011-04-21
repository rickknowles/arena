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
 * Implements a numeric only check - ensures the field is either null or
 * a numeric value
 *
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: NumericOnlyValidation.java,v 1.1 2005/02/04 04:40:50 rickknowles Exp $
 */
public class DecimalOnlyValidation extends ValidationCheck {

    private boolean positiveOnly;
    
    public DecimalOnlyValidation() {
        this(false);
    }
    
    public DecimalOnlyValidation(boolean positiveOnly) {
        this.positiveOnly = positiveOnly;
    }
    
    public ValidationFailure validate(Object value) {
        if (value == null || value.equals("")) {
            return null;
        }
        boolean failed = false;
        try {
            double parsed = Double.parseDouble(value.toString());
            failed = Pattern.compile("\\p{Alpha}").matcher(value.toString()).find();
            if (!failed && positiveOnly) {
                if (parsed < 0) {
                    failed = true;
                }
            }
        } catch (Throwable err) {
            failed = true;
        }
        return failed ? new ValidationFailure(getFieldName(),
                Validation.NUMERIC_ONLY, value) : null;
    }
}
