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
 * Implements a check to see if all characters in this string are the same. This is 
 * used to prevent passwords such as 2222.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles </a>
 * @version $Id: AllSameCharactersValidation.java,v 1.1 2005/04/15 10:37:37 rickknowles Exp $
 */
public class AllSameCharactersValidation extends ValidationCheck {

    public ValidationFailure validate(Object value) {
        if (value == null) { return null; }
		String valueStr = (value + "");
		if (valueStr.length() > 0) {
	        char firstChar = valueStr.charAt(0);
			for (int n = 1; n < valueStr.length(); n++) {
				if (valueStr.charAt(n) != firstChar) {
					return null;
				}
			}
			return new ValidationFailure(getFieldName(),
	                Validation.ALL_SAME_CHARACTERS, value);
		} else {
			return null;
		}
    }
}