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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class ReflectingRowMapper<T> implements ParameterizedRowMapper<T> {
    
    private ColumnMetadata columns[];
    private String alias;
    private Class<T> valueobjectClass;
    
    public ReflectingRowMapper(ColumnMetadata columns[], String alias, Class<T> valueobjectClass) {
        this.columns = columns;
        this.alias = alias;
        this.valueobjectClass = valueobjectClass;
    }

    public T mapRow(ResultSet resultset, int rowNo) throws SQLException {
        T out = null;
        try {
            out = this.valueobjectClass.newInstance();  
        } catch (IllegalAccessException err) {
            throw new RuntimeException("Error creating valueobject class: " + this.valueobjectClass, err);
        } catch (InstantiationException err) {
            throw new RuntimeException("Error creating valueobject class: " + this.valueobjectClass, err);
        }
        for (ColumnMetadata column : this.columns) {
            column.setFromResultset(out, resultset, this.alias);
        }
        return out;
    }
}
