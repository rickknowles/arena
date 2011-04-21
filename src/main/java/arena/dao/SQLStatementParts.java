package arena.dao;

import java.io.IOException;
import java.util.List;

public interface SQLStatementParts {

    public boolean appendSelectClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException;

    public boolean appendFromClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException;

    public boolean appendGroupByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException;

    public boolean appendWhereClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException;

    public boolean appendOrderByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver) throws IOException;

    public boolean appendLimitOffsetClause(StringBuilder sql, List<Object> bindArgs) throws IOException;
}
