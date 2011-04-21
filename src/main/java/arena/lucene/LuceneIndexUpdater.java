/*
 * Keystone Development Framework
 * Copyright (C) 2004-2009 Rick Knowles
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * Version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License Version 2 for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * Version 2 along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package arena.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;

import arena.action.RequestState;
import arena.collections.ArrayIterable;
import arena.dao.ReadOnlyDAO;


/**
 * Gets a valueobject by an attribute that we interpret to be the primary key.
 * This is basically just to save us from having to do heaps of SearchController
 * calls to return what is a single item. Also cuts down on unnecessary database
 * access, since criteria searches hit the db for id lists even when we query by pk.
 *  
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id: GetValueObjectByIdController.java,v 1.2 2006/05/28 06:35:44 rickknowles Exp $
 */
public class LuceneIndexUpdater<T> {
//	private final Log log = LogManager.getLog(LuceneIndexUpdater.class);

    private ReadOnlyDAO<T> dao;
    private LuceneIndexContentWriter<T> contentMarshall;
    private LuceneDirectoryBean directoryBean;
    private Analyzer analyzer;
    private LuceneIndexSearcher<?>[] searchersToReset;
    
    public String rebuildIndex(RequestState state) throws Exception {
        int indexed = updateIndex(true, this.dao.select().list());
        state.setArg("indexedCount", indexed);
        return "OK";
    }
    
    public int updateIndex(boolean deleteAllFromIndexFirst, T... valueobjects) {
        return updateIndex(deleteAllFromIndexFirst, new ArrayIterable<T>(valueobjects));
    }

    public int updateIndex(boolean deleteAllFromIndexFirst, Iterable<T> valueobjects) {
        IndexWriter writer = null;
        try {
            writer = new IndexWriter(directoryBean.getDirectory(), analyzer, deleteAllFromIndexFirst, MaxFieldLength.LIMITED);
            int docCount = 0;
            for (T vo : valueobjects) {
                Term pkTerm = this.contentMarshall.getPKTerm(vo);
                writer.deleteDocuments(pkTerm);
                
                Document doc = this.contentMarshall.serialize(vo);
                if (doc != null) {
                    writer.addDocument(doc);
                    docCount++;
                }
            }
            if (this.searchersToReset != null) {
                for (LuceneIndexSearcher<?> searcher : this.searchersToReset) {
                    searcher.reset();
                }
            }
            return docCount;
        } catch (IOException err) {
            throw new RuntimeException("Error deleting documents from lucene index", err);
        } finally {
            if (writer != null) {
                try {writer.close();} catch (IOException err) {}
            }
        }
	}

    public LuceneIndexContentWriter<T> getContentMarshall() {
        return contentMarshall;
    }

    public void setContentMarshall(LuceneIndexContentWriter<T> contentMarshall) {
        this.contentMarshall = contentMarshall;
    }

    public LuceneDirectoryBean getDirectoryBean() {
        return directoryBean;
    }

    public void setDirectoryBean(LuceneDirectoryBean directoryBean) {
        this.directoryBean = directoryBean;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setDao(ReadOnlyDAO<T> dao) {
        this.dao = dao;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public void setSearchersToReset(LuceneIndexSearcher<?>[] searchersToReset) {
        this.searchersToReset = searchersToReset;
    }
}
