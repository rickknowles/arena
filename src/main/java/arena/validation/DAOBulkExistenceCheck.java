package arena.validation;

import java.util.ArrayList;
import java.util.List;

import arena.dao.SelectSQL;



public class DAOBulkExistenceCheck extends ValidationCheck {

    private String idField;
    private SelectSQL<?> selectSQL;
    private boolean convertToLong;
    
    public DAOBulkExistenceCheck(SelectSQL<?> selectSQL, String idField) {
        this(selectSQL, idField, true);
    }
    
    public DAOBulkExistenceCheck(SelectSQL<?> selectSQL, String idField, boolean convertToLong) {
        this.selectSQL = selectSQL;
        this.idField = idField;
        this.convertToLong = convertToLong;
    }
    
    @Override
    public ValidationFailure validate(Object value) {
        if (value != null) {
            if (!(value instanceof String[])) {
                if (value.equals("")) {
                    return null;
                }
                value = new String[] {value.toString()};
            }
            
            // NOTE: once DAOs support IN-clauses or OR queries, it'd be nice to remove the loop from this
            List<Object> notFound = null;
            for (String idStr : (String[]) value) {
                Object id = this.convertToLong ? Long.valueOf(idStr.toString()) : idStr;
                if (this.selectSQL.where(idField, id).rowCount() <= 0) {
                    if (notFound == null) {
                        notFound = new ArrayList<Object>();
                    }
                    notFound.add(id);
                }
            }
            if (notFound != null) {
                return new ValidationFailure(getFieldName(), "existenceCheck", notFound);
            }
        }
        return null;
    }

}
