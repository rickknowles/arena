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

import java.io.IOException;
import java.util.List;

public interface LuceneIndexSearcher<T> {

    /**
     * Perform the search of the lucene index, returning only the document ids in the LuceneSearchResult object.
     */
    public List<LuceneSearchResult<T>> search(LuceneQueryModel queryModel, PagingHint pagingHint, boolean resolve) throws IOException;

    /**
     * Fully resolve the loaded objects in the results object
     */
    public void resolve(List<LuceneSearchResult<T>> results, PagingHint pagingHint, LuceneQueryModel queryModel) throws IOException;

    /**
     * Reset internals so we can re-initialize
     */
    public void reset() throws IOException;
    
}