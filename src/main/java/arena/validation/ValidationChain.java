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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import arena.utils.StringUtils;




/**
 * Chains a validation set together, and on validate() it aborts after the
 * first error.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: ValidationChain.java,v 1.3 2007/04/10 05:22:27 rickknowles Exp $
 */
public class ValidationChain {
    
    private List<ValidationCheck> validationQueue;
    private List<Object> values;
    
    public ValidationChain() {
        this.validationQueue = new ArrayList<ValidationCheck>();
        this.values = new ArrayList<Object>();
    }
    
    public void addValidation(String validationType, 
            String fieldName, Object value) {
        addValidation(validationType, fieldName, value, null);
    }
    
    public void addValidation(String validationType, 
            String fieldName, Object value, Object additionalInfo) {
        this.values.add(value);
        String validationClassName = validationType;
        if (validationType.indexOf(".") == -1) {
            validationClassName = getClass().getPackage().getName() + 
                    "." + StringUtils.upperFirstChar(validationType) + 
                    "Validation";
        }
        ValidationCheck vc = getValidation(validationClassName, 
                fieldName, additionalInfo);
        this.validationQueue.add(vc);
    }
    
    public boolean validate(List<ValidationFailure> errorList) {
        ValidationFailure failure = null;
        int n = 0;
        
        for (Iterator<ValidationCheck> i = this.validationQueue.iterator();
        i.hasNext() && (failure == null); n++) {
            ValidationCheck vc = i.next();
            Object value = this.values.get(n);
            failure = vc.validate(value);
        }
        
        if ((failure != null) && (errorList != null)) {
            errorList.add(failure);
        }
        
        return failure == null;
    }
    
    /**
     * Try to load and initialise a validationCheck implementation class
     */
    private ValidationCheck getValidation(String validationClassName,
            String fieldName, Object extraInfo) {
        try {
            ValidationCheck vc = (ValidationCheck) Class.forName(
                    validationClassName).newInstance();
            vc.setFieldName(fieldName);
            vc.setExtraInfo(extraInfo);
            return vc;
        } catch (Throwable err) {
            throw new RuntimeException("Can't find validationClass = " +
                    validationClassName, err);
        }
    }
}
