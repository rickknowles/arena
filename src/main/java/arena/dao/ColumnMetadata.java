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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import arena.utils.ReflectionUtils;



public class ColumnMetadata {
    private String fieldName;
    private Class<?> fieldType;
    private String columnName;
    private boolean insertable;
    private boolean updateable;
    private boolean nullable;
    private boolean pkField;
    private int size;
    private String sequenceName;
    private boolean autoNumber;
    
    private char[] buffer = new char[1024];
    
    public ColumnMetadata(String fieldName, Class<?> fieldType, String columnName, 
            boolean insertable, boolean updateable, boolean nullable, 
            boolean pkField, int size, String sequenceName, boolean autoNumber) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.columnName = columnName;
        this.insertable = insertable;
        this.updateable = updateable;
        this.nullable = nullable;
        this.pkField = pkField;
        this.size = size;
        this.sequenceName = sequenceName;
        this.autoNumber = autoNumber;
    }
    
    public void writeUpdateSQLClause(StringBuilder sql, List<Object> bindArgs, Object valueobject) {
        sql.append(columnName).append(" = ?");
        bindArgs.add(convertToDBValue(getFieldFromValueobject(valueobject)));
    }
    
    public void writeInsertSQLColumnClause(StringBuilder sql) {
        sql.append(columnName);
    }
    
    public void writeInsertSQLValuesClause(StringBuilder sql, List<Object> bindArgs, Object valueobject) {
        sql.append("?");
        bindArgs.add(convertToDBValue(getFieldFromValueobject(valueobject)));
    }
    
    public void writeSelectSQLClause(StringBuilder sql, List<Object> bindArgs, String tableAlias) throws IOException {
        if (tableAlias != null) {
            sql.append(tableAlias).append(".");
        }
        sql.append(columnName);
    }
    
    public void writeWhereSQLClause(StringBuilder sql, List<Object> bindArgs, String tableAlias, Object value) throws IOException {
        sql.append(columnName).append(" = ?");
        bindArgs.add(convertToDBValue(value));
    }
    
    public void writeWhereSQLClauseMulti(StringBuilder sql, List<Object> bindArgs, Iterator<?> values, int max) throws IOException {
        sql.append(columnName);
        Object first = values.next();
        if (values.hasNext()) {
            sql.append(" IN (?");
            bindArgs.add(convertToDBValue(first));
            int readSoFar = 1;
            while (values.hasNext() && ((max < 0) || (readSoFar < max))) {
                bindArgs.add(convertToDBValue(values.next()));
                sql.append(", ?");
                readSoFar++;
            }
            sql.append(")");
            return;
        } else {
            sql.append(" = ?");
            bindArgs.add(convertToDBValue(first));
        }
    }
    
    public void writeCreateTableDDL(StringBuilder sql, SQLDialect sqlDialect) {
        sql.append(columnName).append(" ");
        // need to work in autonumber column definitions here somehow
        sql.append(sqlDialect.convertJavaTypeToDBType(fieldType, size));
        if (!nullable) {
            sql.append(" NOT NULL");
        }
    }
    
    public void setFromResultset(Object valueobject, ResultSet resultset, String alias) throws SQLException {
        Object dbValue = DAOUtils.getFieldFromResultSet(makeColumnAlias(alias), this.fieldType, resultset, this.buffer);
        if (dbValue == null && !nullable) {
            throw new RuntimeException("DB value is null, but java field type is not nullable: " + makeColumnAlias(alias));
        }
        ReflectionUtils.setAttributeUsingSetter(this.fieldName, valueobject, dbValue);
    }
    
    protected Object getFieldFromValueobject(Object valueobject) {
        return ReflectionUtils.getAttributeUsingGetter(this.fieldName, valueobject);
    }
    
    public Object convertToDBValue(Object in) {
        return DAOUtils.convertToDBValue(this.fieldType, in);
    }
    
    public String makeColumnAlias(String alias) {
        if ((alias != null) && !alias.equals("")) {
            return alias + "_" + this.columnName; // come up with some way to limit this to 30 chars for oracle
        } else {
            return this.columnName;
        }
    }    
    
    public String makeTableDotColumnName(String alias) {
        if ((alias != null) && !alias.equals("")) {
            return alias + "." + columnName;
        } else {
            return columnName;
        }
    }

    public boolean isPkField() {
        return pkField;
    }

    public void setPkField(boolean pkField) {
        this.pkField = pkField;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isInsertable() {
        return insertable;
    }

    public boolean isUpdateable() {
        return updateable;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public boolean isAutoNumber() {
        return autoNumber;
    }

    public void setAutoNumber(boolean autoNumber) {
        this.autoNumber = autoNumber;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }
    
}
