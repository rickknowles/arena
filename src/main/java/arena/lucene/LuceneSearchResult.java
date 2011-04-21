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

/**
 * Valueobject for holding the results of lucene searches
 * 
 * @author <a href="mailto:rick_knowles@hotmail.com">Rick Knowles</a>
 * @version $Id$
 */
public class LuceneSearchResult<T> {
    
    private int luceneDocId = -1;
    private T item;
    private int index;
    private float score;
    private String summary;
    
    public LuceneSearchResult() {}
    public LuceneSearchResult(int luceneDocId, int index, float score) {
        this();
        setLuceneDocId(luceneDocId);
        setIndex(index);
        setScore(score);
    }
    
    public T getItem() {
        return item;
    }
    public void setItem(T item) {
        this.item = item;
    }
    public float getScore() {
        return score;
    }
    public void setScore(float score) {
        this.score = score;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public int getLuceneDocId() {
        return luceneDocId;
    }
    public void setLuceneDocId(int luceneDocId) {
        this.luceneDocId = luceneDocId;
    }
    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
}
