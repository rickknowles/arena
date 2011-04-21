package arena.dao.where;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import arena.dao.AliasDotColumnResolver;
import arena.dao.DAOUtils;
import arena.dao.ResolvedColumnMetadata;
import arena.dao.WhereExpression;



public class Where implements WhereExpression {
    public static final String FIELD_NAME_PATTERN = "###fieldName###";
    
    private String alias;
    private String field;
    private String fieldOperatorValuePattern;
    private Object[] bindArgs;
    
    protected Where(String field, String alias, String fieldOperatorValuePattern, Object... args) {
        this.field = field;
        this.alias = alias;
        this.fieldOperatorValuePattern = fieldOperatorValuePattern;
        this.bindArgs = args;
    }
    @Override
    public boolean writeSQL(StringBuilder sql, AliasDotColumnResolver resolver, 
            List<Object> outputArgs) throws IOException {
        String thisAlias = this.alias;
        if (thisAlias == null) {
            thisAlias = resolver.getDefaultAlias();
        }
        if (field != null) {
            ResolvedColumnMetadata rcm = resolver.resolve(field, thisAlias);
            if (rcm == null) {
                throw new RuntimeException("Can't resolve field name " + thisAlias + "." + field);
            }
            StringBuilder fn = new StringBuilder();
            rcm.writeSQLExpression(fn, outputArgs, resolver);
            sql.append(fieldOperatorValuePattern.replace(FIELD_NAME_PATTERN, fn.toString()));
            for (Object arg : bindArgs) {
                outputArgs.add(rcm.convertToDBValue(arg));
            }
        } else {
            sql.append(fieldOperatorValuePattern);
            for (Object arg : bindArgs) {
                outputArgs.add(DAOUtils.convertToDBValue(arg.getClass(), arg));
            }
        }
        return true;
    }
    
    public static WhereExpression equals(String field, Object arg) {
        return new Where(field, null, FIELD_NAME_PATTERN + " = ?", arg);
    }
    
    public static WhereExpression equalsCaseInsensitive(String field, String arg) {
        return new Where(field, null, "upper(" + FIELD_NAME_PATTERN + ") = upper(?)", arg);
    }
    
    public static WhereExpression equals(String field, String alias, Object arg) {
        return new Where(field, alias, FIELD_NAME_PATTERN + " = ?", arg);
    }
    
    public static WhereExpression notEquals(String field, Object arg) {
        return new Where(field, null, FIELD_NAME_PATTERN + " <> ?", arg);
    }
    
    public static WhereExpression notEqualsCaseInsensitive(String field, String arg) {
        return new Where(field, null, "upper(" + FIELD_NAME_PATTERN + ") <> upper(?)", arg);
    }
    
    public static WhereExpression notEquals(String field, String alias, Object arg) {
        return new Where(field, alias,FIELD_NAME_PATTERN +  " <> ?", arg);
    }
    
    public static WhereExpression lessThan(String field, Object arg) {
        return new Where(field, null, FIELD_NAME_PATTERN + " < ?", arg);
    }
    
    public static WhereExpression lessThan(String field, String alias, Object arg) {
        return new Where(field, alias, FIELD_NAME_PATTERN + " < ?", arg);
    }
    
    public static WhereExpression greaterThan(String field, Object arg) {
        return new Where(field, null, FIELD_NAME_PATTERN + " > ?", arg);
    }
    
    public static WhereExpression greaterThan(String field, String alias, Object arg) {
        return new Where(field, alias, FIELD_NAME_PATTERN + " > ?", arg);
    }
    
    public static WhereExpression lessThanEqual(String field, Object arg) {
        return new Where(field, null, FIELD_NAME_PATTERN + " <= ?", arg);
    }
    
    public static WhereExpression lessThanEqual(String field, String alias, Object arg) {
        return new Where(field, alias, FIELD_NAME_PATTERN + " <= ?", arg);
    }
    
    public static WhereExpression greaterThanEqual(String field, Object arg) {
        return new Where(field, null, FIELD_NAME_PATTERN + " >= ?", arg);
    }
    
    public static WhereExpression greaterThanEqual(String field, String alias, Object arg) {
        return new Where(field, alias, FIELD_NAME_PATTERN + " >= ?", arg);
    }
    
    public static WhereExpression isNull(String field) {
        return new Where(field, null, FIELD_NAME_PATTERN + " IS NULL");
    }
    
    public static WhereExpression isNull(String field, String alias) {
        return new Where(field, alias, FIELD_NAME_PATTERN + " IS NULL");
    }
    
    public static WhereExpression isNotNull(String field) {
        return new Where(field, null, FIELD_NAME_PATTERN + " IS NOT NULL");
    }
    
    public static WhereExpression alwaysFalse() {
        return new Where(null, null, "1 = 0");
    }
    
    public static WhereExpression alwaysTrue() {
        return new Where(null, null, "1 = 1");
    }
    
    public static WhereExpression custom(String field, String operator, Object... args) {
        return new Where(field, null, FIELD_NAME_PATTERN + " " + operator + " ?", args);
    }

    public static WhereExpression isNotNull(String field, String alias) {
        return new Where(field, alias, FIELD_NAME_PATTERN + " IS NOT NULL");
    }
    
    public static WhereExpression custom(String field, String alias, String operator, Object... args) {
        return new Where(field, alias, FIELD_NAME_PATTERN + " " + operator + " ?", args);
    }
    
    public static WhereExpression[] appendToArray(WhereExpression[] arr, WhereExpression extra) {
        WhereExpression[] out = null;
        if (arr == null) {
            out = (WhereExpression[]) Array.newInstance(WhereExpression.class, 1);
        } else {
            out = Arrays.copyOf(arr, arr.length + 1);
        }
        out[out.length - 1] = extra;
        return out;
    }
}
