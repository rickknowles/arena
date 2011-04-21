/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
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
package arena.dao;

import java.math.BigDecimal;
import java.util.Date;

public class AnsiSQLDialect implements SQLDialect {

    public String makeCaseWhen(String whenClause, String thenClause, String elseClause) {
        return "CASE WHEN " + whenClause + " THEN " + thenClause + " ELSE " + elseClause + " END";
    }

    public boolean supportsAutonumberColumns() {
        return false;
    }

    public String upperCaseFunction(String argument) {
        return "UPPER(" + argument + ")";
    }
    
    public String convertJavaTypeToDBType(Class<?> javaType, int size) {
        if (javaType != null) {
            if (Date.class.isAssignableFrom(javaType)) {
                return "TIMESTAMP";
            } else if (BigDecimal.class.isAssignableFrom(javaType) ) {
                return "FLOAT";
            } else if (Float.class.isAssignableFrom(javaType) || javaType.equals(Float.TYPE)) {
                return "FLOAT";
            } else if (Double.class.isAssignableFrom(javaType) || javaType.equals(Double.TYPE)) {
                return "FLOAT";
            } else if (Number.class.isAssignableFrom(javaType) || 
                    javaType.equals(Byte.TYPE) || 
                    javaType.equals(Short.TYPE) || 
                    javaType.equals(Long.TYPE) || 
                    javaType.equals(Integer.TYPE)) {
                return "INT";
            } else if (Boolean.class.isAssignableFrom(javaType) || javaType.equals(Boolean.TYPE)) {
                return "INT";
            } else if (byte[].class.isAssignableFrom(javaType)) {
                return "BLOB";
            }
        }
        return size > 400 ? "TEXT" : "VARCHAR(" + size + ")";
    }
    
    public String createSequenceDDL(String name, int initialValue, int allocationSize) {
        return "CREATE SEQUENCE " + name + " START " + initialValue + " CACHE " + allocationSize;
    }
    
    public String getNextFromSequenceSQL(String name) {
        return "SELECT nextval('" + name + "')";
    }
    
    public boolean supportsLimitClause() {
        return true;
    }

}
