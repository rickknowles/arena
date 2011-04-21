package arena.action;

import java.util.List;

import arena.dao.SelectSQL;



public class PagedRowSet<T> {
    private List<T> rows;
    private int rowCount;
    private int selectedPage;
    private int maxPages;
    private int startRow;
    private int endRow;
    
    public List<T> getRows() {
        return rows;
    }
    public void setRows(List<T> rows) {
        this.rows = rows;
    }
    public int getRowCount() {
        return rowCount;
    }
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
    public int getSelectedPage() {
        return selectedPage;
    }
    public void setSelectedPage(int selectedPage) {
        this.selectedPage = selectedPage;
    }
    public int getMaxPages() {
        return maxPages;
    }
    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }
    public int getStartRow() {
        return startRow;
    }
    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }
    public int getEndRow() {
        return endRow;
    }
    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }
    
    public static <X> SelectSQL<X> wrapInJQGridSort(SelectSQL<X> sql, RequestState state, String... allowedSortFields) throws Exception {
        String sidx = state.getArg("sidx", "");
        String sord = state.getArg("sord", "asc");
        for (String fld : allowedSortFields) {
            if (fld.equalsIgnoreCase(sidx)) {
                if (sord.equals("desc")) {
                    sql = sql.descOrderBy(fld);
                } else {
                    sql = sql.ascOrderBy(fld);
                }
            }
        }
        return sql;
    }
    
    public static <X> SelectSQL<X> wrapInJQGridLimit(SelectSQL<X> sql, RequestState state, int rowCount) {
        String page = state.getArg("page", "1");
        String rows = state.getArg("rows", "");
        if (!rows.equals("")) {
            int rowsPerPage = Integer.parseInt(rows);
            int endRow = (Integer.parseInt(page) * rowsPerPage);
            int startRow = Math.min(rowCount, Math.max(0, endRow - rowsPerPage));
            sql = sql.limit(startRow + rowsPerPage).offset(startRow);
        }
        return sql;
    }
    
    public static <X> PagedRowSet<X> wrapInJQGridPaging(List<X> rowData, RequestState state, int rowCount) {
        PagedRowSet<X> out = new PagedRowSet<X>();
        out.setRows(rowData);
        out.setRowCount(rowCount);
        
        String page = state.getArg("page", "1");
        String rows = state.getArg("rows", "");
        
        if (!rows.equals("")) {
            int rowsPerPage = Integer.parseInt(rows);
            int endRow = (Integer.parseInt(page) * rowsPerPage);
            int startRow = Math.min(rowCount, Math.max(0, endRow - rowsPerPage));
            out.setEndRow(Math.max(rowsPerPage, startRow + rowsPerPage));
            out.setStartRow(startRow);
            out.setMaxPages((int) Math.ceil((double) rowCount / (double) rowsPerPage));
        }
        out.setSelectedPage(Integer.parseInt(page));
        return out;
    }
}
