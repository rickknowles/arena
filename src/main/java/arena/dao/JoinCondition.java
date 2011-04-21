package arena.dao;

/**
 * Force joins to be "right nested", meaning the right side of the join is always a single table query. As a
 * result, the rightAlias never needs to be specified because it's always the only table available. 
 */
public class JoinCondition {

    private String leftField;
    private String rightField;
    private String leftAlias;
    
    public JoinCondition(String leftField, String rightField) {
        this.leftField = leftField;
        this.rightField = rightField;
    }
    
    public JoinCondition(String leftAlias, String leftField, String rightField) {
        this.leftField = leftField;
        this.rightField = rightField;
        this.leftAlias = leftAlias;
    }
    
    public String getLeftField() {
        return leftField;
    }
    public String getRightField() {
        return rightField;
    }
    public String getLeftAlias() {
        return leftAlias;
    }
    
}
