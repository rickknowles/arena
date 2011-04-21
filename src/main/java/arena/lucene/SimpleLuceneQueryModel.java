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

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

/**
 * A simple bean implementation of the LuceneQueryModel interface. Allows
 * pre-generation of the query details before passing to lucene.
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class SimpleLuceneQueryModel implements LuceneQueryModel {
    
    private Query query;
    private Filter filter;
    private Sort sort;
    
    public SimpleLuceneQueryModel() {}
    public SimpleLuceneQueryModel(Query query, Filter filter, Sort sort) {
        this();
        setQuery(query);
        setFilter(filter);
        setSort(sort);
    }
    
    public Query getQuery() {
        return query;
    }
    public void setQuery(Query query) {
        this.query = query;
    }
    public Filter getFilter() {
        return filter;
    }
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    public Sort getSort() {
        return sort;
    }
    public void setSort(Sort sort) {
        this.sort = sort;
    }
}
