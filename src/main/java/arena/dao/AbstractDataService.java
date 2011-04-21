package arena.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.support.DaoSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

public abstract class AbstractDataService extends DaoSupport {
    private final Log log = LogFactory.getLog(this.getClass());
    
    private JdbcTemplate jdbcTemplate;

    @Override
    protected void checkDaoConfig() throws IllegalArgumentException {
        if (this.jdbcTemplate == null) {
            throw new IllegalArgumentException("No data source injected");
        }
    }
    
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> query(String sql, Object[] args, RowMapper rowMapper) {
        log.info("SQL: " + sql);
        int count = 1;
        for (Object arg : args) {
            log.info("SQL Arg " + count++ + ": " + arg);
        }
        
        List<T> results = null;
        long start = System.currentTimeMillis();
        try {
            results = (List<T>) this.jdbcTemplate.query(sql, args, rowMapper);
            return results;
        } finally {
            log.info("SQL returned " + (results == null ? 0 : results.size()) + " rows in " + (System.currentTimeMillis() - start) + "ms");
        }
    }
    
    protected void query(String sql, Object[] args, final RowCallbackHandler rch) {
        log.info("SQL: " + sql);
        int count = 1;
        for (Object arg : args) {
            log.info("SQL Arg " + count++ + ": " + arg);
        }
        final int[] rowCount = new int[1];
        
        long start = System.currentTimeMillis();
        try {
            this.jdbcTemplate.query(sql, args, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rst) throws SQLException {
                    rowCount[0]++;
                    rch.processRow(rst);
                }
            });
        } finally {
            log.info("SQL returned " + rowCount[0] + " rows in " + (System.currentTimeMillis() - start) + "ms");
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper rowMapper) {
        log.info("SQL: " + sql);
        
        List<T> results = null;
        long start = System.currentTimeMillis();
        try {
            results = (List<T>) this.jdbcTemplate.query(sql, pss, rowMapper);
            return results;
        } finally {
            log.info("SQL returned " + (results == null ? 0 : results.size()) + " rows in " + (System.currentTimeMillis() - start) + "ms");
        }
    }
    
    protected void query(String sql, PreparedStatementSetter pss, final RowCallbackHandler rch) {
        log.info("SQL: " + sql);
        final int[] rowCount = new int[1];
        
        long start = System.currentTimeMillis();
        try {
            this.jdbcTemplate.query(sql, pss, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rst) throws SQLException {
                    rowCount[0]++;
                    rch.processRow(rst);
                }
            });
        } finally {
            log.info("SQL returned " + rowCount[0] + " rows in " + (System.currentTimeMillis() - start) + "ms");
        }
    }
    
    @SuppressWarnings("unchecked")
    protected <T> T query(String sql, Object[] args, ResultSetExtractor rse) {
        log.info("SQL: " + sql);
        int count = 1;
        for (Object arg : args) {
            log.info("SQL Arg " + count++ + ": " + arg);
        }
        
        T results = null;
        long start = System.currentTimeMillis();
        try {
            results = (T) this.jdbcTemplate.query(sql, args, rse);
            return results;
        } finally {
            log.info("SQL returned in " + (System.currentTimeMillis() - start) + "ms");
        }
    }
    
    protected int queryForInt(String sql, Object[] args) {
        log.info("SQL: " + sql);
        int count = 1;
        for (Object arg : args) {
            log.info("SQL Arg " + count++ + ": " + arg);
        }
        
        int results = 0;
        long start = System.currentTimeMillis();
        try {
            results = this.jdbcTemplate.queryForInt(sql, args);
            return results;
        } finally {
            log.info("SQL returned 1 row in " + (System.currentTimeMillis() - start) + "ms");
        }
    }

}
