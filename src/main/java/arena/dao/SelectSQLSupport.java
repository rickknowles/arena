package arena.dao;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.dao.consumer.ListConsumer;
import arena.dao.consumer.MapByFieldConsumer;
import arena.dao.where.Not;
import arena.dao.where.Where;

public abstract class SelectSQLSupport<T> implements SelectSQL<T> {
    private final Log log = LogFactory.getLog(SelectSQLSupport.class);

    public boolean isEmpty() {
        return rowCount() == 0;
    }
    
    public T first() {
        return consumeOut(new SelectSQLConsumerWithOutput<T,T>() {
            private T found;
            public T getOutput() {return found;}
            public boolean consume(T input, int rowCount) {
                this.found = input;
                return false;
            }
        });
    }

    public T unique() {
        return consumeOut(new SelectSQLConsumerWithOutput<T,T>() {
            private T found;
            public T getOutput() {return found;}
            public boolean consume(T input, int rowCount) {
                if (this.found != null) {
                    throw new RuntimeException("Resultset contains more than one result");
                } else {
                    this.found = input;
                    return true;
                }
            }
        });
    }

    public List<T> list() {
        return consumeOut(new ListConsumer<T>());
    }
    
    public <X> Map<X,T> mapByField(String fieldName) {
        return consumeOut(new MapByFieldConsumer<X,T>(fieldName));
    }

    /**
     * We can do better than this in most cases, but acceptable as a fallback
     */
    public int rowCount() {
        return consumeOut(new SelectSQLConsumerWithOutput<T,Integer>() {
            int count = 0;
            public boolean consume(T input, int rowCount) {count++; return true;}
            public Integer getOutput() {return count;}
        });
    }
    
    public void consume(SelectSQLConsumer<T> consumer) {
        log.debug("(SelectSQL.consume() not implemented. Override in subclass)");
    }

    public <X> X consumeOut(SelectSQLConsumerWithOutput<T,X> consumer) {
        consume(consumer);
        return consumer.getOutput();
    }

    public int delete() {
        log.warn("Delete not implemented in " + getClass().getName());
        return 0;
    }
    
    public SelectSQL<T> where(String field, Object value) {return where(Where.equals(field, value));}
    public SelectSQL<T> where(String field, String operator, Object value) {return where(Where.custom(field, " " + operator + " ?", value));}
    public SelectSQL<T> whereNot(String field, Object value) {return where(Where.notEquals(field, value));}
    public SelectSQL<T> where(WhereExpression where) {return this;}
    public SelectSQL<T> whereNot(WhereExpression where) {return where(new Not(where));}
    
    public SelectSQL<T> ascOrderBy(String field) {return orderBy(new OrderBy(field, true));}
    public SelectSQL<T> descOrderBy(String field) {return orderBy(new OrderBy(field, false));}
    public SelectSQL<T> orderBy(OrderBy orderBy) {return this;}
    
    public SelectSQL<T> offset(int startRow) {return this;}
    public SelectSQL<T> limit(int maxRows) {return this;}
    
    public SelectSQL<T> innerJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions) {return this;}
    public SelectSQL<T> leftJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions) {return this;}
    public SelectSQL<T> rightJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions) {return this;}
    
    public SelectSQL<T> innerJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions) {return this;}
    public SelectSQL<T> leftJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions) {return this;}
    public SelectSQL<T> rightJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions) {return this;}
    
    public SelectSQL<T> getNested() {return null;}
    
    public AliasDotColumnResolver getResolver(String tableAlias) {
        throw new RuntimeException("getResolver() not implemented on this SelectSQL");
    }

}
