package arena.dao;

import java.sql.SQLException;

import junit.framework.TestCase;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.mockrunner.mock.jdbc.MockResultSet;

public class ReflectingDAOTest extends TestCase {

    public void testSimpleSelect() throws Exception {
        ReflectingDAO<TestVo> testDAO = new ReflectingDAO<TestVo>();
        testDAO.setTemplate(new JdbcTemplate() {
            @Override
            public Object query(String sql, Object[] args,
                    ResultSetExtractor rse) throws DataAccessException {
                // verify correctly compiled SQL here
                assertEquals("SELECT id, name, number FROM test_vo", sql);
                assertEquals(0, args.length);
                return null;
            }
        });
        testDAO.setAutoCreateSchema(false);
        testDAO.setValueobjectClass(TestVo.class);
        testDAO.checkDaoConfig();
        
        testDAO.select().list();
    }

    public void testWhereOrderSelect() throws Exception {
        ReflectingDAO<TestVo> testDAO = new ReflectingDAO<TestVo>();
        testDAO.setTemplate(new JdbcTemplate() {
            @Override
            public Object query(String sql, Object[] args,
                    ResultSetExtractor rse) throws DataAccessException {
                // verify correctly compiled SQL here
                assertEquals("SELECT id, name, number FROM test_vo WHERE name = ? ORDER BY id ASC", sql);
                assertEquals(1, args.length);
                assertEquals("blue", args[0]);
                return null;
            }
        });
        testDAO.setAutoCreateSchema(false);
        testDAO.setValueobjectClass(TestVo.class);
        testDAO.checkDaoConfig();
        
        testDAO.select().where("name", "blue").ascOrderBy("id").list();
    }

    public void testWhereOrderOffsetLimitSelect() throws Exception {
        ReflectingDAO<TestVo> testDAO = new ReflectingDAO<TestVo>();
        testDAO.setTemplate(new JdbcTemplate() {
            @Override
            public Object query(String sql, Object[] args,
                    ResultSetExtractor rse) throws DataAccessException {
                // verify correctly compiled SQL here
                assertEquals("SELECT id, name, number FROM test_vo WHERE name = ? ORDER BY id ASC LIMIT 3 OFFSET 1", sql);
                assertEquals(1, args.length);
                assertEquals("blue", args[0]);
                return null;
            }
        });
        testDAO.setAutoCreateSchema(false);
        testDAO.setValueobjectClass(TestVo.class);
        testDAO.checkDaoConfig();
        
        testDAO.select().where("name", "blue").limit(3).offset(1).ascOrderBy("id").list();
    }

    public void testJoinSelect() throws Exception {
        ReflectingDAO<TestVo> testDAO = new ReflectingDAO<TestVo>();
        ReflectingDAO<TestVoJoin> testJoinDAO = new ReflectingDAO<TestVoJoin>();
        testDAO.setTemplate(new JdbcTemplate() {
            @Override
            public Object query(String sql, Object[] args,
                    ResultSetExtractor rse) throws DataAccessException {
                // verify correctly compiled SQL here
                assertEquals("SELECT test_vo.id, test_vo.name, test_vo.number FROM test_vo AS test_vo INNER JOIN test_vo_join AS test_vo_join " +
                		"ON test_vo.id = test_vo_join.id AND test_vo.name = test_vo_join.name WHERE test_vo.name = ?", sql);
                assertEquals(1, args.length);
                assertEquals("abc", args[0]);
                return null;
            }
        });
        testDAO.setAutoCreateSchema(false);
        testDAO.setValueobjectClass(TestVo.class);
        
        testJoinDAO.setAutoCreateSchema(false);
        testJoinDAO.setValueobjectClass(TestVoJoin.class);
        
        testDAO.checkDaoConfig();
        testJoinDAO.checkDaoConfig();
        
        testDAO.select().innerJoin(testJoinDAO.select(), new JoinCondition("id", "id"), new JoinCondition("name", "name")).where("name", "abc").list();
    }

    public void testJoinSelectMultiple() throws Exception {
        ReflectingDAO<TestVo> testDAO = new ReflectingDAO<TestVo>();
        ReflectingDAO<TestVoJoin> testJoinDAO = new ReflectingDAO<TestVoJoin>();
        testDAO.setTemplate(new JdbcTemplate() {
            @Override
            public Object query(String sql, Object[] args,
                    ResultSetExtractor rse) throws DataAccessException {
                // verify correctly compiled SQL here
                assertEquals("SELECT test_vo.id, test_vo.name, test_vo.number " +
                		"FROM test_vo AS test_vo INNER JOIN test_vo_join AS testJoin1 " +
                        "ON test_vo.id = testJoin1.id AND test_vo.name = testJoin1.name " +
                        "INNER JOIN test_vo_join AS testJoin2 ON test_vo.id = testJoin2.id " +
                        "WHERE test_vo.name = ? AND testJoin2.id = ?", sql);
                assertEquals(2, args.length);
                assertEquals("abc", args[0]);
                return null;
            }
        });
        testDAO.setAutoCreateSchema(false);
        testDAO.setValueobjectClass(TestVo.class);
        
        testJoinDAO.setAutoCreateSchema(false);
        testJoinDAO.setValueobjectClass(TestVoJoin.class);
        
        testDAO.checkDaoConfig();
        testJoinDAO.checkDaoConfig();
        
        SelectSQL<TestVo> testSQL = testDAO.select()
               .innerJoin(testJoinDAO.select(), "testJoin1", new JoinCondition("id", "id"), new JoinCondition("name", "name"))
               .innerJoin(testJoinDAO.select().where("id", 3), "testJoin2", new JoinCondition("id", "id"))
               .where("name", "abc");
        testSQL.list();
    }

    public void testRowMapper() throws Exception {
        ReflectingDAO<TestVo> testDAO = new ReflectingDAO<TestVo>();
        ReflectingDAO<TestVoJoin> testJoinDAO = new ReflectingDAO<TestVoJoin>();
        testDAO.setTemplate(new JdbcTemplate() {
            @Override
            public Object query(String sql, Object[] args,
                    ResultSetExtractor rse) throws DataAccessException {
                // verify correctly compiled SQL here
                assertEquals("SELECT test_vo.id, test_vo.name, test_vo.number FROM test_vo AS test_vo INNER JOIN test_vo_join AS test_vo_join " +
                        "ON test_vo.id = test_vo_join.id AND test_vo.name = test_vo_join.name WHERE test_vo.name = ?", sql);
                assertEquals(1, args.length);
                assertEquals("abc", args[0]);
                MockResultSet mrst = new MockResultSet("");
                mrst.addColumn("id");
                mrst.addColumn("name");
                mrst.addColumn("number");
                mrst.addRow(new Object[] {new Long(1L), "testing object", new Integer(34)});
                try {
                    return rse.extractData(mrst);
                } catch (SQLException err) {
                    throw new InvalidDataAccessApiUsageException("Error in extractData", err);
                }
            }
        });
        testDAO.setAutoCreateSchema(false);
        testDAO.setValueobjectClass(TestVo.class);
        
        testJoinDAO.setAutoCreateSchema(false);
        testJoinDAO.setValueobjectClass(TestVoJoin.class);
        
        testDAO.checkDaoConfig();
        testJoinDAO.checkDaoConfig();
        
        TestVo tvo = testDAO.select().innerJoin(testJoinDAO.select(), new JoinCondition("id", "id"), new JoinCondition("name", "name")).where("name", "abc").unique();

        assertNotNull(tvo);
        assertEquals(new Long(1L), tvo.getId());
        assertEquals("testing object", tvo.getName());
        assertEquals(34, tvo.getNumber());
    }

    public void testRowMapperJoin() throws Exception {
        ReflectingDAO<TestVo> testDAO = new ReflectingDAO<TestVo>();
        testDAO.setTemplate(new JdbcTemplate() {
            @Override
            public Object query(String sql, Object[] args,
                    ResultSetExtractor rse) throws DataAccessException {
                // verify correctly compiled SQL here
                assertEquals("SELECT id, name, number FROM test_vo", sql);
                assertEquals(0, args.length);
                MockResultSet mrst = new MockResultSet("");
                mrst.addColumn("id");
                mrst.addColumn("name");
                mrst.addColumn("number");
                mrst.addRow(new Object[] {new Long(1L), "testing object", new Integer(34)});
                try {
                    return rse.extractData(mrst);
                } catch (SQLException err) {
                    throw new InvalidDataAccessApiUsageException("Error in extractData", err);
                }
            }
        });
        testDAO.setAutoCreateSchema(false);
        testDAO.setValueobjectClass(TestVo.class);
        testDAO.checkDaoConfig();
        
        TestVo tvo = testDAO.select().unique();
        assertNotNull(tvo);
        assertEquals(new Long(1L), tvo.getId());
        assertEquals("testing object", tvo.getName());
        assertEquals(34, tvo.getNumber());
    }
}
