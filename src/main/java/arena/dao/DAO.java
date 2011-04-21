package arena.dao;

public interface DAO<T> extends ReadOnlyDAO<T> {
    public int insert(T... rows);
    public int update(T... rows);
    public int delete(T... rows);
    
    public int insert(Iterable<T> rows);
    public int update(Iterable<T> rows);
    public int delete(Iterable<T> rows);

}
