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

public class Join2FilterSelectSQL<T> extends ConditionsSelectSQLSupport<T> implements SQLStatementParts {
    private final Log log = LogFactory.getLog(Join2FilterSelectSQL.class);

    private SelectSQL<T> nested;
    private ReflectingDAOSelectSQL<?> joinedFilterQuery;
    private JoinCondition[] conditions;
    private String joinType;
    private String rightAlias; // null if this is a joined query itself, reserved for specific queries
    private AliasDotColumnResolver rightResolver;

    public Join2FilterSelectSQL(SelectSQL<T> nested, SelectSQL<?> joinedFilterQuery, 
            String rightAlias, String joinType, JoinCondition... conditions) {
        super();
        
        // refactor right side ? 
        if (!(joinedFilterQuery instanceof ReflectingDAOSelectSQL<?>)) {
            throw new IllegalArgumentException("Only support reflecting dao on the right at the moment. Refactor of joins not complete");
        }
        
        this.nested = nested;
        this.joinedFilterQuery = (ReflectingDAOSelectSQL<?>) joinedFilterQuery;
        this.conditions = conditions;
        this.joinType = joinType;
        this.rightAlias = rightAlias;
        if (this.rightAlias == null) {
            this.rightAlias = this.joinedFilterQuery.dao.getTableName();
        }
        this.rightResolver = this.joinedFilterQuery.getResolver(this.rightAlias);
    }
    
    protected Join2FilterSelectSQL(Join2FilterSelectSQL<T> old, WhereExpression[] wheres, 
            OrderBy[] orderBys, int offset, int limit) {
        super(wheres, orderBys, offset, limit);

        this.nested = old.nested;
        this.joinedFilterQuery = old.joinedFilterQuery;
        this.conditions = old.conditions;
        this.joinType = old.joinType;
        this.rightAlias = old.rightAlias;
        this.rightResolver = old.rightResolver;
    }
    
    @Override
    public void consume(SelectSQLConsumer<T> consumer) {
        ReflectingDAOSelectSQL<T> nestedCenter = getNestedCenter();
        String nestedCenterAlias = getRightAlias(nestedCenter);
        
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        try {
            buildSelectSQL(sql, bindArgs, nestedCenterAlias, true, true, true);
        } catch (IOException err) {
            throw new RuntimeException("Error building select SQL", err);
        }
        DAOUtils.consume(sql.toString(), bindArgs, consumer, nestedCenter.dao.getReadTemplate(), nestedCenter.dao.getRowMapper(null), log);
    }

    @Override
    public SelectSQL<T> getNested() {
        return this.nested;
    }

    @Override
    public int delete() {
        throw new RuntimeException("delete() not supported on joined tables");
    }   
    
    @Override
    protected SelectSQL<T> makeNew(WhereExpression[] wheres, OrderBy[] orderBys, int offset, int limit) {
        return new Join2FilterSelectSQL<T>(this, wheres, orderBys, offset, limit);
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
    public boolean isEmpty() {
        ReflectingDAOSelectSQL<T> nestedCenter = getNestedCenter();
        String nestedCenterAlias = getRightAlias(nestedCenter);
        
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        try {
            sql.append("SELECT 1 AS found WHERE EXISTS (");
            buildSelectSQL(sql, bindArgs, nestedCenterAlias, false, true, false);
            sql.append(")");
        } catch (IOException err) {
            throw new RuntimeException("Error building row count SQL", err);
        }
        log.info("Executing SQL: " + sql.toString());
        for (int n = 0; n < bindArgs.size(); n++) {
            log.info("Setting query param: n=" + (n+1) + ", value=" + bindArgs.get(n));
        }
        return (Boolean) nestedCenter.dao.getReadTemplate().query(sql.toString(), bindArgs.toArray(), new ResultSetExtractor() {
            public Object extractData(ResultSet rst) throws SQLException, DataAccessException {
                return !rst.next();
            }
        });
    }
    
    @Override
    public int rowCount() {
        ReflectingDAOSelectSQL<T> nestedCenter = getNestedCenter();
        String nestedCenterAlias = getRightAlias(nestedCenter);
        
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        try {
            sql.append("SELECT count(1) AS rowcountval FROM (");
            buildSelectSQL(sql, bindArgs, nestedCenterAlias, false, true, false);
            sql.append(") countView");
        } catch (IOException err) {
            throw new RuntimeException("Error building row count SQL", err);
        }
        log.info("Executing SQL: " + sql.toString());
        for (int n = 0; n < bindArgs.size(); n++) {
            log.info("Setting query param: n=" + (n+1) + ", value=" + bindArgs.get(n));
        }
        return nestedCenter.dao.getReadTemplate().queryForInt(sql.toString(), bindArgs.toArray());
    }

    protected void buildSelectSQL(StringBuilder sql, List<Object> bindArgs, String nestedCenterAlias,
            boolean includeGroupBy, boolean includeOrderBy, boolean includeLimitOffset) throws IOException {
        AliasDotColumnResolver resolver = getResolver(nestedCenterAlias);
        
        StringBuilder mca = new StringBuilder();
        mca.append("SELECT ");
        appendSelectClause(mca, bindArgs, resolver);
        mca.append(" FROM ");
        appendFromClause(mca, bindArgs, resolver);
        mca.append(" WHERE ");
        int preLength = mca.length();
        if (!appendWhereClause(mca, bindArgs, resolver)) {
            mca.setLength(preLength - 7);
        }
        if (includeGroupBy) {
            mca.append(" GROUP BY ");
            preLength = mca.length();
            if (!appendGroupByClause(mca, bindArgs, resolver)) {
                mca.setLength(preLength - 10);
            }
        }
        if (includeOrderBy) {
            mca.append(" ORDER BY ");
            preLength = mca.length();
            if (!appendOrderByClause(mca, bindArgs, resolver)) {
                mca.setLength(preLength - 10);
            }
        }
        appendLimitOffsetClause(mca, bindArgs);
        sql.append(mca);
    }
    
    @Override
    public boolean appendSelectClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        ReflectingDAOSelectSQL<T> nestedCenter = getNestedCenter();
        return nestedCenter.appendSelectClause(sql, bindArgs, resolver);
    }

    public boolean appendFromClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        // Left side of the join
        if (this.nested instanceof Join2FilterSelectSQL<?>) {
            ((Join2FilterSelectSQL<T>) this.nested).appendFromClause(sql, bindArgs, resolver);
        } else {
            ReflectingDAOSelectSQL<T> nestedCenter = (ReflectingDAOSelectSQL<T>) this.nested;
            nestedCenter.appendFromClause(sql, bindArgs, resolver);
        }
        
        // Middle and right sides of join
        sql.append(" ").append(this.joinType).append(" ");
        sql.append(this.joinedFilterQuery.dao.getTableName()).append(" AS ").append(this.rightAlias);

        // add in join on clause
        boolean first = true;
        for (JoinCondition condition : this.conditions) {
            if (first) {
                sql.append(" ON ");
                first = false;
            } else {
                sql.append(" AND ");
            }
            String extraLeftAlias = condition.getLeftAlias();
            if (extraLeftAlias == null) {
                extraLeftAlias = resolver.getDefaultAlias();
            }
            
            ResolvedColumnMetadata leftRcm = resolver.resolve(condition.getLeftField(), extraLeftAlias);
            if (leftRcm != null) {
                leftRcm.writeSQLExpression(sql, bindArgs, resolver);
            } else {
                throw new RuntimeException("Couldn't find join condition field: " + extraLeftAlias + "." + condition.getLeftField());
            }
            
            sql.append(" = ");
            
            ResolvedColumnMetadata rightRcm = resolver.resolve(condition.getRightField(), this.rightAlias);
            if (rightRcm != null) {
                rightRcm.writeSQLExpression(sql, bindArgs, resolver);
            } else {
                throw new RuntimeException("Couldn't find join condition field: " + this.rightAlias + "." + condition.getRightField());
            }
        }
        return true;
    }
    
    @Override
    public boolean appendWhereClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean localClausesAdded = false;
        if (this.wheres != null && this.wheres.length > 0) {
            localClausesAdded = new And(false, this.wheres).writeSQL(sql, resolver, bindArgs);
        }
        if (localClausesAdded) {
            sql.append(" AND ");
        }

        boolean nestedClausesAdded = false;
        if (this.nested instanceof Join2FilterSelectSQL<?>) {
            nestedClausesAdded = ((Join2FilterSelectSQL<T>) this.nested).appendWhereClause(sql, bindArgs, resolver);
        } else {
            nestedClausesAdded = ((ReflectingDAOSelectSQL<T>) this.nested).appendWhereClause(sql, bindArgs, resolver);
        }
        if (nestedClausesAdded) {
            sql.append(" AND ");
        }
        boolean rightClausesAdded = this.joinedFilterQuery.appendWhereClause(sql, bindArgs, rightResolver);
        if (!rightClausesAdded && (nestedClausesAdded || localClausesAdded)) {
            sql.setLength(sql.length() - 5);
        }
        return (localClausesAdded || nestedClausesAdded || rightClausesAdded);
    }
    
    public boolean appendGroupByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        ReflectingDAOSelectSQL<T> nestedCenter = getNestedCenter();
        return nestedCenter.appendGroupByClause(sql, bindArgs, resolver);
    }

    public boolean appendOrderByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean localClausesAdded = false;
        if (this.orderBys != null && this.orderBys.length > 0) {
            localClausesAdded = appendLocalOrderByClause(sql, bindArgs, resolver);
        }
        if (localClausesAdded) {
            sql.append(", ");
        }

        boolean nestedClausesAdded = false;
        if (this.nested instanceof Join2FilterSelectSQL<?>) {
            nestedClausesAdded = ((Join2FilterSelectSQL<T>) this.nested).appendOrderByClause(sql, bindArgs, resolver);
        } else {
            nestedClausesAdded = ((ReflectingDAOSelectSQL<T>) this.nested).appendOrderByClause(sql, bindArgs, resolver);
        }
        if (nestedClausesAdded) {
            sql.append(", ");
        }

        boolean rightClausesAdded = this.joinedFilterQuery.appendOrderByClause(sql, bindArgs, rightResolver);
        if (!rightClausesAdded && (nestedClausesAdded || localClausesAdded)) {
            sql.setLength(sql.length() - 2);
        }
        return (localClausesAdded || nestedClausesAdded || rightClausesAdded);
    }        
        
//    protected boolean appendLocalOrderByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
//        if (this.orderBys != null && this.orderBys.length > 0) {
//            boolean hasOneClause = false;
//            
//            for (OrderBy orderBy : this.orderBys) {
//                if (orderBy != null) {
//                    String thisAlias = orderBy.getAlias();
//                    if (thisAlias == null) {
//                        thisAlias = resolver.getDefaultAlias();
//                    }
//                    ResolvedColumnMetadata rcm = resolver.resolve(orderBy.getField(), thisAlias);
//                    if (rcm == null) {
//                        throw new RuntimeException("Can't resolve field name " + thisAlias + "." + orderBy.getField());
//                    } else {
//                        hasOneClause = true;
//                        rcm.writeSQLExpression(sql, bindArgs, resolver);
//                        sql.append(orderBy.isAsc() ? " ASC" : " DESC").append(", ");
//                    }
//                }
//            }
//            if (hasOneClause) {
//                sql.setLength(sql.length() - 2);
//                return true;
//            }
//        }
//        return false;
//    }
    
    protected ReflectingDAOSelectSQL<T> getNestedCenter() {
        SelectSQL<T> parent = this;
        while (parent.getNested() != null) {
            parent = parent.getNested();
        }
        return ((ReflectingDAOSelectSQL<T>) parent);
    }

    public AliasDotColumnResolver getResolver(final String nestedCenterAlias) {
        final AliasDotColumnResolver nestedResolver;
        if (nested instanceof Join2FilterSelectSQL<?>) {
            nestedResolver = ((Join2FilterSelectSQL<T>)nested).getResolver(nestedCenterAlias);
        } else {
            nestedResolver = ((ReflectingDAOSelectSQL<T>) nested).getResolver(nestedCenterAlias);
        }
        
        // Return an object that recurses to the root, favoring the right branch
        return new AliasDotColumnResolver() {
            
            @Override
            public String getDefaultAlias() {
                return nestedCenterAlias;
            }

            @Override
            public ResolvedColumnMetadata resolve(String conditionFieldName, String conditionAlias) {
                if (conditionAlias == null) {
                    conditionAlias = nestedCenterAlias;
                }
                if (rightAlias.equals(conditionAlias)) {
                    return rightResolver.resolve(conditionFieldName, conditionAlias);
                } else {
                    return nestedResolver.resolve(conditionFieldName, conditionAlias);
                }
            }   
        };        
    }
}
