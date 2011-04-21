/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.dao;

/**
 * Implementing classes provide differing interpretations of the SQL specification
 * using this interface for a particular database or SQL variant. 
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public interface SQLDialect {
    
    public String upperCaseFunction(String argument);
    
    public String makeCaseWhen(String whenClause, String thenClause, String elseClause);
    
    public boolean supportsAutonumberColumns();
    
    public String convertJavaTypeToDBType(Class<?> javaType, int size);
    
    public String createSequenceDDL(String name, int initialValue, int allocationSize);

    public String getNextFromSequenceSQL(String name);
    
    public boolean supportsLimitClause();
}
