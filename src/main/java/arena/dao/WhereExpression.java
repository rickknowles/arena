package arena.dao;

import java.io.IOException;
import java.util.List;

public interface WhereExpression {
    public boolean writeSQL(StringBuilder sql, AliasDotColumnResolver resolver, List<Object> outputArgs) throws IOException;
}
