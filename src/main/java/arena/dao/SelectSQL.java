package arena.dao;

import java.util.List;
import java.util.Map;

public interface SelectSQL<T> {
    
    public boolean isEmpty();
    public int rowCount();
    public T unique();
    public T first();
    public List<T> list();
    public <X> Map<X,T> mapByField(String fieldName);
    public void consume(SelectSQLConsumer<T> consumer); 
    public <X> X consumeOut(SelectSQLConsumerWithOutput<T,X> consumer); 
    public int delete();
    
    public SelectSQL<T> where(String field, Object value);
    public SelectSQL<T> where(String field, String operator, Object value);
    public SelectSQL<T> whereNot(String field, Object value);
    public SelectSQL<T> where(WhereExpression where);
    public SelectSQL<T> whereNot(WhereExpression where);
    public SelectSQL<T> ascOrderBy(String field);
    public SelectSQL<T> descOrderBy(String field);
    public SelectSQL<T> orderBy(OrderBy orderBy);
    public SelectSQL<T> offset(int startRow);
    public SelectSQL<T> limit(int maxRows);
    
    public SelectSQL<T> innerJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions);
    public SelectSQL<T> leftJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions);
    public SelectSQL<T> rightJoin(SelectSQL<?> joinToQuery, JoinCondition... conditions);
    
    public SelectSQL<T> innerJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions);
    public SelectSQL<T> leftJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions);
    public SelectSQL<T> rightJoin(SelectSQL<?> joinToQuery, String rightAlias, JoinCondition... conditions);
    
    public SelectSQL<T> getNested();
    
    public AliasDotColumnResolver getResolver(String tableAlias);
}
