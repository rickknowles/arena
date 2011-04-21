package arena.dao;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.sql.DataSource;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

import arena.collections.OneItemIterator;
import arena.utils.ReflectionUtils;
import arena.utils.StringUtils;


public class ReflectingDAO<T> extends DAOSupport<T> {
    private final Log log = LogFactory.getLog(ReflectingDAO.class);
    
    private JdbcTemplate readTemplate;
    private JdbcTemplate writeTemplate;
    private Class<T> valueobjectClass;
    private SQLDialect sqlDialect;
    
    private String table;
    private String[] pkFields;
    private ColumnMetadata[] mappings;
    private Map<String,ColumnMetadata> mappingsByName;
    
    private int batchSizeSelectByPK = 512;
    private boolean autoCreateSchema = true;
    private boolean autoSetSequenceOnPK = true;
    private boolean suppressNoPkWarning = false;
    private int sequenceAllocationSize = 1;
    private int sequenceInitialValue = 1000;
    
    private Map<String,String> hardWiredColumnNames;
    private Collection<String> hardWiredIgnoredColumns;
    
    private String deleteTimestampField;
    
    public ReflectingDAO() {}
    
    public ReflectingDAO(Class<T> clazz) {
        this();
        setValueobjectClass(clazz);
    }
    
    public SQLDialect getSqlDialect() {
        return this.sqlDialect;
    }
    
    @SuppressWarnings("unchecked")
    public void setValueobject(String valueobjectClassName) {
        try {
            setValueobjectClass((Class<T>) Class.forName(valueobjectClassName));
        } catch (ClassNotFoundException err) {
            throw new RuntimeException("Class not found: " + valueobjectClassName);
        }
    }

    public void setValueobjectClass(Class<T> clazz) {
        this.valueobjectClass = clazz;
    }
    
    public void setSqlDialect(SQLDialect sqlDialect) {
        this.sqlDialect = sqlDialect;
    }

    public void setAutoCreateSchema(boolean autoCreateSchema) {
        this.autoCreateSchema = autoCreateSchema;
    }

    public void setColumnNames(Map<String, String> columnNames) {
        this.hardWiredColumnNames = columnNames;
    }

    public void setIgnoredColumns(Collection<String> ignoredColumns) {
        this.hardWiredIgnoredColumns = ignoredColumns;
    }

    public void setDeleteTimestampField(String deleteTimestampField) {
        this.deleteTimestampField = deleteTimestampField;
    }

    protected void checkDaoConfig() throws IllegalArgumentException {
        if (this.valueobjectClass == null) {
            throw new RuntimeException("No valueobject class configured");
        }
        if (this.readTemplate == null) {
            if (this.writeTemplate == null) {
                log.warn("WARNING: no read or write data source defined: cannot execute queries with this peer");
            } else {
                log.warn("No read data source defined: ReflectingSQLPersistencePeer will only support write operations");
            }
        } else if (this.writeTemplate == null) {
            log.warn("No write data source defined: ReflectingSQLPersistencePeer will only support read operations");
        }
        if (this.sqlDialect == null) {
            this.sqlDialect = new AnsiSQLDialect();
        }
        String fieldNames[] = ReflectionUtils.getAttributeNamesUsingGetter(this.valueobjectClass);
        
        // if not configured (check for table annotation, otherwise guess from classname)
        if (this.table == null) {
            Table table = this.valueobjectClass.getAnnotation(Table.class);
            if (table != null) {
                this.table = table.name();
            }
            if ((this.table == null) || this.table.equals("")) {
                this.table = StringUtils.lmcToUnderscore(
                        StringUtils.makeLowerMixedCase(
                        ReflectionUtils.extractClassNameWithoutPackage(
                                this.valueobjectClass.getName())));
            }
        }
        
        // if not configured (Check for column name annotation, otherwise guess from field name)
        List<ColumnMetadata> allMappings = new ArrayList<ColumnMetadata>();
        this.mappingsByName = new Hashtable<String,ColumnMetadata>(); // anything threadsafe to get is fine 
        List<String> pkFields = new ArrayList<String>();
        for (String fieldName : fieldNames) {
            if (!fieldName.equals("class")) {
                log.debug("Analyzing property: " + fieldName);
                ColumnMetadata mapping = initializeFieldMapping(fieldName, this.valueobjectClass, 
                        this.table, this.hardWiredColumnNames, this.hardWiredIgnoredColumns);
                if (mapping != null) {
                    allMappings.add(mapping);
                    this.mappingsByName.put(mapping.getFieldName(), mapping);
                    if (mapping.isPkField()) {
                        pkFields.add(mapping.getFieldName());
                    }
                }
            }
        }
        this.mappings = (ColumnMetadata[]) allMappings.toArray(new ColumnMetadata[allMappings.size()]);

        if (pkFields.isEmpty()) {
            if (this.mappingsByName.containsKey("id")) {
                this.pkFields = new String[] {"id"};
            } else if (this.mappingsByName.containsKey(this.table + "Id")) {
                this.pkFields = new String[] {this.table + "Id"};
            } else if (this.mappingsByName.containsKey(this.table + "ID")) {
                this.pkFields = new String[] {this.table + "ID"};
            } else {
                if (!this.suppressNoPkWarning) {
                    log.warn("WARNING: No pk field found automatically - " + this.table + 
                    " only supports insert and by-criteria operations");
                }
                this.pkFields = new String[0];
            }
        } else {
            this.pkFields = (String[]) pkFields.toArray(new String[pkFields.size()]);
        }
        
        if ((this.pkFields.length == 1) && this.autoSetSequenceOnPK) {
            ColumnMetadata pkMapping = this.mappingsByName.get(this.pkFields[0]);
            if (Number.class.isAssignableFrom(pkMapping.getFieldType())) {
                if (this.sqlDialect.supportsAutonumberColumns()) {
                    if (!pkMapping.isAutoNumber()) {
                        pkMapping.setAutoNumber(true);
                        log.debug("Automatically set autonumber property on " + 
                                this.valueobjectClass.getName() + "." + pkMapping.getFieldName());
                    }
                } else {
                    if (pkMapping.getSequenceName() == null) {
                        pkMapping.setSequenceName(this.table + "_" + this.pkFields[0] + "_seq");
                        log.debug("Automatically set sequence name property on " + 
                                this.valueobjectClass.getName() + "." + pkMapping.getFieldName() + 
                                " to " + pkMapping.getSequenceName());
                    }
                }
            }
        }
        
        // where there are autonumber fields with no sequence and we don't support autonumbers, set a derived sequence name
        if (!this.sqlDialect.supportsAutonumberColumns()) {
            for (ColumnMetadata mapping : this.mappings) {
                if (mapping.isAutoNumber() && mapping.getSequenceName() == null) {
                    // set the sequence name if none found
                    mapping.setSequenceName(this.table + "_" + StringUtils.lmcToUnderscore(mapping.getFieldName()) + "_seq");
                }
            }
        }
        
        // Trigger schema auto-creation if required
        if (this.autoCreateSchema) {
            this.writeTemplate.execute(new ConnectionCallback() {
                public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                    ensureTableCreation(con, table);
                    for (ColumnMetadata mapping : mappings) {
                        String sequenceName = mapping.getSequenceName();
                        if (sequenceName != null) {
                            ensureSequenceCreation(con, sequenceName);
                        }
                    }
                    return null;
                }
            });
        }
    }
    
    private void ensureTableCreation(Connection con, String table) throws SQLException, DataAccessException {
        ResultSet rst = null;
        Statement stmTable = null;
        try {
            rst = con.getMetaData().getTables(null, null, table, null);
            if (!rst.next()) {
                String createTableDDL = buildCreateTableDDL();
                log.debug("DDL: " + createTableDDL);
                
                // auto create schema
                stmTable = con.createStatement();
                stmTable.execute(createTableDDL);
            }
        } finally {
            if (rst != null) {
                try {rst.close();} catch (SQLException err) {}
            }
            if (stmTable != null) {
                try {stmTable.close();} catch (SQLException err) {}
            }
        }
    }
    
    private void ensureSequenceCreation(Connection con, String sequence) 
            throws SQLException, DataAccessException {
        try {
            String sql = this.sqlDialect.getNextFromSequenceSQL(sequence);
            log.debug("DDL: " + sql);
            this.writeTemplate.queryForLong(sql); // don't care about the response
        } catch (Throwable err) {
            Statement stmSequence = null;
            try {
                // sequence ? use serial ?
                stmSequence = con.createStatement();
                stmSequence.execute(sqlDialect.createSequenceDDL(sequence, 
                        sequenceInitialValue, sequenceAllocationSize));
            } finally {
                if (stmSequence != null) {
                    try {stmSequence.close();} catch (SQLException err2) {}
                }
            }
        }
    }

    protected String buildCreateTableDDL() {
        StringBuilder sql = new StringBuilder();
        try {
            sql.append("CREATE TABLE ").append(this.table).append(" (");
            boolean first = true;
            for (ColumnMetadata mapping : this.mappings) {
                if (first) {
                    first = false;
                } else {
                    sql.append(", ");
                }
                mapping.writeCreateTableDDL(sql, sqlDialect);
            }
            if (this.pkFields != null && this.pkFields.length > 0) {
                sql.append(", PRIMARY KEY (");
                for (int n = 0; n < this.pkFields.length; n++) {
                    if (n > 0) {
                        sql.append(", ");
                    }
                    this.mappingsByName.get(this.pkFields[n]).writeSelectSQLClause(sql, null, null);
                }
                sql.append(")");
            }
            
            // add some foreign key constraints here ?
            sql.append(");");
        } catch (IOException err) {
            throw new RuntimeException("Error building create table SQL", err);
        }
        return sql.toString();
    }

    protected ColumnMetadata initializeFieldMapping(String fieldName, Class<?> valueobjectClass, String table,
            Map<String,String> hardWiredColumnNames, Collection<String> hardWiredIgnoredColumns) {
        try {
            Method meth = null;
            try {
                String methodName = "get" + StringUtils.upperFirstChar(fieldName);
                meth = valueobjectClass.getMethod(methodName, new Class[0]);
            } catch (NoSuchMethodException err) {
                String methodName = "is" + StringUtils.upperFirstChar(fieldName);
                try {
                    meth = valueobjectClass.getMethod(methodName, new Class[0]);
                    if (!meth.getReturnType().equals(Boolean.class) &&
                            !meth.getReturnType().equals(Boolean.TYPE)) {
                        throw err;
                    }
                } catch (NoSuchMethodException err2) {
                    log.debug("Error looking up method " + methodName, err2);
                    throw err;
                }
            }              
            
            Transient transientAnn = meth.getAnnotation(Transient.class);
            if (transientAnn != null) {
                return null;
            }

            // column name
            String dbName = null;
            boolean insertable = true;
            boolean updateable = true;
            boolean nullable = true;
            boolean isPKField = false;
            int size = 400;
            String sequenceName = null;
            boolean autoNumber = false;
            
            Id id = meth.getAnnotation(Id.class);
            if (id != null) {
                isPKField = true;
            }
        
            Column column = meth.getAnnotation(Column.class);
            if (column != null) {
                dbName = column.name();
                if (!column.table().equals("") && !column.table().equals(table)) {
                    log.warn("WARNING: Ignoring table name on annotation: " + column.table());
                }
                insertable = column.insertable();
                updateable = column.updatable();
                nullable = column.nullable();
                size = column.length();
            }
            if ((dbName == null) || dbName.equals("")) {
                dbName = StringUtils.lmcToUnderscore(fieldName);
            }
            
            GeneratedValue genVal = meth.getAnnotation(GeneratedValue.class);
            if (genVal != null) {
                GenerationType genType = genVal.strategy();
                if (genType == null) {
                    autoNumber = true;
                } else if (genType.equals(GenerationType.SEQUENCE)) {
                    // look up generator
                    String genName = genVal.generator();
                    SequenceGenerator gen = meth.getAnnotation(SequenceGenerator.class);
                    if (gen == null) {
                        gen = valueobjectClass.getAnnotation(SequenceGenerator.class);
                    }
                    if ((gen != null) && ((gen.name() == null) || genName == null ||
                            gen.name().equals(genName)))  {
                        sequenceName = gen.sequenceName();
                    }
                } else if (genType.equals(GenerationType.IDENTITY) ||
                        genType.equals(GenerationType.AUTO)) {
                    autoNumber = true;
                }
            }
            
            if ((hardWiredColumnNames != null) &&
                    hardWiredColumnNames.containsKey(fieldName)) {
                dbName = hardWiredColumnNames.get(fieldName);
            }
            if ((hardWiredIgnoredColumns != null) &&
                    hardWiredIgnoredColumns.contains(fieldName)) {
                insertable = false;
                updateable = false;
                nullable = true;
            }
            
            // field type
            Class<?> fieldType = ReflectionUtils.getAttributeTypeUsingGetter(fieldName, valueobjectClass);
            return new ColumnMetadata(fieldName, fieldType, dbName, 
                    insertable, updateable, nullable, isPKField, size, 
                    sequenceName, autoNumber);
        } catch (NoSuchMethodException err) {
            throw new IllegalArgumentException("Error initializing field: " + fieldName, err);
//        } catch (InvocationTargetException err) {
//            throw new IllegalArgumentException("Error initializing field: " + fieldName, err);
//        } catch (IllegalAccessException err) {
//            throw new IllegalArgumentException("Error initializing field: " + fieldName, err);
        }
    }

    @Override
    protected int doInsert(Iterator<T> valueobjects) {
        if ((valueobjects == null) || !valueobjects.hasNext()) {
            return 0;
        }
        
        Date insertedTimestamp = new Date();
        int rowIndex = 0;
        String lastSQL = null;
        StringBuilder sql = new StringBuilder();
        List<Object> bindArgs = new ArrayList<Object>();
        List<Object[]> bindArgsThisBatch = new ArrayList<Object[]>();
        BatchPreparedStatementSetter bpss = new ReflectingBatchPreparedStatementSetter(bindArgsThisBatch);
        while (valueobjects.hasNext()) {
            T valueobject = valueobjects.next();
            if (valueobject instanceof InsertTimestamped) {
                ((InsertTimestamped) valueobject).markInsertTimestamp(insertedTimestamp);
            }
            
            sql.setLength(0);
            bindArgs.clear();
            sql.append("INSERT INTO ").append(this.table).append(" (");
            boolean first = true;
            for (ColumnMetadata mapping : this.mappings) {
                if (mapping.isInsertable()) {
                    if (!first) {
                        sql.append(", ");
                    } else {
                        first = false;
                    }
                    mapping.writeInsertSQLColumnClause(sql);
                }
            }
            sql.append(") VALUES (");
            first = true;
            for (ColumnMetadata mapping : this.mappings) {
                if (mapping.isInsertable()) {
                    if (!first) {
                        sql.append(", ");
                    } else {
                        first = false;
                    }
                    if (mapping.getSequenceName() != null) {
//                        try {
                            long value = this.writeTemplate.queryForLong(this.sqlDialect.getNextFromSequenceSQL(mapping.getSequenceName()));
                            ReflectionUtils.setAttributeUsingSetter(mapping.getFieldName(), valueobject, value);
//                        } catch (NoSuchMethodException err) {
//                            throw new RuntimeException("Error reading from sequence", err);
//                        }
                    }
                    mapping.writeInsertSQLValuesClause(sql, bindArgs, valueobject);
                }
            }
            sql.append(")");
            if (lastSQL == null) {
                lastSQL = sql.toString();
            } else if (!lastSQL.equals(sql.toString())) {
                log.debug("SQL: " + lastSQL + ", rowCount=" + bindArgsThisBatch.size());
                try {
                    int rowCounts[] = this.writeTemplate.batchUpdate(lastSQL, bpss); 
                    for (int n = 0; n < rowCounts.length; n++) {
                        if (rowCounts[n] != 1) {
                            log.warn("insert() row count on row " + (rowIndex + n) + " was " + 
                                    rowCounts[n] + ", expected 1");
                        }
                    }
                    rowIndex += bindArgsThisBatch.size();
                    bindArgsThisBatch.clear();
                    lastSQL = sql.toString();
                } catch (DataAccessException err) {
                    Throwable nested = err.getRootCause();
                    if ((nested != null) && nested instanceof SQLException) {
                        SQLException next = ((SQLException) nested).getNextException();
                        while (next != null) {
                            log.error("getNextException() returned the following", next);
                            next = next.getNextException();
                        }
                    }
                    throw err;
                }
            }
            bindArgsThisBatch.add(bindArgs.toArray());
        }
        if ((lastSQL != null) && !bindArgsThisBatch.isEmpty()) {
            log.debug("SQL: " + lastSQL + ", rowCount=" + bindArgsThisBatch.size());
            try {
                int rowCounts[] = this.writeTemplate.batchUpdate(lastSQL, bpss); 
                for (int n = 0; n < rowCounts.length; n++) {
                    if (rowCounts[n] != 1) {
                        log.warn("insert() row count on row " + (rowIndex + n) + " was " + 
                                rowCounts[n] + ", expected 1");
                    }
                }
                rowIndex += bindArgsThisBatch.size();
                bindArgsThisBatch.clear();
            } catch (DataAccessException err) {
                Throwable nested = err.getRootCause();
                if ((nested != null) && nested instanceof SQLException) {
                    SQLException next = ((SQLException) nested).getNextException();
                    while (next != null) {
                        log.error("getNextException() returned the following", err);
                        next = next.getNextException();
                    }
                }
                throw err;
            }
        }
        return rowIndex;
    }

    @Override
    protected int doUpdate(Iterator<T> valueobjects) {
        if ((valueobjects == null) || !valueobjects.hasNext()) {
            return 0;
        } else if (this.pkFields == null) {
            throw new RuntimeException("No pkField set, can't update");
        }

        Date updatedTimestamp = new Date();
        int rowIndex = 0;
        int totalRowCountUpdated = 0;
        String lastSQL = null;
        StringBuilder sql = new StringBuilder();
        List<Object> bindArgs = new ArrayList<Object>();
        List<Object[]> bindArgsThisBatch = new ArrayList<Object[]>();
        BatchPreparedStatementSetter bpss = new ReflectingBatchPreparedStatementSetter(bindArgsThisBatch);
        while (valueobjects.hasNext()) {
            T valueobject = valueobjects.next();
            if (valueobject instanceof UpdateTimestamped) {
                ((UpdateTimestamped) valueobject).markUpdateTimestamp(updatedTimestamp);
            }
            
            sql.setLength(0);
            bindArgs.clear();
            try {
                sql.append("UPDATE ").append(this.table).append(" SET ");
                boolean first = true;
                for (ColumnMetadata mapping : this.mappings) {
                    if (mapping.isUpdateable() && !mapping.isPkField()) {
                        if (!first) {
                            sql.append(", ");
                        } else {
                            first = false;
                        }
                        mapping.writeUpdateSQLClause(sql, bindArgs, valueobject);
                    }
                }
                sql.append(" WHERE ");
                addPKWhereClause(sql, bindArgs, null, new PKIterator(new OneItemIterator<T>(valueobject)), 1);
            } catch (IOException err) {
                throw new RuntimeException("Error building update SQL", err);
            }

            if (lastSQL == null) {
                lastSQL = sql.toString();
            } else if (!lastSQL.equals(sql.toString())) {
                log.debug("SQL: " + lastSQL + ", rowCount=" + bindArgsThisBatch.size());
                try {
                    int rowCounts[] = this.writeTemplate.batchUpdate(lastSQL, bpss); 
                    for (int n = 0; n < rowCounts.length; n++) {
                        totalRowCountUpdated += rowCounts[n];
                        if (rowCounts[n] != 1) {
                            log.warn("update() row count on row " + (rowIndex + n) + " was " + 
                                    rowCounts[n] + ", expected 1");
                        }
                    }
                    rowIndex += bindArgsThisBatch.size();
                    bindArgsThisBatch.clear();
                    lastSQL = sql.toString();
                } catch (DataAccessException err) {
                    Throwable nested = err.getRootCause();
                    if ((nested != null) && nested instanceof SQLException) {
                        SQLException next = ((SQLException) nested).getNextException();
                        while (next != null) {
                            log.error("getNextException() returned the following", next);
                            next = next.getNextException();
                        }
                    }
                    throw err;
                }
            }
            bindArgsThisBatch.add(bindArgs.toArray());
        }
        if ((lastSQL != null) && !bindArgsThisBatch.isEmpty()) {
            log.debug("SQL: " + lastSQL + ", rowCount=" + bindArgsThisBatch.size());
            try {
                int rowCounts[] = this.writeTemplate.batchUpdate(lastSQL, bpss); 
                for (int n = 0; n < rowCounts.length; n++) {
                    totalRowCountUpdated += rowCounts[n];
                    if (rowCounts[n] != 1) {
                        log.warn("update() row count on row " + (rowIndex + n) + " was " + 
                                rowCounts[n] + ", expected 1");
                    }
                }
                rowIndex += bindArgsThisBatch.size();
                bindArgsThisBatch.clear();
            } catch (DataAccessException err) {
                Throwable nested = err.getRootCause();
                if ((nested != null) && nested instanceof SQLException) {
                    SQLException next = ((SQLException) nested).getNextException();
                    while (next != null) {
                        log.error("getNextException() returned the following", next);
                        next = next.getNextException();
                    }
                }
                throw err;
            }
        }
        return totalRowCountUpdated;
    }

    @Override
    protected int doDelete(Iterator<T> valueobjects) {
        if ((valueobjects == null) || !valueobjects.hasNext()) {
            return 0;
        } else if (this.pkFields == null) {
            throw new RuntimeException("No pkField set, can't delete");
        }
        List<Object> bindArgs = new ArrayList<Object>();
        int deleted = 0;
        StringBuilder sql = new StringBuilder();
        while (valueobjects.hasNext()) {
            sql.setLength(0);
            try {
                if (this.deleteTimestampField != null) {
                    sql.append("UPDATE ").append(this.table).append(" SET ");
                    // not strictly correct, but the sql is the same
                    this.mappingsByName.get(this.deleteTimestampField).writeWhereSQLClause(sql, bindArgs, null, new Date());
                } else {
                    sql.append("DELETE FROM ").append(this.table);
                }
                sql.append(" WHERE ");
                addPKWhereClause(sql, bindArgs, null, new PKIterator(valueobjects), this.batchSizeSelectByPK);
            } catch (IOException err) {
                throw new RuntimeException("Error building delete SQL", err);
            } 
            long start = System.currentTimeMillis();
            int thisRowCount = this.writeTemplate.update(sql.toString(), bindArgs.toArray());
            log.debug("Deleted " + thisRowCount + " rows in " + (System.currentTimeMillis() - start)  + "ms");
            deleted += thisRowCount;
        }
        return deleted;
    }
    
    public void appendDeletePrefix(StringBuilder sql, List<Object> bindArgs) throws IOException  {
        if (this.deleteTimestampField != null) {
            sql.append("UPDATE ").append(this.table).append(" SET ");
            // not strictly correct, but the sql is the same
            this.mappingsByName.get(this.deleteTimestampField).writeWhereSQLClause(sql, bindArgs, null, new Date());
        } else {
            sql.append("DELETE FROM ").append(this.table);
        }
    }
    
    public String getTableName() {
        return table;
    }

    protected void addPKWhereClause(StringBuilder sql, List<Object> bindArgs, String alias, Iterator<?> pks, int maxPKs) throws IOException { if (!pks.hasNext()) {
            sql.append("1 = 0");
            log.info("No pks supplied in iterator");
        } else if (this.pkFields.length == 1) {
            this.mappingsByName.get(this.pkFields[0]).writeWhereSQLClauseMulti(sql, bindArgs, pks, maxPKs);
        } else {
            int readSoFar = 0;
            while (pks.hasNext() && ((maxPKs < 0) || (readSoFar < maxPKs))) {
                if (readSoFar > 0) {
                    sql.append(" OR ");
                }
                readSoFar++;
                sql.append("(");
                Object pk = pks.next(); 
                for (int n = 0; n < this.pkFields.length; n++) {
                    if (n > 0) {
                        sql.append(" AND ");
                    }
                    Object pkItem = null;
                    if (pk == null) {
                        pkItem = null;
                    } else if (pk.getClass().isArray()) {
                        pkItem = Array.get(pk, n);
                    } else if (n == 0) {
                        pkItem = pk;
                    } else {
                        pkItem = null;
                    }
                    this.mappingsByName.get(this.pkFields[n]).writeWhereSQLClause(sql, bindArgs, alias, pkItem);
                }
                sql.append(")");
            }
        }
    }
//    
//    public String getMappedColumnName(String tableAlias, String fieldName) {
//        ColumnMetadata mapped = this.mappingsByName.get(fieldName);
//        if (mapped != null) {
//            return mapped.makeColumnAlias(tableAlias);
//        } else {
//            return null;
//        }
//    }
//    
//    public String getMappedTableDotColumnName(String tableAlias, String fieldName) {
//        ColumnMetadata mapped = this.mappingsByName.get(fieldName);
//        if (mapped != null) {
//            return mapped.makeTableDotColumnName(tableAlias);
//        } else {
//            return null;
//        }
//    }
    
    public ColumnMetadata getMappedColumn(String fieldName) {
        return this.mappingsByName.get(fieldName);
    }
    
    public ColumnMetadata[] getAllColumnMetadata() {
        return this.mappings;
    }

    @Override
    public SelectSQL<T> select() {
        return new ReflectingDAOSelectSQL<T>(this);
    }
    
    public void setDataSource(DataSource dataSource) {
        this.readTemplate = new JdbcTemplate(dataSource);
        this.writeTemplate = new JdbcTemplate(dataSource);
    }
    
    public void setReadDataSource(DataSource dataSource) {
        this.readTemplate = new JdbcTemplate(dataSource);
    }
    
    public void setWriteDataSource(DataSource dataSource) {
        this.writeTemplate = new JdbcTemplate(dataSource);
    }
    
    public void setTemplate(JdbcTemplate template) {
        this.readTemplate = template;
        this.writeTemplate = template;
    }
    
    public void setReadTemplate(JdbcTemplate template) {
        this.readTemplate = template;
    }
    
    public void setWriteTemplate(JdbcTemplate template) {
        this.writeTemplate = template;
    }
    
    public JdbcTemplate getReadTemplate() {
        return readTemplate;
    }

    public JdbcTemplate getWriteTemplate() {
        return writeTemplate;
    }

    public void setSuppressNoPkWarning(boolean suppressNoPkWarning) {
        this.suppressNoPkWarning = suppressNoPkWarning;
    }

    class ReflectingBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
        private List<Object[]> argsList;
        
        ReflectingBatchPreparedStatementSetter(List<Object[]> argsList) {
            this.argsList = argsList;
        }
        public int getBatchSize() {
            return argsList.size();
        }
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            Object[] values = argsList.get(i);
            for (int n = 0; n < values.length; n++) {
                Object value = values[n];
                if (value instanceof SqlParameterValue) {
                    SqlParameterValue paramValue = (SqlParameterValue) value;
                    StatementCreatorUtils.setParameterValue(ps, n + 1, paramValue, paramValue.getValue());
                } else {
                    StatementCreatorUtils.setParameterValue(ps, n + 1, SqlTypeValue.TYPE_UNKNOWN, value);
                }
            }
        }
    }
    
    class PKIterator implements Iterator<Object> {
        Iterator<T> source;
        PKIterator(Iterator<T> source) {
            this.source = source;
        }
        @Override
        public boolean hasNext() {
            return this.source.hasNext();
        }
        @Override
        public Object next() {
            T item = source.next();
            return item == null ? null : getPKFromValueObject(item);
        }
        @Override
        public void remove() {
        }
    }
    
    protected Object getPKFromValueObject(T valueobject) {
//        try {
            if (pkFields == null) {
                throw new RuntimeException("No PK field set");
            } else if (pkFields.length == 1) {
                return ReflectionUtils.getAttributeUsingGetter(pkFields[0], valueobject);
            } else {
                Object pks[] = new Object[pkFields.length]; 
                for (int n = 0; n < pkFields.length; n++) {
                   pks[n] = ReflectionUtils.getAttributeUsingGetter(pkFields[n], valueobject);
                }
                return pks;
            }
//        } catch (NoSuchMethodException err) {
//            throw new RuntimeException("PK field " + Arrays.asList(pkFields) + 
//                    " not found on " + valueobject, err);
//        }
    }
    
    public ParameterizedRowMapper<T> getRowMapper(String alias) {
        return new ReflectingRowMapper<T>(this.mappings, alias, this.valueobjectClass);
    }
    
    public AliasDotColumnResolver getResolver(final String tableAlias) {
        return new AliasDotColumnResolver() {
            @Override
            public String getDefaultAlias() {
                return tableAlias;
            }

            @Override
            public ResolvedColumnMetadata resolve(String conditionFieldName, String conditionAlias) {
                if (tableAlias == null || conditionAlias == null || conditionAlias.equals(tableAlias)) {
                    final ColumnMetadata cm = getMappedColumn(conditionFieldName);
                    if (cm != null) {
                        return new ResolvedColumnMetadata() {
                            public Object convertToDBValue(Object in) {
                                return cm.convertToDBValue(in);
                            }
                            public void writeSQLExpression(StringBuilder sql, List<Object> bindArgs, 
                                    AliasDotColumnResolver resolver) throws IOException {
                                sql.append(cm.makeTableDotColumnName(tableAlias));
                            }
                        };
                    }
                }
                return null;
            }   
        };
    }
}
