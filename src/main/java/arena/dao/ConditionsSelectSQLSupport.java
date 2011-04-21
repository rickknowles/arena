package arena.dao;

import java.io.IOException;
import java.util.List;

import arena.dao.where.Where;

/**
 * Support class that implements many of the helper methods for adding where/order-by clauses
 * 
 * @author Rick Knowles
 * @version $Id$
 */
public abstract class ConditionsSelectSQLSupport<T> extends SelectSQLSupport<T> {

    protected WhereExpression[] wheres;
    protected OrderBy[] orderBys;
    protected int offset = 0;
    protected int limit = -1;
    
    protected ConditionsSelectSQLSupport() {}
    
    protected ConditionsSelectSQLSupport(WhereExpression[] wheres, OrderBy[] orderBys, int offset, int limit) {
        this();
        this.wheres = wheres;
        this.orderBys = orderBys;
        this.offset = offset;
        this.limit = limit;
    }

    @Override
    public SelectSQL<T> where(WhereExpression where) {
        return makeNew(Where.appendToArray(wheres, where), orderBys, offset, limit);
    }
    
    @Override
    public SelectSQL<T> orderBy(OrderBy orderBy) {
        return makeNew(wheres, OrderBy.appendToArray(orderBys, orderBy), offset, limit);
    }

    @Override
    public SelectSQL<T> offset(int startRow) {
        return makeNew(wheres, orderBys, startRow, limit);
    }

    @Override
    public SelectSQL<T> limit(int maxRows) {
        return makeNew(wheres, orderBys, offset, maxRows);
    }

    public boolean appendLimitOffsetClause(StringBuilder sql, List<Object> bindArgs) throws IOException {
        boolean modified = false;
        if (this.limit >= 0) {
            sql.append(" LIMIT " + this.limit);
            modified = true;
        }
        if (this.offset > 0) {
            sql.append(" OFFSET " + this.offset);
            modified = true;
        }
        return modified;
    }
    
    protected abstract SelectSQL<T> makeNew(WhereExpression[] wheres, OrderBy[] orderBys, int offset, int limit);      
        
    protected boolean appendLocalOrderByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException {
        if (this.orderBys != null && this.orderBys.length > 0) {
            boolean hasOneClause = false;
            
            for (OrderBy orderBy : this.orderBys) {
                if (orderBy != null) {
                    String thisAlias = orderBy.getAlias();
                    if (thisAlias == null) {
                        thisAlias = resolver.getDefaultAlias();
                    }
                    ResolvedColumnMetadata rcm = resolver.resolve(orderBy.getField(), thisAlias);
                    if (rcm == null) {
                        throw new RuntimeException("Can't resolve field name " + thisAlias + "." + orderBy.getField());
                    } else {
                        hasOneClause = true;
                        rcm.writeSQLExpression(sql, bindArgs, resolver);
                        sql.append(orderBy.isAsc() ? " ASC" : " DESC").append(", ");
                    }
                }
            }
            if (hasOneClause) {
                sql.setLength(sql.length() - 2);
                return true;
            }
        }
        return false;
    }
}
