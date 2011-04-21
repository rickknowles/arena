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

public abstract class ValidationCheck {
    
    private String fieldName;
    private Object extraInfo;
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public void setExtraInfo(Object extraInfo) {
        this.extraInfo = extraInfo;
    }
    
    protected String getFieldName() {
        return this.fieldName;
    }
    
    protected Object getExtraInfo() {
        return this.extraInfo;
    }
    
    public abstract ValidationFailure validate(Object value);
}