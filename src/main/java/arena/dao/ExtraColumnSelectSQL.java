package arena.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public abstract class ExtraColumnSelectSQL<X,T> extends ConditionsSelectSQLSupport<T> implements SQLStatementParts {
    private final Log log = LogFactory.getLog(ExtraColumnSelectSQL.class);
    
//    protected ReflectingDAO<X> dao;
    protected SQLStatementParts selectSQL;
    protected AliasDotColumnResolver parentResolver;
    protected ExtraColumn[] columns;
    protected String mainAlias = "main";
    
    public ExtraColumnSelectSQL(SQLStatementParts selectSQL, AliasDotColumnResolver parentResolver, ExtraColumn[] columns) {
        super();
        this.selectSQL = selectSQL;
        this.parentResolver = parentResolver;
        this.columns = columns;
    }

    protected ExtraColumnSelectSQL(SQLStatementParts selectSQL, AliasDotColumnResolver parentResolver, ExtraColumn[] columns, WhereExpression[] wheres, OrderBy[] orderBys, int offset, int limit) {
        super(wheres, orderBys, offset, limit);
        this.selectSQL = selectSQL;
        this.parentResolver = parentResolver;
        this.columns = columns;
    }

    public void setMainAlias(String mainAlias) {
        this.mainAlias = mainAlias;
    }

    @Override
    public int rowCount() {
        AliasDotColumnResolver resolver = getResolver(this.mainAlias, this.parentResolver);
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(1) AS rowcountval FROM (");
        DAOUtils.buildSelectSQL(this, sql, bindArgs, resolver, true, false, false);
        sql.append(") countView");
        log.info("Executing SQL: " + sql.toString());
        for (int n = 0; n < bindArgs.size(); n++) {
            log.info("Setting query param: n=" + (n+1) + ", value=" + bindArgs.get(n));
        }
        return getJdbcTemplate().queryForInt(sql.toString(), bindArgs.toArray());
    }

    @Override
    public void consume(SelectSQLConsumer<T> consumer) {        
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        AliasDotColumnResolver resolver = getResolver(this.mainAlias, this.parentResolver);
        DAOUtils.buildSelectSQL(this, sql, bindArgs, resolver, true, true, true);
        
        DAOUtils.consume(sql.toString(), bindArgs, consumer, getJdbcTemplate(), getRowMapper(null), log);
    }
    
    public boolean appendSelectClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean out = this.selectSQL.appendSelectClause(sql, bindArgs, resolver);
        for (ExtraColumn column : this.columns) {
            if (out) {
                sql.append(", ");
            }
            column.writeSQLExpression(sql, bindArgs, resolver);
            sql.append(" AS ");
            sql.append(column.getAlias());
            out = true;
        }
        return out;
    }

    public boolean appendFromClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        return this.selectSQL.appendFromClause(sql, bindArgs, resolver);
    }

    public boolean appendGroupByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean out = this.selectSQL.appendGroupByClause(sql, bindArgs, resolver);
        for (ExtraColumn column : this.columns) {
            if (!column.isAggregate()) {
                if (out) {
                    sql.append(", ");
                }
                sql.append(column.getAlias());
                out = true;
            }
        }
        return out;
    }

    public boolean appendWhereClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        return this.selectSQL.appendWhereClause(sql, bindArgs, resolver);
    }

    public boolean appendOrderByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean localClausesAdded = false;
        if (this.orderBys != null && this.orderBys.length > 0) {
            localClausesAdded = appendLocalOrderByClause(sql, bindArgs, resolver);
        }
        if (localClausesAdded) {
            sql.append(", ");
        }
        boolean childClausesAdded = this.selectSQL.appendOrderByClause(sql, bindArgs, resolver);

        if (childClausesAdded || localClausesAdded) {
            sql.setLength(sql.length() - 2);
            return true;
        } else {
            return false;
        }
    }
    
    protected AliasDotColumnResolver getResolver(final String tableAlias, final AliasDotColumnResolver parentResolver) {
        return new AliasDotColumnResolver() {
//            private AliasDotColumnResolver parent = selectSQL.getResolver(tableAlias);

            public String getDefaultAlias() {return tableAlias;}

            public ResolvedColumnMetadata resolve(String conditionFieldName, String conditionAlias) {
                if (conditionAlias == null || tableAlias == null || conditionAlias.equals(tableAlias)) {
                    for (ExtraColumn col : columns) {
                        if (col.getAlias().equals(conditionFieldName)) {
                            return col;
                        }
                    }
                }
                return parentResolver.resolve(conditionFieldName, conditionAlias == null ? tableAlias : conditionAlias);
            }
        };
    }

    /**
     * Have to override this: defines how to read the columns out into an object
     * @param alias
     * @return
     */
    protected abstract JdbcTemplate getJdbcTemplate();
    protected abstract ParameterizedRowMapper<T> getRowMapper(final String alias) ;

    public interface ExtraColumn extends ResolvedColumnMetadata {
        public String getAlias();
        public boolean isAggregate();
    }
    
}
