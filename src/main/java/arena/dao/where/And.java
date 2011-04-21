package arena.dao.where;

import arena.dao.WhereExpression;

public class And extends Or {
    
    public And(WhereExpression... children) {
        super(children);
    }
    
    public And(boolean includeBrackets, WhereExpression... children) {
        super(includeBrackets, children);
    }

    @Override
    protected String getJoinKeyword() {
        return " AND ";
    }
}
