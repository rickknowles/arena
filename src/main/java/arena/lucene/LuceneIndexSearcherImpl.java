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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopScoreDocCollector;

import arena.collections.ResultFragmentList;


public class LuceneIndexSearcherImpl<T> implements LuceneIndexSearcher<T>{
    private final Log log = LogFactory.getLog(LuceneIndexSearcherImpl.class);
    
    private int absoluteMaxResultCount = Integer.MAX_VALUE;
    
//    private LuceneDirectoryBean directory;
//    private IndexReader indexReader;
    private LuceneIndexContentReader<T> contentMarshall;
    
    private LuceneReaderResolver readerResolver;
    
    public List<LuceneSearchResult<T>> search(LuceneQueryModel queryModel, PagingHint pagingHint, boolean resolve) throws IOException {
        if (this.readerResolver == null) {
            throw new IllegalArgumentException("No readerResolver available, call setDirectory() or setReaderResolver()");
        }
        Query query = queryModel.getQuery();
        Filter filter = queryModel.getFilter();
        Sort sort = queryModel.getSort();
        IndexReader reader = null;
        try {
            reader = this.readerResolver.getIndexReader(queryModel);
            IndexSearcher searcher = new IndexSearcher(reader);
            ResultFragmentList<LuceneSearchResult<T>> out = searchUnresolved(searcher, query, filter, sort, pagingHint);
            if (resolve) {
                resolveInternal(searcher, out, out.getFragmentSize(), pagingHint, queryModel);
            }
            return out;
        } finally {
            if (reader != null) {
                this.readerResolver.releaseReader(reader, queryModel);
            }
        }
    }
    
    /**
     * Expert: Direct access to the query execution. Only call this if you understand the meaning of the Weight 
     * class and its caching implications. Use  
     */
    protected ResultFragmentList<LuceneSearchResult<T>> searchUnresolved(IndexSearcher searcher, Query query, Filter filter, 
            Sort sort, PagingHint pagingHint) throws IOException {
        int hintOffset = pagingHint == null ? 0 : pagingHint.getHintOffset();
        int hintLimit = pagingHint == null ? -1 : pagingHint.getHintLimit();
        int hintMaxRows = pagingHint == null ? -1 : pagingHint.getMaxRows();
        int collectorLimit = hintMaxRows;
        if ((collectorLimit < 0) || ((hintLimit >= 0) && (hintMaxRows > hintLimit))) {
            collectorLimit = hintLimit;
        }
        if (collectorLimit < 0) {
         // force a hard limit on results retrieved, most times paging will take care of details
            collectorLimit = Math.min(searcher.maxDoc(), this.absoluteMaxResultCount); 
        }
        
        log.info("Issuing lucene query: query=" + query + " filter=" + filter + " sort=" + sort + 
                ", paging=[pageStart:" + hintOffset + " pageEnd:" + hintLimit + " maxRows:" + hintMaxRows + 
                " collectorLimit=" + collectorLimit + "]");

        long startTime = System.currentTimeMillis();
        TopDocs topDocs = executeSearch(searcher, query, filter, sort, collectorLimit);        
        
        // work out start and end rows
        int max = hintMaxRows >= 0 ? Math.min(hintMaxRows, topDocs.totalHits) : topDocs.totalHits;
        int start = Math.min(hintOffset, max);
        int end = Math.min(hintLimit >= 0 ? hintLimit : max, max);
        
        List<LuceneSearchResult<T>> results = new ArrayList<LuceneSearchResult<T>>();
        for (int n = start; n < end; n++) {
            ScoreDoc scoreDoc = topDocs.scoreDocs[n];
            results.add(new LuceneSearchResult<T>(scoreDoc.doc, n, scoreDoc.score));
        }

        if (max == 0) {
            log.info("Lucene search returned 0 hits in " + (System.currentTimeMillis() - startTime) + "ms");
        } else if (end <= start) {
            log.info("Lucene search returned " + max + " hits, requested unavailable results starting at " + 
                    (start + 1) + " in " + (System.currentTimeMillis() - startTime) + "ms");
        } else {
            log.info("Lucene search returned " + max + " hits, iterated results " + 
                    (start + 1) + "-" + end + " in " + (System.currentTimeMillis() - startTime) + "ms");
        }
        
        ResultFragmentList<LuceneSearchResult<T>> out = new ResultFragmentList<LuceneSearchResult<T>>(results);
        out.setOffset(start);
        out.setFullSize(max);
        return out;
    }
    
    protected TopDocs executeSearch(IndexSearcher searcher, Query query, Filter filter, Sort sort, int collectorLimit) throws IOException {
        // Decide on how to search based on which elements of the lucene query model are available
        if (query != null) {
            // Full scoring search
            TopDocsCollector<? extends ScoreDoc> collector = null;
            if (sort == null) {
                collector = TopScoreDocCollector.create(collectorLimit, true);
            } else {
                SortField sortFields[] = sort.getSort();
                if (sortFields != null && sortFields.length > 0 && 
                        sortFields[0].getType() == SortField.SCORE && 
                        !sortFields[0].getReverse()) {
                    collector = TopScoreDocCollector.create(collectorLimit, true);
                } else {                    
                    collector = TopFieldCollector.create(sort, collectorLimit, false, true, true, true);                    
                }
            }
            searcher.search(query, filter, collector);
            return collector.topDocs();
            
        } else if (filter != null) {            
            // No query = no need for scoring, just dump the results into a hit collector that runs 
            // off the results in the order we want 
            DocIdSetIterator filterMatchesIterator = filter.getDocIdSet(searcher.getIndexReader()).iterator();
            if (sort == null) {
                // no sort available, so the natural iteration order is fine
                // if we have an iterator that means sorting is already handled, so just pull off the first n rows into the output
                ScoreDoc[] scoreDocs = new ScoreDoc[collectorLimit];
                int found = 0;
                int docId;
                while (found < collectorLimit && (docId = filterMatchesIterator.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                    scoreDocs[found++] = new ScoreDoc(docId, 1f);
                }
                return new TopDocs(found, found < collectorLimit ? 
                        Arrays.copyOf(scoreDocs, found) : scoreDocs, 1f);
            } else {
                TopDocsCollector<? extends ScoreDoc> collector = TopFieldCollector.create(sort, collectorLimit, false, true, true, true);
                int docId;
                while ((docId = filterMatchesIterator.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                    collector.collect(docId);
                }
                return collector.topDocs();
                
            }
        } else if (sort != null) {            
            // no query and no filter so no score but add every doc in the index for non-score sorting            
            TopDocsCollector<? extends ScoreDoc> collector = TopFieldCollector.create(sort, collectorLimit, false, true, true, true);
            int numDocs = searcher.getIndexReader().numDocs();
            for  (int n = 0; n < numDocs; n++) {
                collector.collect(n);
            }
            return collector.topDocs();
        } else {
            // no query filter or sort: return the top n docs
            ScoreDoc[] scoreDocs = new ScoreDoc[Math.min(collectorLimit, searcher.getIndexReader().numDocs())];
            
            for (int n = 0; n < scoreDocs.length; n++) {
                scoreDocs[n] = new ScoreDoc(n, 1f);
            }
            return new TopDocs(scoreDocs.length, scoreDocs, 1f);
        }
    }

    public void resolve(List<LuceneSearchResult<T>> results, PagingHint pagingHint, LuceneQueryModel queryModel) throws IOException {
        int length = results instanceof ResultFragmentList<?> ? ((ResultFragmentList<?>) results).getFragmentSize() : results.size();
        IndexReader reader = null;
        try {
            reader = this.readerResolver.getIndexReader(queryModel);
            resolveInternal(new IndexSearcher(reader), results, length, pagingHint, queryModel);
        } finally {
            if (reader != null) {
                this.readerResolver.releaseReader(reader, queryModel);
            }
        }
    }
    
    protected void resolveInternal(IndexSearcher searcher, List<LuceneSearchResult<T>> results, int resultFragmentLength, PagingHint pagingHint, LuceneQueryModel queryModel) throws IOException {
        long resolveStartTime = System.currentTimeMillis();
        this.contentMarshall.deserialize(searcher, results, queryModel);
        log.debug("Resolution of " + resultFragmentLength + " results from lucene in " + (System.currentTimeMillis() - resolveStartTime) + "ms");
    }

    public void setReaderResolver(LuceneReaderResolver readerResolver) {
        if (this.readerResolver != null) {
            try {
                this.readerResolver.reset();
            } catch (IOException err) {
                log.error("Error closing the previous readerResolver", err);
            }
        }
        this.readerResolver = readerResolver;
    }

    public void setDirectory(final LuceneDirectoryBean directory) {
        // default simple one reader implementation
        setReaderResolver(new LuceneReaderResolver() {
            private IndexReader indexReader;
            
            public synchronized IndexReader getIndexReader(LuceneQueryModel queryModel)
                    throws IOException {
                if (this.indexReader == null) {
                    this.indexReader = IndexReader.open(directory.getDirectory(), true);
                }
                return this.indexReader;
            }

            public synchronized void reset() throws IOException {
                if (this.indexReader != null) {
                    this.indexReader.close();
                    this.indexReader = null;
                }
            }

            public void releaseReader(IndexReader reader, LuceneQueryModel queryModel) throws IOException {}
        });
    }
    
    /**
     * Reset internals so we can re-initialize
     */
    public void reset() throws IOException {
        synchronized (this) {
            if (this.readerResolver != null) {
                this.readerResolver.reset();
            }
        }
    }

    public void setContentMarshall(LuceneIndexContentReader<T> contentMarshall) {
        this.contentMarshall = contentMarshall;
    }

	public LuceneIndexContentReader<T> getContentMarshall() {
		return contentMarshall;
	}

    public void setAbsoluteMaxResultCount(int absoluteMaxResultCount) {
        this.absoluteMaxResultCount = absoluteMaxResultCount;
    }
}
