/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.lucene;

public class PagingHint {
    private int rowsPerPage = -1;
    private int selectedPageNo = 1;
    private int maxRows = -1;
    
    private int hintOffset = 0;
    private int hintLimit = 0;
    
    protected PagingHint() {}
    
    public static PagingHint newHintFromRowsPerPage(int rowsPerPage, int selectedPageNo) {
        PagingHint hint = new PagingHint();
        hint.rowsPerPage = rowsPerPage;
        hint.selectedPageNo = selectedPageNo;
        if (rowsPerPage >= 0) {
            hint.hintOffset = rowsPerPage * (selectedPageNo - 1);
            hint.hintLimit = rowsPerPage * selectedPageNo;
        } else {
            hint.hintOffset = 0;
            hint.hintLimit = -1;
        }
        return hint;
    }
    
    public static PagingHint newHintFromOffsets(int offset, int limit) {
        PagingHint hint = new PagingHint();
        hint.hintOffset = offset;
        hint.hintLimit = limit;
        return hint;
    }
    
    public int getRowsPerPage() {
        return rowsPerPage;
    }
    public int getSelectedPageNo() {
        return selectedPageNo;
    }
    public int getMaxRows() {
        return maxRows;
    }
    public int getHintOffset() {
        return hintOffset;
    }
    public int getHintLimit() {
        return hintLimit;
    }

    public void setMaxRows(int hintMaxRows) {
        this.maxRows = hintMaxRows;
    }
}
