package arena.dao.where;

import java.io.IOException;
import java.util.List;

import arena.dao.AliasDotColumnResolver;
import arena.dao.WhereExpression;



public class Or implements WhereExpression {

    private WhereExpression[] children;
    private boolean includeBrackets;
    
    public Or(WhereExpression... children) {
        this(true, children);
    }
    
    public Or(boolean includeBrackets, WhereExpression... children) {
        this.children = children;
    }
    
    protected String getJoinKeyword() {
        return " OR ";
    }
    
    public void add(WhereExpression where) {
        this.children = Where.appendToArray(children, where);
    }
    
    @Override
    public boolean writeSQL(StringBuilder sql, AliasDotColumnResolver resolver, List<Object> outputArgs) throws IOException {
        if (children != null && children.length > 0) {
            String joinKeyword = getJoinKeyword();
            boolean hasOneClause = false;
            if (this.includeBrackets) {
                sql.append('(');    
            }
            
            for (WhereExpression where : children) {
                if (where != null && where.writeSQL(sql, resolver, outputArgs)) {
                    hasOneClause = true;
                    sql.append(joinKeyword);
                }
            }
            if (hasOneClause) {
                sql.setLength(sql.length() - joinKeyword.length());
                if (this.includeBrackets) {
                    sql.append(')');    
                }
                return true;
            } else {
                sql.setLength(sql.length() - 1);
            }
        }
        return false;
    }

}
