package arena.dao.where;

import java.io.IOException;
import java.util.List;

import arena.dao.AliasDotColumnResolver;
import arena.dao.WhereExpression;



public class Not implements WhereExpression {

    private WhereExpression negated;
    
    public Not(WhereExpression negated) {
        this.negated = negated;
    }
    
    @Override
    public boolean writeSQL(StringBuilder sql, AliasDotColumnResolver resolver, List<Object> outputArgs) throws IOException {
//        StringBuilder sql = new StringBuilder();
        if (negated != null) {
            sql.append("not(");
            negated.writeSQL(sql, resolver, outputArgs);
//            if (clause != null && !clause.equals("")) {
//                sql.append(clause);
//            }
            sql.append(')');
            return true;
        }
        return false;
//        return sql.toString();
    }
}
