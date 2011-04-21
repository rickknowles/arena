package arena.dao;

import org.springframework.dao.support.DaoSupport;

public abstract class ReadOnlyDAOSupport<T> extends DaoSupport implements ReadOnlyDAO<T> {


//    public AliasDotColumnResolver getResolver(String tableAlias) {
//        throw new RuntimeException("getResolver() not implemented on this DAO");
//    }
//
//    public ParameterizedRowMapper<T> getRowMapper(String tableAlias) {
//        throw new RuntimeException("getRowMapper() not implemented on this DAO");
//    }
    
    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
    }

}
