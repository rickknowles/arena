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

import arena.utils.SimpleValueObject;


/**
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: ValidationFailure.java,v 1.10 2006/09/27 13:16:42 rickknowles Exp $
 */
public class ValidationFailure extends SimpleValueObject{
    
    private String type;
    private String field;
    private Object value;
    private Object additionalInfo;
    
    public ValidationFailure() {
        super();
    }
    
    public ValidationFailure(String field, String type, Object value) {
        this();
        this.type = type;
        this.field = field;
        this.value = value;
    }
    
    public ValidationFailure(String field, String type, Object value,
            Object additionalInfo) {
        this(field, type, value);
        this.additionalInfo = additionalInfo;
    }
    
    public String getMessage() {
        return "Error validating " + this.field + ": value=" + this.value +
        " error type=" + this.type;
    }
    
    public String getField() {
        return field;
    }
    
    public String getType() {
        return type;
    }
    
    public Object getValue() {
        return value;
    }
    
    public Object getAdditionalInfo() {
        return additionalInfo;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
    
    public void setAdditionalInfo(Object additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
    
    public String toString() {
        return "[ValidationFailure: type=" + type + " field=" + field + 
                " value=" + value + " additionalInfo=" + additionalInfo + "]";
    }
}
