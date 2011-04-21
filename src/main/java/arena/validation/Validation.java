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
 * Keeps a list of the known types of Validations as constants.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: Validation.java,v 1.8 2008/10/21 10:52:47 rickknowles Exp $
 */
public class Validation {
    public static final String ALPHABET_ONLY = "alphabetOnly";
    public static final String ALPHANUMERIC_ONLY = "alphanumericOnly";
    public static final String ALPHANUMERICSPACE_ONLY = "alphanumericSpaceOnly";
    public static final String EMAIL_CHECK = "email";
    public static final String ENUMERATION = "enumeration";
    public static final String MAX_LENGTH = "maxLength";
    public static final String MIN_LENGTH = "minLength";
    public static final String NULL_CHECK = "required";
    public static final String NUMERIC_ONLY = "numericOnly";
    public static final String URL_CHECK = "url";
    public static final String ZENKAKU_ONLY = "zenkakuOnly";
    public static final String VALID_DATE = "validDate";
    public static final String ALL_SAME_CHARACTERS = "allSameCharacters";
    public static final String PASSWORD_STRENGTH = "passwordStrength";
    
    public static final String PUBLIC_EMAIL_ACCOUNT_CHECK = "publicEmailAccount";
    public static final String KEITAI_EMAIL_ACCOUNT_CHECK = "keitaiEmailAccount";
    
    private List<ValidationCheck> validationQueue;
    private String fieldName;
    private Object value;
    
    public Validation(String fieldName, Object value) {
        this.validationQueue = new ArrayList<ValidationCheck>();
        this.fieldName = fieldName;
        this.value = value;
    }
    
    public void addValidation(String validationType) {
        addValidation(validationType, null);
    }
    
    public void addValidation(ValidationCheck vc) {
        vc.setFieldName(this.fieldName);
        this.validationQueue.add(vc);
    }
    
    public void addValidation(String validationType, Object additionalInfo) {
        String validationClassName = validationType;
        if (validationType.indexOf(".") == -1) {
            validationClassName = getClass().getPackage().getName() + 
                    "." + StringUtils.upperFirstChar(validationType) + 
                        "Validation";
        }
        ValidationCheck vc = getValidation(validationClassName, additionalInfo);
        this.validationQueue.add(vc);
    }
    
    public boolean validate(List<ValidationFailure> errorList) {
        ValidationFailure failure = null;
        int n = 0;
        
        for (Iterator<ValidationCheck> i = this.validationQueue.iterator();
        i.hasNext() && (failure == null); n++) {
            ValidationCheck vc = i.next();
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
            Object extraInfo) {
        try {
            ValidationCheck vc = (ValidationCheck) Class.forName(
                    validationClassName).newInstance();
            vc.setFieldName(this.fieldName);
            vc.setExtraInfo(extraInfo);
            return vc;
        } catch (Throwable err) {
            throw new RuntimeException("Can't find validationClass = " +
                    validationClassName, err);
        }
    }
}
