package arena.dao;

import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class DAOUtils {

    public static Object convertToDBValue(Class<?> fieldType, Object in) {
        if (in == null) {
            return null;
        }
        // do date to timestamp conversions and boolean to int conversions here
        if (fieldType.isAssignableFrom(Date.class)) {
            return new java.sql.Timestamp(((Date) in).getTime());
        } else if (fieldType.isAssignableFrom(Boolean.class) ||
                fieldType.isAssignableFrom(Boolean.TYPE)) {
            return in.equals(Boolean.TRUE) ? 1 : 0;
        } else {
            return in;
        }
    }
    
    public static Object getFieldFromResultSet(String retrievalFieldName, Class<?> fieldType, 
            ResultSet attributes, char buffer[]) throws SQLException {
        if (fieldType.isAssignableFrom(Long.class) || fieldType.equals(Long.TYPE)) {
            long result = attributes.getLong(retrievalFieldName);
            return attributes.wasNull() ? null : new Long(result);
        } else if (fieldType.isAssignableFrom(Integer.class) || fieldType.equals(Integer.TYPE)) {
            int result = attributes.getInt(retrievalFieldName);
            return attributes.wasNull() ? null : new Integer(result);
        } else if (fieldType.isAssignableFrom(Date.class)) {
            Date date = attributes.getTimestamp(retrievalFieldName);
            return date == null ? null : new Date(date.getTime()); // to avoid db specific java.sql.Date types
        } else if (fieldType.isAssignableFrom(Boolean.class) || fieldType.equals(Boolean.TYPE)) {
            long result = attributes.getLong(retrievalFieldName);
            return attributes.wasNull() ? null : ((result == 0) ? Boolean.FALSE : Boolean.TRUE);
        } else if (fieldType.isAssignableFrom(Float.class) || fieldType.equals(Float.TYPE)) {
            float result = attributes.getFloat(retrievalFieldName);
            return attributes.wasNull() ? null : new Float(result);
        } else if (fieldType.isAssignableFrom(Double.class) || fieldType.equals(Double.TYPE)) {
            double result = attributes.getDouble(retrievalFieldName);
            return attributes.wasNull() ? null : new Double(result);
        } else {
            // Get the clob contents, and read it into a string
            Reader content = attributes.getCharacterStream(retrievalFieldName);
            if (content == null) {
                return null;
            } else try {
                int readChars = content.read(buffer);
                if (readChars == -1) {
                    return "";
                }
                int next = content.read();
                if (next == -1) {
                    return new String(buffer, 0, readChars);
                }
                StringBuilder out = new StringBuilder();
                out.append(buffer, 0, readChars).append((char) next);
                while ((readChars = content.read(buffer)) != -1) {
                    out.append(buffer, 0, readChars);
                }
                return out.toString();
            } catch (IOException err) {
                throw new SQLException("Error reading clob from database");
            } finally {
                try {content.close();} catch (IOException err) {}
            }
        }
    }
    
    public static <T> void consume(String sql, List<Object> bindArgs, final SelectSQLConsumer<T> consumer, final JdbcTemplate readTemplate, 
            final ParameterizedRowMapper<T> daoRowMapper, Log log) {
        log.info("SQL: " + sql.toString());
        int count = 1;
        for (Object arg : bindArgs) {
            log.info("SQL Arg " + count++ + ": " + arg);
        }
        
        final int[] rowCount = new int[1];
        long start = System.currentTimeMillis();
        try {
           readTemplate.query(sql.toString(), bindArgs.toArray(), new ResultSetExtractor() {               
                @Override
                public Object extractData(ResultSet rst) throws SQLException, DataAccessException {
                    boolean continueFlag = true;
                    while (continueFlag && rst.next()) {
                        try {
                            continueFlag = consumer.consume(daoRowMapper.mapRow(rst, rowCount[0]), rowCount[0]);
                            rowCount[0]++;
                        } catch (Exception err) {
                            throw new RuntimeException();
                        }
                    }
                    return null; // not used anyway
                }
            });
        } finally {
            log.info("SQL returned " + rowCount[0] + " rows in " + (System.currentTimeMillis() - start) + "ms");
        }        
    }
    
    public static void buildSelectSQL(SQLStatementParts parts, Appendable sql, List<Object> bindArgs, AliasDotColumnResolver resolver, boolean includeGroupBy, 
            boolean includeOrderBy, boolean includeLimitOffset) {
        try {
            StringBuilder mca = new StringBuilder();
            mca.append("SELECT ");
            parts.appendSelectClause(mca, bindArgs, resolver);
            mca.append(" FROM ");
            parts.appendFromClause(mca, bindArgs, resolver);
            mca.append(" WHERE ");
            int preLength = mca.length();
            if (!parts.appendWhereClause(mca, bindArgs, resolver)) {
                mca.setLength(preLength - 7);
            }
            if (includeGroupBy) {
                mca.append(" GROUP BY ");
                preLength = mca.length();
                if (!parts.appendGroupByClause(mca, bindArgs, resolver)) {
                    mca.setLength(preLength - 10);
                }
            }
            if (includeOrderBy) {
                mca.append(" ORDER BY ");
                preLength = mca.length();
                if (!parts.appendOrderByClause(mca, bindArgs, resolver)) {
                    mca.setLength(preLength - 10);
                }
            }
            parts.appendLimitOffsetClause(mca, bindArgs);
            sql.append(mca);        
        } catch (IOException err) {
            throw new RuntimeException("Error building select SQL", err);
        }
    }

    public static boolean appendOrderByClause(StringBuilder sql, List<Object> bindArgs, AliasDotColumnResolver resolver, OrderBy... orderBys) throws IOException {
        if (orderBys != null && orderBys.length > 0) {
            StringBuilder tempSql = new StringBuilder();
            boolean hasOneClause = false;
            
            for (OrderBy orderBy : orderBys) {
                if (orderBy != null) {
                    String thisAlias = orderBy.getAlias();
                    if (thisAlias == null) {
                        thisAlias = resolver.getDefaultAlias();
                    }
                    ResolvedColumnMetadata rcm = resolver.resolve(orderBy.getField(), thisAlias);
                    if (rcm == null) {
                        throw new RuntimeException("Can't resolve field name " + thisAlias + "." + orderBy.getField());
                    }
                    hasOneClause = true;
                    rcm.writeSQLExpression(tempSql, bindArgs, resolver);
                    tempSql.append(orderBy.isAsc() ? " ASC" : " DESC").append(", ");
                }
            }
            if (hasOneClause) {
                sql.append(tempSql.subSequence(0, tempSql.length() - 2));
                return true;
            }
        }
        return false;
    }
}
