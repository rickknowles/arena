package arena.validation;

import arena.dao.SelectSQL;
import arena.dao.where.Where;

public class DAOExistenceCheck extends ValidationCheck {

    private String idField;
    private SelectSQL<?> selectSQL;
    private boolean convertToLong = true;
    private boolean negate = false;
    private boolean caseSensitive = true;
    
    public DAOExistenceCheck(SelectSQL<?> selectSQL, String idField) {
        this(selectSQL, idField, true, false, true);
    }
    
    public DAOExistenceCheck(SelectSQL<?> selectSQL, String idField, boolean convertToLong, boolean negate, boolean caseSensitive) {
        this.selectSQL = selectSQL;
        this.idField = idField;
        this.convertToLong = convertToLong;
        this.negate = negate;
        this.caseSensitive = caseSensitive;
    }
    
    @Override
    public ValidationFailure validate(Object value) {
        if (value != null && !value.equals("")) {
            Object id = this.convertToLong ? Long.valueOf(value.toString()) : value;
            SelectSQL<?> sql = this.selectSQL.where(this.caseSensitive 
                    ? Where.equals(idField, id) 
                    : Where.equalsCaseInsensitive(idField, id.toString()));
            boolean found = (sql.rowCount() <= 0);
            if (found && !this.negate) {
                return new ValidationFailure(this.getFieldName(), "existenceCheck", value);
            } else if (!found && this.negate) {
                return new ValidationFailure(this.getFieldName(), "nonExistenceCheck", value);
            }
        }
        return null;
    }

}
