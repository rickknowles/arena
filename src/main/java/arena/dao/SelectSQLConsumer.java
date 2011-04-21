package arena.dao;

public interface SelectSQLConsumer<I> {
    /**
     * Called once per row in the result set. Return true to continue with the resultset if rows are available.
     * @param input Item parsed from the resultset to be consumed
     * @param rowNum Current row number
     * @return true to continue reading results, false to quit
     */
    public boolean consume(I input, int rowNum) throws Exception;
}
