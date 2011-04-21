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

import org.apache.lucene.index.IndexReader;

/**
 * Returns an index reader determined using the query model as input. The simplest case is
 * to always return the same reader, but when indices are split we can use this hook to return a
 * parallel reader that aggregates only the indices relevant to the query.
 * <p>
 * Note: When a reader is retrieved using getIndexReader(), it *must* be released using releaseReader()
 * when processing is complete. Use finally clauses if necessary. 
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public interface LuceneReaderResolver {

    public IndexReader getIndexReader(LuceneQueryModel queryModel) throws IOException;
    
    public void releaseReader(IndexReader reader, LuceneQueryModel queryModel) throws IOException;
    
    public void reset() throws IOException;
}
