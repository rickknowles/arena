package arena.dao;

import java.io.IOException;
import java.util.List;

public interface ResolvedColumnMetadata {

    /**
     * Writes the sql. Assumes that resolve has been called first
     * @param sql
     * @param fieldName
     * @throws IOException
     */
    public void writeSQLExpression(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException;

    /**
     * Converts to a java side value to its DB side representation (e.g. true/false becomes 1/0, etc)
     * @param in java side value
     * @return db side representation of java side value
     */
    public Object convertToDBValue(Object in);

}
