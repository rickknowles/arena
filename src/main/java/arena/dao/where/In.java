package arena.dao.where;

import java.io.IOException;
import java.util.List;

import arena.dao.AliasDotColumnResolver;
import arena.dao.ResolvedColumnMetadata;
import arena.dao.WhereExpression;
import arena.utils.ReflectionUtils;



public class In implements WhereExpression {

    private String alias;
    private String fieldName;
    private List<?> items;
    private String attributeName;
    
    public In(String fieldName, List<?> items) {
        this(fieldName, null, items, null);
    }
    
    public In(String fieldName, List<?> items, String attributeName) {
        this(fieldName, null, items, attributeName);
    }
    
    public In(String fieldName, String alias, List<?> items, String attributeName) {
        this.fieldName = fieldName;
        this.alias = alias;
        this.items = items;
        this.attributeName = attributeName;
    }
    
    @Override
    public boolean writeSQL(StringBuilder sql, AliasDotColumnResolver resolver, List<Object> outputArgs) throws IOException {
        if (this.items == null || this.items.isEmpty()) {
            sql.append("1 = 0"); // definite false
            return true;
        }
        String thisAlias = this.alias;
        if (thisAlias == null) {
            thisAlias = resolver.getDefaultAlias();
        }
        ResolvedColumnMetadata resolved = resolver.resolve(fieldName, thisAlias);
        if (resolved == null) {
            throw new RuntimeException("Can't resolve field name " + thisAlias + "." + fieldName);
        } else {
            resolved.writeSQLExpression(sql, outputArgs, resolver);
            sql.append(" IN (");
            for (int n = 0; n < this.items.size(); n++) {
                sql.append(n == 0 ? "?" : ", ?");
                Object arg = this.items.get(n);
                if (attributeName != null) {
                    arg = ReflectionUtils.getAttributeUsingGetter(attributeName, arg);
                }
                outputArgs.add(resolved.convertToDBValue(arg));
            }
            sql.append(")");
            return true;
        }
//        ColumnMetadata cm = selectSql.getMappedColumn(fieldName, alias);    
//        String sql = cm.makeTableDotColumnName(alias) + " IN (";
//        for (Object i : items) {
//            sql += "?, ";
//            Object arg = i;
//            if (attributeName != null) {
//                arg = ReflectionUtils.getAttributeUsingGetter(attributeName, i);
//            }
//            outputArgs.add(arg);
//        }
//        return sql.substring(0, sql.length() - 2) + ")";
        
    }

}
