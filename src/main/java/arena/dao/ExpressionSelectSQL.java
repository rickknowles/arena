package arena.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class ExpressionSelectSQL<T> extends ConditionsSelectSQLSupport<T> implements SQLStatementParts {
    private final Log log = LogFactory.getLog(ExpressionSelectSQL.class);

    private ReflectingDAOSelectSQL<T> proxiedSelectSQL;
    private ExpressionColumn[] columns;
    
    public ExpressionSelectSQL(ReflectingDAOSelectSQL<T> proxiedSelectSQL) {
        super();
        this.proxiedSelectSQL = proxiedSelectSQL;
    }
        
    protected ExpressionSelectSQL(ExpressionSelectSQL<T> selectSQL, ReflectingDAOSelectSQL<T> proxiedSelectSQL, WhereExpression[] wheres, OrderBy[] orderBys, int offset, int limit) {
        super(wheres, orderBys, offset, limit);
        this.proxiedSelectSQL = selectSQL.proxiedSelectSQL;
        this.columns = selectSQL.columns;
    }
    
    @Override
    protected SelectSQL<T> makeNew(WhereExpression[] wheres, OrderBy[] orderBys, int offset, int limit) {
        return new ExpressionSelectSQL<T>(this, this.proxiedSelectSQL, wheres, orderBys, offset, limit);
    }

    @Override
    public void consume(SelectSQLConsumer<T> consumer) {
        List<Object> bindArgs = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        AliasDotColumnResolver resolver = proxiedSelectSQL.getResolver(null);
        DAOUtils.buildSelectSQL(this, sql, bindArgs, resolver, true, true, true);
        DAOUtils.consume(sql.toString(), bindArgs, consumer, proxiedSelectSQL.dao.getReadTemplate(), new ParameterizedRowMapper<T>() {
            public T mapRow(ResultSet rst, int rowNum) throws SQLException {
                return null;
            }
        }, log);
    }

    public boolean appendSelectClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean modified = this.proxiedSelectSQL.appendSelectClause(sql, bindArgs, resolver);
        for (ExpressionColumn column : this.columns) {
            if (modified) {
                sql.append(",");
            }
            column.appendSQLExpression(sql, bindArgs);
            sql.append(" AS ").append(column.getFieldAlias());
            modified = true;
        }
        return modified;
    }

    public boolean appendFromClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        return this.proxiedSelectSQL.appendFromClause(sql, bindArgs, resolver);
    }

    public boolean appendWhereClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean modified = this.proxiedSelectSQL.appendWhereClause(sql, bindArgs, resolver);
        return modified;
    }

    public boolean appendGroupByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean modified = this.proxiedSelectSQL.appendGroupByClause(sql, bindArgs, resolver);
        return modified;
    }

    public boolean appendOrderByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        boolean modified = this.proxiedSelectSQL.appendOrderByClause(sql, bindArgs, resolver);
        return modified;
    }

    @Override
    public SelectSQL<T> orderBy(OrderBy orderBy) {
        for (ExpressionColumn column : this.columns) {
            if (orderBy.getField().equals(column.getFieldAlias())) {
                return super.orderBy(orderBy);
            }
        }
        return new ExpressionSelectSQL<T>(this, (ReflectingDAOSelectSQL<T>) proxiedSelectSQL.orderBy(orderBy), wheres, orderBys, offset, limit);
    }
//
//    @Override
//    public SelectSQL<T> where(WhereExpression where) {
//        for (ExpressionColumn column : this.columns) {
//            if (where.().equals(column.getFieldAlias())) {
//                return super.where(where);
//            }
//        }
//        return new ExpressionSelectSQL<T>(this, (ReflectingDAOSelectSQL<T>) proxiedSelectSQL.orderBy(orderBy), wheres, orderBys, offset, limit);
//    }
}
