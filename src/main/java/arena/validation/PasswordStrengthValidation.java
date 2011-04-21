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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PasswordStrengthValidation extends ValidationCheck {
    private final Log log = LogFactory.getLog(PasswordStrengthValidation.class);
    
    public ValidationFailure validate(Object value) {
        if (value == null) {
            return null;
        }
        
        /* adapted from the logic described at geekwisdom.com in the passwordchecker.js file: */
        String strValue = value.toString();
        int score = lengthScore(strValue);
        log.debug("Scores: length=" + score);
        
        int letterScore = lettersScore(strValue);
        score += letterScore;
        log.debug("Scores: letters=" + letterScore);
        
        int numberScore = numbersScore(strValue);
        score += numberScore;
        log.debug("Scores: numbers=" + numberScore);
        
        int specialCharsScore = specialCharsScore(strValue);
        score += specialCharsScore;
        log.debug("Scores: special=" + specialCharsScore);
        
        /*
            combinations:
            level 0 (1 points): letters and numbers exist
            level 1 (1 points): mixed case letters
            level 1 (2 points): letters, numbers and special characters exist
            level 1 (2 points): mixed case letters, numbers and special characters exist
         */
        if (numberScore > 0 && letterScore > 0) {
            score += 1;
        }
        if (numberScore > 0 && letterScore > 0 && specialCharsScore > 0) {
            score += 2;
        }
        if (numberScore > 0 && letterScore >= 7 && specialCharsScore > 0) {
            score += 2;
        }
        
        Number minScore = (Number) getExtraInfo();
        if (minScore == null) {
            minScore = new Integer(30);
        }
        log.debug("Scores: final=" + score + ", min=" + minScore);
        
        return (minScore.intValue() > score) ? new ValidationFailure(getFieldName(), 
                Validation.PASSWORD_STRENGTH, value, new Integer(score)) : null;
    }
    
    /**
     * Password Strength Factors and Weightings
     * password length:
     *   level 0 (3 point): less than 4 characters
     *   level 1 (6 points): between 5 and 7 characters
     *   level 2 (12 points): between 8 and 15 characters
     *   level 3 (18 points): 16 or more characters
     */
    private int lengthScore(String text) {
        int length = text.length();
        if (length < 4) {
            return 3;
        } else if (length < 8) {
            return 6;
        } else if (length < 15) {
            return 12;
        } else {
            return 18;
        }
    }
    
    /**
     * Password Strength Factors and Weightings
     *   letters:
     *      level 0 (0 points): no letters
     *      level 1 (5 points): all letters are lower case
     *      level 2 (7 points): letters are mixed case
     */
    private int lettersScore(String text) {
        if (!Pattern.compile("\\p{Alpha}").matcher(text).find()) {
            return 0;
        } else if (!Pattern.compile("\\p{Upper}").matcher(text).find()) {
            return 5;
        } else if (!Pattern.compile("\\p{Lower}").matcher(text).find()) {
            return 5;
        } else {
            return 7;
        }
    }
    
    /**
     * Password Strength Factors and Weightings
     *   numbers:
     *      level 0 (0 points): no numbers exist
     *      level 1 (5 points): one number exists
     *      level 2 (7 points): 3 or more numbers exists
     */
    private int numbersScore(String text) {
        if (!Pattern.compile("\\p{Digit}").matcher(text).find()) {
            return 0;
        } else if (!Pattern.compile("\\p{Digit}.*\\p{Digit}.*\\p{Digit}").matcher(text).find()) {
            return 5;
        } else {
            return 7;
        }
    }
    
    /**
     * Password Strength Factors and Weightings
     *   special characters:
     *      level 0 (0 points): no special characters
     *      level 1 (5 points): one special character exists
     *      level 2 (10 points): more than one special character exists
     */
    private int specialCharsScore(String text) {
        if (!Pattern.compile("[!@#\\$%\\^\\&\\*?_~]").matcher(text).find()) {
            return 0;
        } else if (!Pattern.compile("[!@#\\$%\\^\\&\\*?_~].*[!@#\\$%\\^\\&\\*?_~]").matcher(text).find()) {
            return 5;
        } else {
            return 10;
        }
    }
}
