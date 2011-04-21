package arena.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ExpressionColumn {

    public boolean isAggregate();
    
    public String getFieldAlias();
    
    public boolean appendSQLExpression(Appendable sql, List<Object> bindArgs) throws IOException;
    
    public Object extractResult(ResultSet rst, int rowNum) throws SQLException;
}
