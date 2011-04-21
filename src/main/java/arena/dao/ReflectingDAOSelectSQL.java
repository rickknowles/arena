package arena.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import arena.dao.where.And;


public class ReflectingDAOSelectSQL<T> extends ConditionsSelectSQLSupport<T> implements SQLStatementParts  {
    private final Log log = LogFactory.getLog(ReflectingDAOSelectSQL.class);

    protected ReflectingDAO<T> dao;
    
    private ColumnMetadata[] selectColumns;
    
    public ReflectingDAOSelectSQL(ReflectingDAO<T> dao) {
        super();
        this.dao = dao;
        this.selectColumns = this.dao.getAllColumnMetadata();
    }
    
    protected ReflectingDAOSelectSQL(ReflectingDAOSelectSQL<T> selectSQL, WhereExpression[] wheres, OrderBy[] orderBys, int offset, int limit) {
        super(wheres, orderBys, offset, limit);
        this.dao = selectSQL.dao;
        this.selectColumns = selectSQL.selectColumns;
    }
    
    @Override
    public void consume(SelectSQLConsumer<T> consumer) {
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        buildSelectSQL(sql, bindArgs, false, true, true);
        DAOUtils.consume(sql.toString(), bindArgs, consumer, dao.getReadTemplate(), dao.getRowMapper(null), log);
    }
    
    @Override
    public boolean isEmpty() {
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT 1 AS found WHERE EXISTS (");
        buildSelectSQL(sql, bindArgs, true, false, false);
        sql.append(")");
        log.info("Executing SQL: " + sql.toString());
        for (int n = 0; n < bindArgs.size(); n++) {
            log.info("Setting query param: n=" + (n+1) + ", value=" + bindArgs.get(n));
        }
        return (Boolean) this.dao.getReadTemplate().query(sql.toString(), bindArgs.toArray(), new ResultSetExtractor() {
            public Object extractData(ResultSet rst) throws SQLException, DataAccessException {
                return !rst.next();
            }
        });
    }

    @Override
    public int rowCount() {
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(1) AS rowcountval FROM (");
        buildSelectSQL(sql, bindArgs, true, false, false);
        sql.append(") countView");
        log.info("Executing SQL: " + sql.toString());
        for (int n = 0; n < bindArgs.size(); n++) {
            log.info("Setting query param: n=" + (n+1) + ", value=" + bindArgs.get(n));
        }
        return this.dao.getReadTemplate().queryForInt(sql.toString(), bindArgs.toArray());
    }

    @Override
    public int delete() {
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        try {
            this.dao.appendDeletePrefix(sql, bindArgs);
            int preLength = sql.length();
            sql.append(" WHERE ");
            if (!appendWhereClause(sql, bindArgs, getResolver(null))) {
                sql.setLength(preLength);
            }
        } catch (IOException err) {
            throw new RuntimeException("Error building delete SQL", err);
        }
        return this.dao.getWriteTemplate().update(sql.toString(), bindArgs.toArray());
    }

    protected void buildSelectSQL(Appendable sql, List<Object> bindArgs, boolean includeGroupBy, boolean includeOrderBy, boolean includeLimitOffset) {
        AliasDotColumnResolver resolver = getResolver(null);
        DAOUtils.buildSelectSQL(this, sql, bindArgs, resolver, includeGroupBy, includeOrderBy, includeLimitOffset);
        
    }

    public boolean appendSelectClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean first = true;
        for (ColumnMetadata column : this.selectColumns) {
            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }
            column.writeSelectSQLClause(sql, bindArgs, resolver.getDefaultAlias());
        }
        return !first;
    }

    public boolean appendFromClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        sql.append(this.dao.getTableName());
        String mainTableAlias = resolver.getDefaultAlias();
        if (mainTableAlias != null) {
            sql.append(" AS ").append(mainTableAlias);
        }
        return true;
    }

    public boolean appendGroupByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean first = true;
        for (ColumnMetadata column : this.selectColumns) {
            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }
            column.writeSelectSQLClause(sql, bindArgs, resolver.getDefaultAlias());
        }
        return !first;
    }

    public boolean appendWhereClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        if (this.wheres != null && this.wheres.length > 0) {
            And and = new And(false, this.wheres);
            return and.writeSQL(sql, resolver, bindArgs);
        }
        return false;
    }

    public boolean appendOrderByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        return DAOUtils.appendOrderByClause(sql, bindArgs, resolver, this.orderBys);
    }

    @Override
    public SelectSQL<T> innerJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions) {
        return innerJoin(joinToQuery, getRightAlias(joinToQuery), conditions);
    }

    @Override
    public SelectSQL<T> leftJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions) {
        return leftJoin(joinToQuery, getRightAlias(joinToQuery), conditions);
    }

    @Override
    public SelectSQL<T> rightJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions) {
        return rightJoin(joinToQuery, getRightAlias(joinToQuery), conditions);
    }

    @Override
    public SelectSQL<T> innerJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions) {
        return new Join2FilterSelectSQL<T>(this, joinToQuery, rightAlias, "INNER JOIN", conditions);
    }

    @Override
    public SelectSQL<T> leftJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions) {
        return new Join2FilterSelectSQL<T>(this, joinToQuery, rightAlias, "LEFT JOIN", conditions);
    }

    @Override
    public SelectSQL<T> rightJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions) {
        return new Join2FilterSelectSQL<T>(this, joinToQuery, rightAlias, "RIGHT JOIN", conditions);
    }
    
    protected String getRightAlias(SelectSQL<?> joinToQuery) {
        if (joinToQuery instanceof ReflectingDAOSelectSQL<?>) {
            return ((ReflectingDAOSelectSQL<?>) joinToQuery).dao.getTableName();
        }
        return null;
    }
    
    @Override
    public SelectSQL<T> getNested() {
        return null;
    }

    protected String getTableAlias(String suppliedAlias) {
        return suppliedAlias;
    }
    
    @Override
    protected SelectSQL<T> makeNew(WhereExpression[] wheres, OrderBy[] orderBys, int offset, int limit) {
        return new ReflectingDAOSelectSQL<T>(this, 
                wheres, orderBys, offset, limit);
    }
    
    public AliasDotColumnResolver getResolver(final String tableAlias) {
        return dao.getResolver(tableAlias);
//        return new AliasDotColumnResolver() {
//            
//            @Override
//            public String getDefaultAlias() {
//                return tableAlias;
//            }
//
//            @Override
//            public ResolvedColumnMetadata resolve(String conditionFieldName, String conditionAlias) {
//                if (tableAlias == null || conditionAlias == null || conditionAlias.equals(tableAlias)) {
//                    final ColumnMetadata cm = dao.getMappedColumn(conditionFieldName);
//                    if (cm != null) {
//                        return new ResolvedColumnMetadata() {
//                            public ColumnMetadata getColumnMetadata() {return cm;}
//                            public void writeAliasDotColumn(StringBuilder sql) throws IOException {
//                                sql.append(cm.makeTableDotColumnName(tableAlias));
//                            }
//                        };
//                    }
//                }
//                return null;
//            }   
//        };
    }
}
