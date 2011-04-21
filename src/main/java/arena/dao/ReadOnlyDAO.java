package arena.dao;

public interface ReadOnlyDAO<T> {
    public SelectSQL<T> select();
}
